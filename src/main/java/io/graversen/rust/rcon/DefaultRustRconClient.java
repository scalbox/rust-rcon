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
import com.google.common.eventbus.Subscribe;
import io.graversen.rust.rcon.event.rcon.RconReceivedEvent;
import io.graversen.rust.rcon.event.ws.WsClosedEvent;
import io.graversen.rust.rcon.event.ws.WsMessageEvent;
import io.graversen.rust.rcon.event.ws.WsOpenedEvent;
import io.graversen.rust.rcon.util.DefaultJsonMapper;
import io.graversen.rust.rcon.util.JsonMapper;
import io.graversen.rust.rcon.util.Lazy;
import io.graversen.rust.rcon.ws.RustWebSocketClient;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Default {@link RustRconClient}, which exchanges JSON RCON messages over a {@link RustWebSocketClient}.
 * <p>
 * Every request is correlated with its response through the request identifier. A request that receives no response
 * within the request timeout ({@link #DEFAULT_REQUEST_TIMEOUT} unless another value is given to the constructor)
 * completes exceptionally with a {@link java.util.concurrent.TimeoutException}; all the requests still waiting for a
 * response complete exceptionally with an {@link IllegalStateException} when the connection is closed.
 */
@Slf4j
public class DefaultRustRconClient implements RustRconClient {
    /**
     * Request timeout used when none is given to the constructor.
     */
    public static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Maximum time {@link #close()} waits for the WebSocket close handshake.
     */
    public static final Duration DEFAULT_CLOSE_TIMEOUT = Duration.ofSeconds(5);

    private final Lazy<RustServer> rustServer = Lazy.of(this::getRustServer);
    private final Lazy<JsonMapper> jsonMapper = Lazy.of(DefaultJsonMapper::new);
    private final Map<Integer, PendingRequest> pendingRequests = new ConcurrentHashMap<>();

    private final @NonNull RustWebSocketClient webSocketClient;
    private final @NonNull EventBus eventBus;
    private final @NonNull Duration requestTimeout;

    /**
     * Creates a client with the {@linkplain #DEFAULT_REQUEST_TIMEOUT default request timeout}.
     *
     * @param webSocketClient websocket connected to the RCON endpoint
     * @param eventBus        bus on which the received messages are posted as {@link RconReceivedEvent}
     */
    public DefaultRustRconClient(@NonNull RustWebSocketClient webSocketClient, @NonNull EventBus eventBus) {
        this(webSocketClient, eventBus, DEFAULT_REQUEST_TIMEOUT);
    }

    /**
     * Creates a client.
     *
     * @param webSocketClient WebSocket connected to the RCON endpoint
     * @param eventBus        bus on which the received messages are posted as {@link RconReceivedEvent}
     * @param requestTimeout  maximum time to wait for the response to a request; must be positive
     * @throws IllegalArgumentException if {@code requestTimeout} is zero or negative
     */
    public DefaultRustRconClient(
            @NonNull RustWebSocketClient webSocketClient,
            @NonNull EventBus eventBus,
            @NonNull Duration requestTimeout
    ) {
        if (requestTimeout.isZero() || requestTimeout.isNegative()) {
            throw new IllegalArgumentException("requestTimeout must be positive: " + requestTimeout);
        }
        this.webSocketClient = webSocketClient;
        this.eventBus = eventBus;
        this.requestTimeout = requestTimeout;
    }

    /**
     * Sends a request over the WebSocket. The returned future completes with the response carrying the same
     * identifier, or exceptionally with:
     * <ul>
     *     <li>{@link IllegalStateException} if the WebSocket is not open, or is closed before the response arrives;</li>
     *     <li>{@link java.util.concurrent.TimeoutException} if no response arrives within the request timeout;</li>
     *     <li>the exception raised by the WebSocket if the message cannot be sent.</li>
     * </ul>
     *
     * @param request the request to send
     * @return a future completed with the response
     */
    @Override
    public CompletableFuture<RustRconResponse> send(@NonNull RustRconRequest request) {
        if (!webSocketClient.isOpen()) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException(String.format("%s instance is not open", webSocketClient.getClass().getSimpleName()))
            );
        }

        final var identifier = request.getIdentifier();
        final var responseFuture = new CompletableFuture<RustRconResponse>();
        final var pendingRequest = new PendingRequest(request, responseFuture);

        final var previousRequest = pendingRequests.put(identifier, pendingRequest);
        if (previousRequest != null) {
            previousRequest.response().completeExceptionally(
                    new IllegalStateException(String.format("Request %d replaced by a new request with the same identifier", identifier))
            );
        }
        responseFuture.whenComplete((response, throwable) -> pendingRequests.remove(identifier, pendingRequest));
        responseFuture.orTimeout(requestTimeout.toMillis(), TimeUnit.MILLISECONDS);

        try {
            final var json = jsonMapper().toJson(mapRequest().apply(request));
            webSocketClient.send(json);
        } catch (Exception e) {
            responseFuture.completeExceptionally(e);
        }

        return responseFuture;
    }

    /**
     * Maps a request to the DTO serialized on the wire, using {@link #name()} as the client name.
     *
     * @return the request mapper
     */
    @Override
    public Function<RustRconRequest, RustRconRequestDTO> mapRequest() {
        return request -> new RustRconRequestDTO(
                request.getIdentifier(),
                request.getMessage(),
                name()
        );
    }

    /**
     * Maps a response DTO received from the server to a {@link RustRconResponse}. A missing identifier is mapped to
     * {@code -1}.
     *
     * @param request the request the response belongs to, or {@code null} for unsolicited messages
     * @return the response mapper
     */
    @Override
    public Function<RustRconResponseDTO, RustRconResponse> mapResponse(@Nullable RustRconRequest request) {
        return rustRconResponseDTO -> new RustRconResponse(
                Objects.requireNonNullElse(rustRconResponseDTO.getIdentifier(), -1),
                rustRconResponseDTO.getMessage(),
                rustRconResponseDTO.getType(),
                rustServer.get(),
                request
        );
    }

    /**
     * Returns the JSON mapper used to serialize requests and parse responses, creating a {@link DefaultJsonMapper}
     * on first use.
     *
     * @return the JSON mapper
     */
    @Override
    public JsonMapper jsonMapper() {
        return jsonMapper.get();
    }

    /**
     * Subscribes this client to the WebSocket events and starts connecting the WebSocket. The method does not wait
     * for the connection to be established.
     */
    @Override
    public void connect() {
        log.debug("Connecting {}", getClass().getSimpleName());
        webSocketClient.registerEvents(this);
        webSocketClient.connect();
    }

    /**
     * Returns the server this client is connected to.
     *
     * @return the server name and redacted URI
     */
    @Override
    public RustServer rustServer() {
        return getRustServer();
    }

    /**
     * Shuts down the WebSocket, waiting at most {@link #closeTimeout()} for the close handshake, and completes
     * exceptionally every request still waiting for a response. The method returns promptly whether or not the
     * connection was ever established. If the calling thread is interrupted while waiting, the method returns
     * with the interrupt flag set.
     *
     * @throws IOException never thrown by this implementation
     */
    @Override
    public void close() throws IOException {
        try {
            webSocketClient.shutdown(closeTimeout());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            failPendingRequests(String.format("%s closed", getClass().getSimpleName()));
        }
    }

    /**
     * Completes exceptionally every request still waiting for a response.
     *
     * @param wsClosedEvent the event posted when the WebSocket was closed
     */
    @Subscribe
    public void onWsClosedEvent(@NonNull WsClosedEvent wsClosedEvent) {
        log.debug("Closed {}: {} ({})", getClass().getSimpleName(), wsClosedEvent.getReason(), wsClosedEvent.getCode());
        failPendingRequests(String.format("Connection closed: %s (Code: %d)", wsClosedEvent.getReason(), wsClosedEvent.getCode()));
    }

    /**
     * Logs that the WebSocket has been opened.
     *
     * @param wsOpenedEvent the event posted when the WebSocket was opened
     */
    @Subscribe
    public void onWsOpenedEvent(@NonNull WsOpenedEvent wsOpenedEvent) {
        log.debug("Connected {}: {}", getClass().getSimpleName(), wsOpenedEvent.getServerUri());
    }

    /**
     * Parses a message received from the server, completes the pending request with the same identifier, if any,
     * and posts the message as a {@link RconReceivedEvent}. Messages whose identifier is below
     * {@link #initialMessageIdentifier()}, such as console output, are only posted.
     *
     * @param messageEvent the event carrying the raw message
     */
    @Subscribe
    public void onWsMessage(@NonNull WsMessageEvent messageEvent) {
        final var rconMessage = jsonMapper().fromJson(messageEvent.getMessage(), RustRconResponseDTO.class);
        final var pendingRequest = pendingRequests.get(rconMessage.getIdentifier());

        final var mappedRconMessage = mapResponse(pendingRequest == null ? null : pendingRequest.request()).apply(rconMessage);

        if (rconMessage.getIdentifier() >= initialMessageIdentifier()) {
            if (pendingRequest != null) {
                pendingRequest.response().complete(mappedRconMessage);
            } else {
                log.warn("Could not resolve response future for ID: {} (Discarding it)", rconMessage.getIdentifier());
            }
        }

        eventBus.post(new RconReceivedEvent(name(), mappedRconMessage));
    }

    /**
     * Registers a subscriber on the event bus of this client.
     *
     * @param subscriber object with {@code @Subscribe} methods
     */
    @Override
    public void registerEvents(@NonNull Object subscriber) {
        eventBus.register(subscriber);
    }

    /**
     * Returns the maximum time to wait for the response to a request.
     *
     * @return the request timeout
     */
    public Duration requestTimeout() {
        return requestTimeout;
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", getClass().getSimpleName(), getRustServer().getServerUri());
    }

    /**
     * Returns the maximum time {@link #close()} waits for the WebSocket close handshake.
     *
     * @return the close timeout, {@link #DEFAULT_CLOSE_TIMEOUT} by default
     */
    protected Duration closeTimeout() {
        return DEFAULT_CLOSE_TIMEOUT;
    }

    /**
     * Completes exceptionally, with an {@link IllegalStateException}, every request still waiting for a response.
     *
     * @param reason message of the exception
     */
    private void failPendingRequests(@NonNull String reason) {
        final var requests = List.copyOf(pendingRequests.values());
        if (!requests.isEmpty()) {
            log.debug("Failing {} pending request(s): {}", requests.size(), reason);
        }
        requests.forEach(pendingRequest -> pendingRequest.response().completeExceptionally(new IllegalStateException(reason)));
    }

    /**
     * Builds the server descriptor from the client name and the redacted WebSocket URI.
     *
     * @return the server descriptor
     */
    private RustServer getRustServer() {
        return new SimpleRustServer(name(), webSocketClient.getServerUri());
    }

    /**
     * A request waiting for its response.
     *
     * @param request  the request sent to the server
     * @param response the future completed with the response
     */
    private record PendingRequest(RustRconRequest request, CompletableFuture<RustRconResponse> response) {
    }
}
