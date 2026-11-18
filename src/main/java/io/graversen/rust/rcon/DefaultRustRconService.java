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

package io.graversen.rust.rcon;

import com.google.common.eventbus.EventBus;
import io.graversen.rust.rcon.event.AutoConfiguringRustEventService;
import io.graversen.rust.rcon.event.RustEventCapabilities;
import io.graversen.rust.rcon.event.RustEventService;
import io.graversen.rust.rcon.event.rcon.RconLogSubscriber;
import io.graversen.rust.rcon.event.umod.DefaultUmodBridgeManagement;
import io.graversen.rust.rcon.event.umod.UmodBridgeManagement;
import io.graversen.rust.rcon.event.umod.UmodBridgeRustEventService;
import io.graversen.rust.rcon.protocol.Codec;
import io.graversen.rust.rcon.protocol.DefaultRustCodec;
import io.graversen.rust.rcon.protocol.broadcast.BroadcastManagement;
import io.graversen.rust.rcon.protocol.broadcast.DefaultBroadcastManagement;
import io.graversen.rust.rcon.protocol.dto.RustDtoMappers;
import io.graversen.rust.rcon.protocol.dto.ServerInfoDTO;
import io.graversen.rust.rcon.protocol.oxide.DefaultOxideManagement;
import io.graversen.rust.rcon.protocol.oxide.OxideManagement;
import io.graversen.rust.rcon.protocol.player.DefaultPlayerManagement;
import io.graversen.rust.rcon.protocol.player.PlayerManagement;
import io.graversen.rust.rcon.tasks.RconTask;
import io.graversen.rust.rcon.tasks.RustPlayersEmitTask;
import io.graversen.rust.rcon.tasks.RustTeamsEmitTask;
import io.graversen.rust.rcon.tasks.ServerInfoEmitTask;
import io.graversen.rust.rcon.util.DefaultJsonMapper;
import io.graversen.rust.rcon.util.Lazy;
import io.graversen.rust.rcon.ws.ReconnectingRustWebSocketClient;
import io.graversen.rust.rcon.ws.RustWebSocketClient;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Default {@link RustRconService}: connects to a single Rust server over RCON, keeps the player, team and diagnostic
 * snapshots up to date through periodic tasks and exposes the management APIs.
 * <p>
 * {@link #stop()} closes the connection, cancels every task scheduled by the service and releases its scheduler.
 * Periodic tasks run on a scheduler created by the service (a single daemon thread), or on a
 * {@link ScheduledExecutorService} supplied by the caller, which allows many services to share the same threads.
 * A supplied scheduler is never shut down by the service.
 */
@Slf4j
public class DefaultRustRconService implements RustRconService {
    private final AtomicBoolean isRconLogEnabled = new AtomicBoolean(false);
    private final AtomicBoolean isStarted = new AtomicBoolean(false);
    private final AtomicBoolean isStopped = new AtomicBoolean(false);
    private final AtomicBoolean ownsScheduledExecutorService = new AtomicBoolean(false);
    private final AtomicReference<RustDiagnostics> diagnostics = new AtomicReference<>(null);
    private final AtomicReference<List<FullRustPlayer>> rustPlayers = new AtomicReference<>(List.of());
    private final AtomicReference<List<RustTeam>> rustTeams = new AtomicReference<>(List.of());
    private final Queue<ScheduledFuture<?>> scheduledTasks = new ConcurrentLinkedQueue<>();

    private final @NonNull RustRconConfiguration configuration;
    private final @Nullable ScheduledExecutorService sharedScheduledExecutorService;

    private final Lazy<ScheduledExecutorService> scheduledExecutorService = Lazy.of(this::resolveScheduledExecutorService);
    private final Lazy<RustDtoMappers> rustDtoMappers = Lazy.of(this::createRustDtoMappers);
    private final Lazy<EventBus> eventBus = Lazy.of(this::createEventBus);
    private final Lazy<RustEventService> rustEventService = Lazy.of(this::delegatingCreateRustEventService);
    private final Lazy<RustWebSocketClient> rustWebSocketClient = Lazy.of(this::createWebSocketClient);
    private final Lazy<RustRconClient> rustRconClient = Lazy.of(this::createRustRconClient);
    private final Lazy<RustRconRouter> rustRconRouter = Lazy.of(this::createRustRconRouter);
    private final Lazy<Codec> codec = Lazy.of(this::createCodec);
    private final Lazy<OxideManagement> oxideManagement = Lazy.of(this::createOxideManagement);
    private final Lazy<UmodBridgeManagement> umodBridgeManagement = Lazy.of(this::createUmodBridgeManagement);
    private final Lazy<BroadcastManagement> broadcastManagement = Lazy.of(this::createBroadcastManagement);
    private final Lazy<PlayerManagement> playerManagement = Lazy.of(this::createPlayerManagement);

    /**
     * Creates a service that schedules its periodic tasks on a scheduler of its own.
     *
     * @param configuration connection settings of the Rust server
     */
    public DefaultRustRconService(@NonNull RustRconConfiguration configuration) {
        this.configuration = configuration;
        this.sharedScheduledExecutorService = null;
    }

    /**
     * Creates a service that schedules its periodic tasks on the given scheduler. The scheduler can be shared by many
     * services; {@link #stop()} cancels only the tasks of this service and never shuts the scheduler down.
     *
     * @param configuration            connection settings of the Rust server
     * @param scheduledExecutorService scheduler for the periodic tasks
     */
    public DefaultRustRconService(
            @NonNull RustRconConfiguration configuration,
            @NonNull ScheduledExecutorService scheduledExecutorService
    ) {
        this.configuration = configuration;
        this.sharedScheduledExecutorService = scheduledExecutorService;
    }

    /**
     * Returns the codec used to build and send the raw RCON commands.
     *
     * @return the codec
     */
    @Override
    public Codec codec() {
        return codec.get();
    }

    /**
     * Configures the event parsing, starts connecting to the server and schedules the internal tasks. The method does
     * not wait for the connection to be established.
     */
    @Override
    public void start() {
        final var startedAt = LocalDateTime.now();
        log.debug("Starting {}", getClass().getSimpleName());
        isStarted.set(true);
        rustEventService.get().configure();
        rustRconRouter.get().start();
        runConfigure();
        log.debug("Successfully started after {} ms", Duration.between(startedAt, LocalDateTime.now()).toMillis());
    }

    /**
     * Stops the service: cancels every task scheduled through {@link #schedule(RconTask, Duration, Duration)},
     * shuts down the scheduler created by the service (a scheduler supplied to the constructor is left running) and
     * closes the connection, waiting a bounded time for the close handshake. Requests still waiting for a response
     * complete exceptionally. Calling the method more than once, or on a service never started, is allowed.
     */
    @Override
    public void stop() {
        if (!isStopped.compareAndSet(false, true)) {
            return;
        }

        final var startedAt = LocalDateTime.now();
        log.debug("Stopping {}", getClass().getSimpleName());
        releaseScheduledTasks();
        if (isStarted.get()) {
            rustRconRouter.get().stop();
        }
        log.debug("Successfully stopped after {} ms", Duration.between(startedAt, LocalDateTime.now()).toMillis());
    }

    /**
     * Requests the server information.
     *
     * @return a future completed with the server information, or exceptionally if the request fails or times out
     */
    @Override
    public CompletableFuture<ServerInfoDTO> serverInfo() {
        return codec().admin().serverInfo().thenApply(rustDtoMappers.get().mapServerInfo());
    }

    /**
     * Returns the API for managing Oxide/uMod plugins and permissions.
     *
     * @return the Oxide management API
     */
    @Override
    public OxideManagement oxideManagement() {
        return oxideManagement.get();
    }

    /**
     * Returns the API for managing the uMod bridge plugin.
     *
     * @return the uMod bridge management API
     */
    @Override
    public UmodBridgeManagement umodBridgeManagement() {
        return umodBridgeManagement.get();
    }

    /**
     * Returns the API for broadcasting messages to the players.
     *
     * @return the broadcast management API
     */
    @Override
    public BroadcastManagement broadcastManagement() {
        return broadcastManagement.get();
    }

    /**
     * Returns the API for managing players and teams.
     *
     * @return the player management API
     */
    @Override
    public PlayerManagement playerManagement() {
        return playerManagement.get();
    }

    /**
     * Schedules a task to run repeatedly, with a fixed delay between the end of one run and the start of the next.
     * Exceptions thrown by the task are logged and do not stop the following runs. The task is cancelled by
     * {@link #stop()}.
     *
     * @param task         the task to run
     * @param fixedDelay   delay between two consecutive runs
     * @param initialDelay delay before the first run; {@code null} means no delay
     * @throws IllegalStateException if the service has been stopped
     */
    @Override
    public void schedule(@NonNull RconTask task, @NonNull Duration fixedDelay, @Nullable Duration initialDelay) {
        if (isStopped.get()) {
            throw new IllegalStateException(String.format("%s has been stopped", getClass().getSimpleName()));
        }

        final var wrappedTask = wrapRconTask(task);
        final var scheduledTask = scheduledExecutorService.get().scheduleWithFixedDelay(
                wrappedTask,
                Objects.requireNonNullElse(initialDelay, Duration.ZERO).toMillis(),
                fixedDelay.toMillis(),
                TimeUnit.MILLISECONDS
        );
        scheduledTasks.add(scheduledTask);

        if (isStopped.get()) {
            releaseScheduledTasks();
        }
    }

    /**
     * Returns the latest diagnostics derived from the server information.
     *
     * @return the diagnostics, or empty until the first server information has been received
     */
    @Override
    public Optional<RustDiagnostics> diagnostics() {
        return Optional.ofNullable(diagnostics.get());
    }

    /**
     * Returns the players currently connected.
     *
     * @return an immutable snapshot of the players
     */
    @Override
    public List<FullRustPlayer> players() {
        return List.copyOf(rustPlayers.get());
    }

    /**
     * Returns the teams on the server.
     *
     * @return an immutable snapshot of the teams
     */
    @Override
    public List<RustTeam> teams() {
        return List.copyOf(rustTeams.get());
    }

    /**
     * Returns the events that the configured event source can emit.
     *
     * @return the event capabilities
     */
    @Override
    public RustEventCapabilities eventCapabilities() {
        return rustEventService.get().capabilities();
    }

    /**
     * Registers a subscriber for the events posted by this service.
     *
     * @param subscriber object with {@code @Subscribe} methods
     */
    @Override
    public void registerEvents(@NonNull Object subscriber) {
        eventBus.get().register(subscriber);
    }

    /**
     * Logs, at debug level, every RCON message received from the server. Only the first call has an effect.
     */
    public void enableRconLogger() {
        if (isRconLogEnabled.compareAndSet(false, true)) {
            eventBus.get().register(new RconLogSubscriber((client, message) -> log.debug("[{}]: {}", client, message)));
        }
    }

    /**
     * Schedules the internal tasks that refresh the server information (every 5 seconds), the players (every
     * 3 seconds) and the teams (every 5 minutes), and registers the listeners that keep the corresponding snapshots.
     * Called by {@link #start()}.
     */
    protected void configure() {
        final var serverInfoEmitTask = new ServerInfoEmitTask(
                rustRconClient.get().rustServer(),
                this::serverInfo,
                eventBus.get()::post
        );
        registerInternalTask(serverInfoEmitTask, Duration.ofSeconds(5));
        final var rustPlayersEmitTask = new RustPlayersEmitTask(
                rustRconClient.get().rustServer(),
                () -> codec().admin().playerList().thenApply(rustDtoMappers.get().mapRustPlayers()),
                eventBus.get()::post
        );
        registerInternalTask(rustPlayersEmitTask, Duration.ofSeconds(3));
        final var rustTeamsEmitTask = new RustTeamsEmitTask(
                rustRconClient.get().rustServer(),
                () -> playerManagement().getTeams(),
                eventBus.get()::post
        );
        registerInternalTask(rustTeamsEmitTask, Duration.ofMinutes(5), Duration.ofSeconds(10));
        registerRustDiagnosticsListener();
        registerRustPlayerEventListener();
        registerRustTeamsEventListener();
    }

    /**
     * Creates the scheduler used when none is supplied to the constructor. The returned scheduler is owned by the
     * service and shut down by {@link #stop()}. The default implementation creates a single daemon thread named
     * after the server address.
     *
     * @return a new scheduler
     */
    protected ScheduledExecutorService createScheduledExecutorService() {
        final var threadName = String.format("rust-rcon-scheduler-%s:%d", configuration.getHostname(), configuration.getPort());
        return Executors.newSingleThreadScheduledExecutor(runnable -> {
            final var thread = new Thread(runnable, threadName);
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Creates the mappers from raw RCON responses to DTOs.
     *
     * @return the DTO mappers
     */
    protected RustDtoMappers createRustDtoMappers() {
        return new RustDtoMappers(new DefaultJsonMapper());
    }

    /**
     * Creates the codec that sends commands through the router.
     *
     * @return the codec
     */
    protected Codec createCodec() {
        return new DefaultRustCodec(
                rustRconRouter.get()
        );
    }

    /**
     * Creates the Oxide management API.
     *
     * @return the Oxide management API
     */
    protected OxideManagement createOxideManagement() {
        return new DefaultOxideManagement(codec().oxide());
    }

    /**
     * Creates the uMod bridge management API.
     *
     * @return the uMod bridge management API
     */
    protected UmodBridgeManagement createUmodBridgeManagement() {
        return new DefaultUmodBridgeManagement(oxideManagement());
    }

    /**
     * Creates the broadcast management API.
     *
     * @return the broadcast management API
     */
    protected BroadcastManagement createBroadcastManagement() {
        return new DefaultBroadcastManagement(codec());
    }

    /**
     * Creates the player management API.
     *
     * @return the player management API
     */
    protected PlayerManagement createPlayerManagement() {
        return new DefaultPlayerManagement(codec().admin(), rustDtoMappers.get());
    }

    /**
     * Creates the WebSocket client, which reconnects automatically with an increasing delay.
     *
     * @return the WebSocket client
     */
    protected RustWebSocketClient createWebSocketClient() {
        return new ReconnectingRustWebSocketClient(
                configuration.getHostname(),
                configuration.getPort(),
                configuration.getPassword(),
                eventBus.get()
        );
    }

    /**
     * Creates the RCON client, using {@link #requestTimeout()} as the request timeout.
     *
     * @return the RCON client
     */
    protected RustRconClient createRustRconClient() {
        return new DefaultRustRconClient(
                rustWebSocketClient.get(),
                eventBus.get(),
                requestTimeout()
        );
    }

    /**
     * Creates the router that assigns identifiers to the messages and sends them through the RCON client.
     *
     * @return the router
     */
    protected RustRconRouter createRustRconRouter() {
        return new DefaultRustRconRouter(
                rustRconClient.get(),
                eventBus.get()
        );
    }

    /**
     * Creates the event service matching the configured {@link io.graversen.rust.rcon.event.RustEventSourceStrategy}.
     *
     * @param eventBus bus on which the parsed events are posted
     * @return the event service
     */
    protected RustEventService createRustEventService(@NonNull EventBus eventBus) {
        return switch (configuration.getEventSourceStrategy()) {
            case RCON -> new AutoConfiguringRustEventService(eventBus);
            case UMOD -> new UmodBridgeRustEventService(eventBus);
        };
    }

    /**
     * Creates the event bus shared by all the components of this service.
     *
     * @return the event bus
     */
    protected EventBus createEventBus() {
        return new EventBus();
    }

    /**
     * Returns the delay before the first run of the internal tasks that do not specify one.
     *
     * @return the default initial delay, 5 seconds
     */
    protected Duration defaultTaskInitialDelay() {
        return Duration.ofSeconds(5);
    }

    /**
     * Returns the maximum time to wait for the response to a request.
     *
     * @return the request timeout, {@link DefaultRustRconClient#DEFAULT_REQUEST_TIMEOUT} by default
     */
    protected Duration requestTimeout() {
        return DefaultRustRconClient.DEFAULT_REQUEST_TIMEOUT;
    }

    /**
     * Returns the scheduler supplied to the constructor or, if none, the one created by
     * {@link #createScheduledExecutorService()}.
     *
     * @return the scheduler for the periodic tasks
     */
    private ScheduledExecutorService resolveScheduledExecutorService() {
        if (sharedScheduledExecutorService != null) {
            return sharedScheduledExecutorService;
        }

        final var created = createScheduledExecutorService();
        ownsScheduledExecutorService.set(true);
        return created;
    }

    /**
     * Cancels every task scheduled by this service and shuts down the scheduler if it was created by the service.
     */
    private void releaseScheduledTasks() {
        ScheduledFuture<?> scheduledTask;
        while ((scheduledTask = scheduledTasks.poll()) != null) {
            scheduledTask.cancel(false);
        }
        if (ownsScheduledExecutorService.get()) {
            scheduledExecutorService.get().shutdownNow();
        }
    }

    /**
     * Creates the event service, resolving the event bus first.
     *
     * @return the event service
     */
    private RustEventService delegatingCreateRustEventService() {
        final var resolvedEventBus = eventBus.get();
        return createRustEventService(resolvedEventBus);
    }

    /**
     * Wraps a task so that any exception it throws is logged instead of cancelling the following runs.
     *
     * @param rconTask the task to wrap
     * @return the wrapped task
     */
    private Runnable wrapRconTask(@NonNull RconTask rconTask) {
        return () -> {
            try {
                rconTask.run();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        };
    }

    /**
     * Schedules an internal task with the {@linkplain #defaultTaskInitialDelay() default initial delay}.
     *
     * @param task       the task to run
     * @param fixedDelay delay between two consecutive runs
     */
    private void registerInternalTask(@NonNull RconTask task, @NonNull Duration fixedDelay) {
        registerInternalTask(task, fixedDelay, null);
    }

    /**
     * Schedules an internal task.
     *
     * @param task         the task to run
     * @param fixedDelay   delay between two consecutive runs
     * @param initialDelay delay before the first run; {@code null} means the default initial delay
     */
    private void registerInternalTask(@NonNull RconTask task, @NonNull Duration fixedDelay, @Nullable Duration initialDelay) {
        log.info("Registering task: {} (Every {} seconds)", task.getClass().getSimpleName(), fixedDelay.getSeconds());
        initialDelay = Objects.requireNonNullElse(initialDelay, defaultTaskInitialDelay());
        schedule(task, fixedDelay, initialDelay);
    }

    /**
     * Registers the listener that keeps the diagnostics snapshot.
     */
    private void registerRustDiagnosticsListener() {
        log.info("Registering {}", ServerInfoDiagnosticsEventListener.class.getSimpleName());
        registerEvents(new ServerInfoDiagnosticsEventListener(diagnostics::set));
    }

    /**
     * Registers the listener that keeps the players snapshot.
     */
    private void registerRustPlayerEventListener() {
        log.info("Registering {}", RustPlayerEventListener.class.getSimpleName());
        registerEvents(new RustPlayerEventListener(rustPlayers::set));
    }

    /**
     * Registers the listener that keeps the teams snapshot.
     */
    private void registerRustTeamsEventListener() {
        log.info("Registering {}", RustTeamsEventListener.class.getSimpleName());
        registerEvents(new RustTeamsEventListener(rustTeams::set));
    }

    /**
     * Runs {@link #configure()}.
     */
    private void runConfigure() {
        log.info("Running internal configuration hook");
        configure();
    }
}
