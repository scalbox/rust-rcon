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
import io.graversen.rust.rcon.event.ws.WsClosedEvent;
import io.graversen.rust.rcon.event.ws.WsErrorEvent;
import io.graversen.rust.rcon.event.ws.WsMessageEvent;
import io.graversen.rust.rcon.event.ws.WsOpenedEvent;
import io.graversen.rust.rcon.util.CommonUtils;
import io.graversen.rust.rcon.util.EventEmitter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ServerHandshake;

import java.io.Closeable;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * WebSocket client for the RCON endpoint of a Rust server.
 * <p>
 * Every websocket lifecycle callback is translated into an event ({@link WsOpenedEvent}, {@link WsMessageEvent},
 * {@link WsErrorEvent}, {@link WsClosedEvent}) and posted on the {@link EventBus} supplied at construction time.
 * The RCON password is part of the connection URI but is never exposed: {@link #getServerUri()} returns a redacted
 * form of it.
 */
@Slf4j
public class RustWebSocketClient extends WebSocketClient implements Closeable, EventEmitter {
    private static final String LOG_TEMPLATE = "[${serverUri}]: ${message}";
    private static final String SERVER_URI_TEMPLATE = "ws://${hostname}:${port}/${password}";
    private static final String SERVER_URI_TEMPLATE_REDACTED = "ws://${hostname}:${port}/*";

    private final EventBus eventBus;
    private final String serverUriRedacted;
    private final AtomicBoolean isOpen;
    private final AtomicBoolean isShutdown;
    private final Object closeMonitor = new Object();

    /**
     * Creates a client for the RCON endpoint {@code ws://hostname:port/password}.
     *
     * @param hostname host name or IP address of the Rust server
     * @param port     RCON port of the Rust server
     * @param password RCON password
     * @param eventBus bus on which the WebSocket events are posted
     */
    public RustWebSocketClient(
            @NonNull String hostname,
            @NonNull Integer port,
            @NonNull String password,
            @NonNull EventBus eventBus
    ) {
        this(serverUri(hostname, port, password), eventBus);
    }

    /**
     * Creates a client for the given RCON endpoint.
     *
     * @param serverUri full WebSocket URI of the RCON endpoint, password included
     * @param eventBus  bus on which the WebSocket events are posted
     */
    public RustWebSocketClient(@NonNull URI serverUri, @NonNull EventBus eventBus) {
        super(serverUri);
        this.eventBus = eventBus;
        this.serverUriRedacted = StringSubstitutor.replace(
                SERVER_URI_TEMPLATE_REDACTED,
                Map.of(
                        "hostname", serverUri.getHost(),
                        "port", serverUri.getPort()
                )
        );
        this.isOpen = new AtomicBoolean(false);
        this.isShutdown = new AtomicBoolean(false);
    }

    /**
     * Marks the connection as open, applies the connection-lost timeout and posts a {@link WsOpenedEvent}.
     * If {@link #shutdown(Duration)} has already been called, the new connection is closed right away instead.
     *
     * @param serverHandshake handshake data sent by the server
     */
    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        if (isShutdown()) {
            log(log::debug, "Connection opened after shutdown, closing it");
            close(CloseFrame.NORMAL, "Client is shut down");
            return;
        }

        isOpen.set(true);
        setConnectionLostTimeout(connectionLostTimeout().toSecondsPart());
        eventBus.post(new WsOpenedEvent(serverUriRedacted));
    }

    /**
     * Posts a {@link WsMessageEvent} carrying the raw text frame received from the server.
     *
     * @param message raw text frame
     */
    @Override
    public void onMessage(String message) {
        eventBus.post(new WsMessageEvent(serverUriRedacted, message));
    }

    /**
     * Marks the connection as closed, wakes up any thread waiting in {@link #shutdown(Duration)} and posts a
     * {@link WsClosedEvent}.
     *
     * @param code   WebSocket close code
     * @param reason close reason
     * @param remote {@code true} if the close was initiated by the server
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        isOpen.set(false);
        synchronized (closeMonitor) {
            closeMonitor.notifyAll();
        }
        eventBus.post(new WsClosedEvent(serverUriRedacted, code, reason));
    }

    /**
     * Logs the error and posts a {@link WsErrorEvent}.
     *
     * @param ex the error raised by the WebSocket
     */
    @Override
    public void onError(Exception ex) {
        log.error(ex.getMessage(), ex);
        eventBus.post(new WsErrorEvent(serverUriRedacted, ex));
    }

    /**
     * Closes the connection and prevents any further connection, waiting at most {@code timeout} for the close
     * handshake to complete. When the handshake does not complete in time, the underlying socket is closed abruptly.
     * When the connection is not open (never established, or already lost) the method returns immediately.
     * Only the first call has an effect.
     *
     * @param timeout maximum time to wait for the close handshake
     * @throws InterruptedException if the calling thread is interrupted while waiting
     */
    public void shutdown(@NonNull Duration timeout) throws InterruptedException {
        if (!isShutdown.compareAndSet(false, true)) {
            return;
        }

        if (!isOpen()) {
            log(log::debug, "Shutting down a connection that is not open");
            close();
            return;
        }

        close(CloseFrame.NORMAL, "Client is shut down");
        if (!awaitClosed(timeout)) {
            log(log::warn, "Close handshake not completed within %d ms, closing the connection", timeout.toMillis());
            closeConnection(CloseFrame.ABNORMAL_CLOSE, "Close handshake timed out");
        }
    }

    /**
     * Tells whether {@link #shutdown(Duration)} has been called.
     *
     * @return {@code true} once the client has been shut down
     */
    public boolean isShutdown() {
        return isShutdown.get();
    }

    /**
     * Registers a subscriber for the websocket events posted by this client.
     *
     * @param subscriber object with {@code @Subscribe} methods
     */
    @Override
    public void registerEvents(@NonNull Object subscriber) {
        eventBus.register(subscriber);
    }

    /**
     * Tells whether the connection is currently open.
     *
     * @return {@code true} between {@link #onOpen(ServerHandshake)} and {@link #onClose(int, String, boolean)}
     */
    @Override
    public boolean isOpen() {
        return isOpen.get();
    }

    /**
     * Returns the RCON endpoint with the password replaced by {@code *}.
     *
     * @return the redacted URI, in the form {@code ws://hostname:port/*}
     */
    public String getServerUri() {
        return serverUriRedacted;
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", getClass().getSimpleName(), getServerUri());
    }

    /**
     * Logs a message at info level, prefixed with the redacted server URI.
     *
     * @param message {@link String#format(String, Object...)} pattern
     * @param args    pattern arguments
     */
    protected void log(@NonNull String message, Object... args) {
        log(log::info, message, args);
    }

    /**
     * Returns the interval used by the websocket to detect a lost connection through ping/pong frames.
     * {@link Duration#ZERO} disables the detection.
     *
     * @return the connection-lost timeout; only its seconds part is used
     */
    protected Duration connectionLostTimeout() {
        return Duration.ZERO;
    }

    /**
     * Logs a message with the given logger, prefixed with the redacted server URI.
     *
     * @param logger  logging method, for example {@code log::warn}
     * @param message {@link String#format(String, Object...)} pattern
     * @param args    pattern arguments
     */
    protected void log(@NonNull Consumer<String> logger, @NonNull String message, Object... args) {
        message = String.format(message, args);
        message = StringSubstitutor.replace(
                LOG_TEMPLATE,
                Map.of(
                        "serverUri", serverUriRedacted,
                        "message", message
                )
        );
        logger.accept(message);
    }

    /**
     * Waits until the connection is closed or the timeout elapses.
     *
     * @param timeout maximum time to wait
     * @return {@code true} if the connection is closed
     * @throws InterruptedException if the calling thread is interrupted while waiting
     */
    private boolean awaitClosed(@NonNull Duration timeout) throws InterruptedException {
        final long deadline = System.nanoTime() + timeout.toNanos();
        synchronized (closeMonitor) {
            while (isOpen()) {
                final long remainingMillis = Duration.ofNanos(deadline - System.nanoTime()).toMillis();
                if (remainingMillis <= 0) {
                    return false;
                }
                closeMonitor.wait(remainingMillis);
            }
        }
        return true;
    }

    /**
     * Builds the RCON endpoint URI {@code ws://hostname:port/password}.
     *
     * @param hostname host name or IP address of the Rust server
     * @param port     RCON port of the Rust server
     * @param password RCON password
     * @return the endpoint URI
     */
    private static URI serverUri(
            @NonNull String hostname,
            @NonNull Integer port,
            @NonNull String password
    ) {
        final var compiledServerUri = StringSubstitutor.replace(
                SERVER_URI_TEMPLATE,
                Map.of(
                        "hostname", hostname,
                        "port", port,
                        "password", password
                )
        );
        return CommonUtils.uri(compiledServerUri);
    }
}
