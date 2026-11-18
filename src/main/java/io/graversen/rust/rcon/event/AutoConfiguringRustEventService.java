/*
 * This file is based on the open-source project "Rust RCON"
 * (https://github.com/MrGraversen/rust-rcon), created by
 * Martin Graversen and originally released under the MIT license.
 *
 * Original base package: io.graversen.rust.rcon
 *
 * The code in this fork
 * (https://github.com/scalbox/rust-rcon) has been adapted and is
 * maintained by scalbox and is distributed under the terms of the
 * Apache License, Version 2.0. For more details, see the LICENSE
 * file included in this repository.
 */

package io.graversen.rust.rcon.event;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import io.graversen.rust.rcon.event.rcon.RconReceivedEvent;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * {@link RustEventService} for the {@link RustEventSourceStrategy#RCON} strategy: parses the console messages
 * received over RCON into {@link RustEvent}s and posts them on the event bus.
 * <p>
 * The parsers are discovered on the classpath: every concrete subclass of {@link #eventParserSuperClass()} found in
 * the packages returned by {@link #eventParserPackages()}, parameterized with a {@link RustEvent} type and with a
 * no-argument constructor, is instantiated. The classpath is scanned once per package and the result is shared by
 * every instance in the JVM; each instance creates its own parser objects.
 */
@Slf4j
@RequiredArgsConstructor
public class AutoConfiguringRustEventService extends BaseEventHandler implements RustEventService {
    private static final Map<ScanKey, List<Class<?>>> SCANNED_PARSER_CLASSES = new ConcurrentHashMap<>();

    private final @NonNull EventBus eventBus;
    private volatile List<BaseRustEventParser<?>> eventParsers = List.of();

    /**
     * Passes a received RCON message to every parser that supports it and posts each parsed event, in parser
     * order.
     *
     * @param event the received RCON message
     */
    @Subscribe
    @Override
    public void onRconReceived(@NonNull RconReceivedEvent event) {
        handleEvent(event);
        final var payload = event.getRconResponse();
        eventParsers.stream()
                .filter(eventParser -> eventParser.supports(payload))
                .map(eventParser -> eventParser.parseEvent(payload))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(parsedEvent -> log.debug("Emitting event {} from payload: {}", parsedEvent, payload.getMessage()))
                .forEach(eventBus::post);
    }

    /**
     * Finds the parser classes in the packages returned by {@link #eventParserPackages()}, scanning the classpath
     * only the first time a package is requested in the JVM, instantiates them sorted by
     * {@link BaseRustEventParser#order()} and subscribes this service to the event bus. Logs a warning when no
     * parser could be loaded.
     */
    @Override
    public void configure() {
        log.debug("Configuration begins");
        final var eventParserClasses = Objects.requireNonNullElseGet(eventParserPackages(), Set::<String>of).stream()
                .map(this::findEventParserClasses)
                .flatMap(List::stream)
                .distinct()
                .toList();

        log.debug("Found {} {} candidates, trying to initialise them now", eventParserClasses.size(), eventParserSuperClass().getSimpleName());

        eventParsers = eventParserClasses.stream()
                .map(this::instantiateEventParser)
                .flatMap(Optional::stream)
                .sorted(Comparator.comparingInt(BaseRustEventParser::order))
                .toList();

        if (eventParsers.isEmpty()) {
            log.warn(
                    "No {} found in packages {}: no event will be parsed from the RCON messages",
                    eventParserSuperClass().getSimpleName(),
                    eventParserPackages()
            );
        } else {
            log.debug("Configured {} event parsers", eventParsers.size());
        }

        eventBus.register(this);
        log.debug("Configuration completed");
    }

    /**
     * Returns the events produced by the configured parsers.
     *
     * @return the event capabilities of the RCON strategy
     */
    @Override
    public RustEventCapabilities capabilities() {
        final var supportedEvents = eventParsers.stream()
                .map(BaseRustEventParser::eventClass)
                .collect(Collectors.<Class<? extends RustEvent>>toUnmodifiableSet());

        return new RustEventCapabilities(RustEventSourceStrategy.RCON, supportedEvents);
    }

    /**
     * Returns the packages scanned for parser classes.
     *
     * @return the package names, {@code io.graversen.rust.rcon.event} by default
     */
    protected Set<String> eventParserPackages() {
        return Set.of(
                "io.graversen.rust.rcon.event"
        );
    }

    /**
     * Returns the base class of the parsers to discover.
     *
     * @return the parser base class, {@link BaseRustEventParser} by default
     */
    protected Class<?> eventParserSuperClass() {
        return BaseRustEventParser.class;
    }

    /**
     * Returns the parser classes found in the given package, scanning the classpath only if the package has not
     * been scanned before for the same base class.
     *
     * @param packageName the package to scan
     * @return the parser classes found, possibly empty
     */
    private List<Class<?>> findEventParserClasses(@NonNull String packageName) {
        final var superClass = eventParserSuperClass();
        return SCANNED_PARSER_CLASSES.computeIfAbsent(
                new ScanKey(packageName, superClass),
                key -> scanEventParserClasses(key.packageName(), key.superClass())
        );
    }

    /**
     * Scans the classpath for the concrete subclasses of {@code superClass} in {@code packageName} whose generic
     * superclass is parameterized with a {@link RustEvent} type.
     *
     * @param packageName the package to scan
     * @param superClass  the parser base class
     * @return the parser classes found, possibly empty
     */
    private static List<Class<?>> scanEventParserClasses(@NonNull String packageName, @NonNull Class<?> superClass) {
        log.debug("Scanning package {} for {} subclasses", packageName, superClass.getSimpleName());
        List<Class<?>> classes;
        try {
            final var reflections = new Reflections(new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage(packageName))
                    .setScanners(Scanners.SubTypes));

            classes = reflections.getSubTypesOf(superClass).stream()
                    .filter(AutoConfiguringRustEventService::isConcreteClass)
                    .filter(AutoConfiguringRustEventService::isParameterizedWithRustEvent)
                    .<Class<?>>map(eventParserSubClass -> eventParserSubClass)
                    .toList();
        } catch (Exception e) {
            log.error(String.format("Scanning package %s for event parsers failed: %s", packageName, e.getMessage()), e);
            classes = List.of();
        }

        log.debug("Found {} {} subclasses in package {}", classes.size(), superClass.getSimpleName(), packageName);
        return classes;
    }

    /**
     * Tells whether a class can be instantiated, that is, it is neither an interface nor abstract.
     *
     * @param type the class to check
     * @return {@code true} if the class is concrete
     */
    private static boolean isConcreteClass(@NonNull Class<?> type) {
        return !type.isInterface() && !Modifier.isAbstract(type.getModifiers());
    }

    /**
     * Tells whether the generic superclass of a class has a {@link RustEvent} type among its type arguments, as in
     * {@code BaseRustEventParser<PlayerChatEvent>}.
     *
     * @param type the class to check
     * @return {@code true} if the class parses a {@link RustEvent} type
     */
    private static boolean isParameterizedWithRustEvent(@NonNull Class<?> type) {
        return type.getGenericSuperclass() instanceof ParameterizedType parameterizedType
                && Arrays.stream(parameterizedType.getActualTypeArguments())
                .anyMatch(typeArgument -> typeArgument instanceof Class<?> typeClass && RustEvent.class.isAssignableFrom(typeClass));
    }

    /**
     * Creates an instance of a parser class through its no-argument constructor.
     *
     * @param eventParserClass the parser class
     * @return the parser, or empty if the class cannot be instantiated or is not a parser
     */
    private Optional<BaseRustEventParser<?>> instantiateEventParser(@NonNull Class<?> eventParserClass) {
        try {
            final var instance = eventParserClass.getDeclaredConstructor().newInstance();
            if (instance instanceof BaseRustEventParser<?> baseEventParser) {
                log.debug("Successfully initialised {} instance!", instance.getClass().getSimpleName());
                return Optional.of(baseEventParser);
            }
        } catch (Exception e) {
            log.error(String.format("Could not instantiate event parser %s: %s", eventParserClass.getName(), e.getMessage()), e);
        }

        return Optional.empty();
    }

    /**
     * Identifies a classpath scan.
     *
     * @param packageName the scanned package
     * @param superClass  the parser base class looked for
     */
    private record ScanKey(String packageName, Class<?> superClass) {
    }
}
