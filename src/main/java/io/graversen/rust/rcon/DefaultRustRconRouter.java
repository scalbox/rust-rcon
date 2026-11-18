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
import io.graversen.rust.rcon.event.rcon.RconProtocolExchangeEvent;
import io.graversen.rust.rcon.protocol.RustRconMessage;
import io.graversen.rust.rcon.util.CommonExecutor;
import io.graversen.rust.rcon.util.EventEmitter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Default {@link RustRconRouter}: assigns an increasing identifier to every message, sends it through a
 * {@link RustRconClient} and posts a {@link RconProtocolExchangeEvent} for every completed exchange.
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultRustRconRouter implements RustRconRouter, EventEmitter {
    private final RustRconClient rconClient;
    private final EventBus eventBus;
    private Supplier<Integer> identifierGenerator;

    /**
     * Creates the generator of request identifiers, which counts up from
     * {@link RustRconClient#initialMessageIdentifier()}.
     *
     * @return the identifier generator
     */
    protected Supplier<Integer> identifierGenerator() {
        final var atomicInteger = new AtomicInteger(rconClient.initialMessageIdentifier());
        return atomicInteger::getAndIncrement;
    }

    /**
     * Connects the underlying client. The method does not wait for the connection to be established.
     */
    @Override
    public void start() {
        log.debug("Starting {}", getClass().getSimpleName());
        rconClient.connect();
    }

    /**
     * Sends a message and maps the response.
     *
     * @param rustRconMessage the message to send
     * @param mapper          function applied to the response
     * @param <T>             type of the mapped response
     * @return a future completed with the mapped response, or exceptionally as described in {@link #send(RustRconMessage)}
     */
    @Override
    public <T> CompletableFuture<T> send(@NonNull RustRconMessage rustRconMessage, @NonNull Function<RustRconResponse, T> mapper) {
        return send(rustRconMessage).thenApply(mapper);
    }

    /**
     * Sends a message with the next identifier. When the exchange completes, a {@link RconProtocolExchangeEvent} is
     * posted on the shared {@link CommonExecutor}; a failed exchange is logged instead.
     *
     * @param rustRconMessage the message to send
     * @return a future completed with the response, or exceptionally if the client is not connected, the response does
     * not arrive in time or the connection is closed
     */
    @Override
    public CompletableFuture<RustRconResponse> send(@NonNull RustRconMessage rustRconMessage) {
        final var identifier = getIdentifierGenerator().get();
        final var message = rustRconMessage.get();
        final var rconRequest = new RustRconRequest(identifier, message);

        return rconClient.send(rconRequest).whenCompleteAsync(handleRconProtocolExchange(rconRequest), CommonExecutor.getInstance());
    }

    /**
     * Closes the underlying client.
     */
    @Override
    @SneakyThrows
    public void stop() {
        rconClient.close();
    }

    /**
     * Returns the identifier generator, creating it on first use.
     *
     * @return the identifier generator
     */
    @Synchronized
    private Supplier<Integer> getIdentifierGenerator() {
        if (identifierGenerator == null) {
            identifierGenerator = identifierGenerator();
        }

        return identifierGenerator;
    }

    /**
     * Registers a subscriber on the event bus of this router.
     *
     * @param subscriber object with {@code @Subscribe} methods
     */
    @Override
    public void registerEvents(@NonNull Object subscriber) {
        eventBus.register(subscriber);
    }

    /**
     * Creates the callback run when an exchange completes. A successful exchange is posted as a
     * {@link RconProtocolExchangeEvent}. A failure caused by a timeout or by a missing or closed connection is
     * logged as a single warning line; any other failure is logged with its stack trace.
     *
     * @param request the request sent to the server
     * @return the completion callback
     */
    private BiConsumer<RustRconResponse, Throwable> handleRconProtocolExchange(@NonNull RustRconRequest request) {
        return (response, throwable) -> {
            if (throwable != null) {
                logFailedExchange(request, throwable);
            } else if (response != null) {
                eventBus.post(new RconProtocolExchangeEvent(request, response));
            } else {
                log.error("Unable to determine outcome of RCON protocol exchange");
            }
        };
    }

    /**
     * Logs a failed exchange: timeouts and missing or closed connections as a single warning line, any other
     * failure with its stack trace.
     *
     * @param request   the request sent to the server
     * @param throwable the failure, possibly wrapped in a {@link CompletionException}
     */
    private void logFailedExchange(@NonNull RustRconRequest request, @NonNull Throwable throwable) {
        final var cause = throwable instanceof CompletionException && throwable.getCause() != null
                ? throwable.getCause()
                : throwable;
        if (cause instanceof TimeoutException) {
            log.warn("No response to RCON request {} within the timeout: {}", request.getIdentifier(), request.getMessage());
        } else if (cause instanceof IllegalStateException) {
            log.warn("RCON request {} failed: {}", request.getIdentifier(), cause.getMessage());
        } else {
            log.error(cause.getMessage(), cause);
        }
    }
}
