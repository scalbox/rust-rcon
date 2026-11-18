/*
 * Copyright (c) scalbox.
 *
 * Distributed under the terms of the Apache License, Version 2.0.
 * For more details, see the LICENSE file included in this repository.
 */

package io.graversen.rust.rcon;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.java_websocket.WebSocket;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Minimal in-process RCON endpoint for tests: answers every request with the message returned by the configured
 * responder.
 */
public final class FakeRconServer extends WebSocketServer implements AutoCloseable {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final CountDownLatch started = new CountDownLatch(1);
    private final AtomicInteger openedConnections = new AtomicInteger();
    private volatile Function<JsonNode, @Nullable String> responder =
            request -> response(request.get("Identifier").asInt(), "ok");

    private FakeRconServer(int port) {
        super(new InetSocketAddress("127.0.0.1", port));
        setReuseAddr(true);
        setDaemon(true);
    }

    /**
     * Starts a server on a free local port and waits until it accepts connections.
     *
     * @return the running server
     * @throws Exception if the server does not start within 5 seconds
     */
    public static FakeRconServer startNew() throws Exception {
        final var server = new FakeRconServer(freePort());
        server.start();
        if (!server.started.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Fake RCON server did not start");
        }
        return server;
    }

    /**
     * Returns a local port with no server listening on it.
     *
     * @return a free port
     * @throws IOException if no port can be allocated
     */
    public static int freePort() throws IOException {
        try (var socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    /**
     * Builds a JSON RCON response.
     *
     * @param identifier request identifier
     * @param message    response message
     * @return the JSON response
     */
    public static String response(int identifier, String message) {
        final ObjectNode node = OBJECT_MAPPER.createObjectNode();
        node.put("Identifier", identifier);
        node.put("Message", message);
        node.put("Type", "Generic");
        node.put("Stacktrace", "");
        return node.toString();
    }

    /**
     * Sets the function that builds the response to each request; a {@code null} result sends no response.
     *
     * @param responder the response builder
     */
    public void respondWith(Function<JsonNode, @Nullable String> responder) {
        this.responder = responder;
    }

    /**
     * Returns the number of connections opened since the server started.
     *
     * @return the number of opened connections
     */
    public int openedConnections() {
        return openedConnections.get();
    }

    /**
     * Drops every open connection without a close handshake.
     */
    public void dropConnections() {
        getConnections().forEach(connection -> connection.closeConnection(CloseFrame.ABNORMAL_CLOSE, "dropped"));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        openedConnections.incrementAndGet();
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            final var reply = responder.apply(OBJECT_MAPPER.readTree(message));
            if (reply != null) {
                conn.send(reply);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
    }

    @Override
    public void onStart() {
        started.countDown();
    }

    @Override
    public void close() throws InterruptedException {
        stop(1000);
    }
}
