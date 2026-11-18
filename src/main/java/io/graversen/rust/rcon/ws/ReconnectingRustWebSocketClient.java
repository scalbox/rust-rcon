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

package io.graversen.rust.rcon.ws;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import io.graversen.rust.rcon.event.ws.WsClosedEvent;
import io.graversen.rust.rcon.event.ws.WsOpenedEvent;
import io.graversen.rust.rcon.tasks.ReconnectRconTask;
import io.graversen.rust.rcon.util.CommonExecutor;
import io.graversen.rust.rcon.util.CommonUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.framing.CloseFrame;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link RustWebSocketClient} that reconnects automatically whenever the connection is lost: when the server closes
 * it, with any close code, when it is closed with a code other than {@link CloseFrame#NORMAL} (for example when the
 * connection-lost detection gives up), and when a connection attempt fails. A normal close requested by this client,
 * such as {@link #close()}, does not trigger a reconnection.
 * <p>
 * The delay between attempts grows exponentially from {@link #minReconnectDelay()} up to
 * {@link #maxReconnectDelay()}, with a random component, and returns to the minimum once a connection is opened.
 * No further attempt is made after {@link #shutdown(Duration)}.
 */
@Slf4j
public class ReconnectingRustWebSocketClient extends RustWebSocketClient {
    private static final Duration DEFAULT_MIN_RECONNECT_DELAY = Duration.ofSeconds(1);
    private static final Duration DEFAULT_MAX_RECONNECT_DELAY = Duration.ofSeconds(60);
    private static final int MAX_BACKOFF_EXPONENT = 20;

    private final ScheduledExecutorService reconnectService;
    private final AtomicInteger reconnectAttempts = new AtomicInteger();
    private final AtomicReference<ScheduledFuture<?>> pendingReconnect = new AtomicReference<>();

    /**
     * Creates a reconnecting client for the RCON endpoint {@code ws://hostname:port/password}. Reconnection attempts
     * run on the shared {@link CommonExecutor}.
     *
     * @param hostname host name or IP address of the Rust server
     * @param port     RCON port of the Rust server
     * @param password RCON password
     * @param eventBus bus on which the WebSocket events are posted
     */
    public ReconnectingRustWebSocketClient(@NonNull String hostname, @NonNull Integer port, @NonNull String password, @NonNull EventBus eventBus) {
        super(hostname, port, password, eventBus);
        this.reconnectService = CommonExecutor.getInstance();
    }

    /**
     * Subscribes this client to its own websocket events and starts connecting.
     */
    @Override
    public void connect() {
        log.debug("Connecting {}", getClass().getSimpleName());
        registerEvents(this);
        super.connect();
    }

    /**
     * Cancels any scheduled reconnection attempt, then closes the connection as described in
     * {@link RustWebSocketClient#shutdown(Duration)}.
     *
     * @param timeout maximum time to wait for the close handshake
     * @throws InterruptedException if the calling thread is interrupted while waiting
     */
    @Override
    public void shutdown(@NonNull Duration timeout) throws InterruptedException {
        try {
            super.shutdown(timeout);
        } finally {
            cancelPendingReconnect();
        }
    }

    /**
     * Resets the reconnection attempt counter, so that the next reconnection starts again from the minimum delay.
     *
     * @param wsOpenedEvent the event posted when the connection was opened
     */
    @Subscribe
    public void onWsOpenedEvent(@NonNull WsOpenedEvent wsOpenedEvent) {
        reconnectAttempts.set(0);
    }

    /**
     * Schedules a reconnection attempt when the client has not been shut down and the connection was either closed
     * by the server, with any code, or closed with a code other than {@link CloseFrame#NORMAL}. The delay is given by
     * {@link #reconnectDelay(int)}.
     *
     * @param wsClosedEvent the event posted when the connection was closed
     */
    @Subscribe
    public void onWsClosedEvent(@NonNull WsClosedEvent wsClosedEvent) {
        if (isShutdown() || (wsClosedEvent.getCode() == CloseFrame.NORMAL && !wsClosedEvent.isRemote())) {
            return;
        }

        final var attempt = reconnectAttempts.getAndIncrement();
        final var reconnectDelay = Objects.requireNonNullElse(reconnectDelay(attempt), minReconnectDelay());
        log(
                log::info,
                "Unexpected close: %s (Code: %d) - Attempting to reconnect after %d ms (attempt %d)",
                CommonUtils.isNullOrEmpty(wsClosedEvent.getReason()) ? "(none)" : wsClosedEvent.getReason(),
                wsClosedEvent.getCode(),
                reconnectDelay.toMillis(),
                attempt + 1
        );

        final var scheduledReconnect = reconnectService.schedule(
                new ReconnectRconTask(this::reconnectUnlessShutdown),
                reconnectDelay.toMillis(),
                TimeUnit.MILLISECONDS
        );
        pendingReconnect.set(scheduledReconnect);
        if (isShutdown()) {
            cancelPendingReconnect();
        }
    }

    /**
     * Computes the delay before a reconnection attempt. The upper bound doubles at every attempt, starting from
     * {@link #minReconnectDelay()} and never exceeding {@link #maxReconnectDelay()}; the returned delay is a random
     * value between half of that bound and the bound itself, and never below {@link #minReconnectDelay()}.
     *
     * @param attempt number of attempts already made since the last open connection, starting from 0
     * @return the delay to wait before the attempt
     */
    protected Duration reconnectDelay(int attempt) {
        final long minMillis = Math.max(1, minReconnectDelay().toMillis());
        final long maxMillis = Math.max(minMillis, maxReconnectDelay().toMillis());
        final int exponent = Math.min(Math.max(attempt, 0), MAX_BACKOFF_EXPONENT);
        final long boundMillis = Math.min(maxMillis, minMillis << exponent);
        final long halfBoundMillis = boundMillis / 2;
        final long delayMillis = halfBoundMillis + ThreadLocalRandom.current().nextLong(boundMillis - halfBoundMillis + 1);
        return Duration.ofMillis(Math.max(minMillis, delayMillis));
    }

    /**
     * Returns the delay before the first reconnection attempt.
     *
     * @return the minimum reconnection delay, 1 second by default
     */
    protected Duration minReconnectDelay() {
        return DEFAULT_MIN_RECONNECT_DELAY;
    }

    /**
     * Returns the upper bound of the delay between reconnection attempts.
     *
     * @return the maximum reconnection delay, 60 seconds by default
     */
    protected Duration maxReconnectDelay() {
        return DEFAULT_MAX_RECONNECT_DELAY;
    }

    /**
     * Reconnects the websocket, unless the client has been shut down in the meantime.
     */
    private void reconnectUnlessShutdown() {
        if (!isShutdown()) {
            reconnect();
        }
    }

    /**
     * Cancels the scheduled reconnection attempt, if any.
     */
    private void cancelPendingReconnect() {
        final var scheduledReconnect = pendingReconnect.getAndSet(null);
        if (scheduledReconnect != null) {
            scheduledReconnect.cancel(false);
        }
    }
}
