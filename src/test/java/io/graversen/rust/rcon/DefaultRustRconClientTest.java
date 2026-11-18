/*
 * Copyright (c) scalbox.
 *
 * Distributed under the terms of the Apache License, Version 2.0.
 * For more details, see the LICENSE file included in this repository.
 */

package io.graversen.rust.rcon;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import io.graversen.rust.rcon.event.rcon.RconReceivedEvent;
import io.graversen.rust.rcon.event.ws.WsOpenedEvent;
import io.graversen.rust.rcon.ws.RustWebSocketClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultRustRconClientTest {
    private FakeRconServer server;
    private DefaultRustRconClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = FakeRconServer.startNew();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (client != null) {
            client.close();
        }
        server.close();
    }

    @Test
    void sendCompletesWithTheMatchingResponse() throws Exception {
        connectedClient(Duration.ofSeconds(5));

        final var response = client.send(new RustRconRequest(1, "serverinfo")).get(5, TimeUnit.SECONDS);

        assertEquals(1, response.getIdentifier());
        assertEquals("ok", response.getMessage());
        assertEquals("serverinfo", response.getRequest().getMessage());
    }

    @Test
    void sendFailsWithTimeoutWhenNoResponseArrives() throws Exception {
        server.respondWith(request -> null);
        connectedClient(Duration.ofMillis(200));

        final var future = client.send(new RustRconRequest(1, "serverinfo"));

        final var exception = assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS));
        assertInstanceOf(TimeoutException.class, exception.getCause());
    }

    @Test
    void pendingRequestsFailWhenTheConnectionIsLost() throws Exception {
        server.respondWith(request -> null);
        connectedClient(Duration.ofSeconds(30));

        final var future = client.send(new RustRconRequest(1, "serverinfo"));
        server.dropConnections();

        final var exception = assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS));
        assertInstanceOf(IllegalStateException.class, exception.getCause());
    }

    @Test
    void closeFailsPendingRequests() throws Exception {
        server.respondWith(request -> null);
        connectedClient(Duration.ofSeconds(30));

        final var future = client.send(new RustRconRequest(1, "serverinfo"));
        client.close();

        final var exception = assertThrows(ExecutionException.class, () -> future.get(5, TimeUnit.SECONDS));
        assertInstanceOf(IllegalStateException.class, exception.getCause());
    }

    @Test
    void messageWithoutIdentifierIsPostedAsEvent() throws Exception {
        final var bus = connectedClient(Duration.ofSeconds(5));
        final var receivedEvents = new LinkedBlockingQueue<RconReceivedEvent>();
        bus.register(new ReceivedEventCollector(receivedEvents));

        server.broadcast("{\"Message\":\"console output\",\"Type\":\"Generic\",\"Stacktrace\":\"\"}");

        final var event = receivedEvents.poll(5, TimeUnit.SECONDS);
        assertNotNull(event, "message not posted");
        assertEquals(-1, event.getRconResponse().getIdentifier());
        assertEquals("console output", event.getRconResponse().getMessage());
    }

    @Test
    void sendFailsImmediatelyWhenNotConnected() {
        final var bus = new EventBus();
        client = new DefaultRustRconClient(new RustWebSocketClient("127.0.0.1", server.getPort(), "secret", bus), bus);

        final var future = client.send(new RustRconRequest(1, "serverinfo"));

        assertTrue(future.isCompletedExceptionally());
        final var exception = assertThrows(ExecutionException.class, future::get);
        assertInstanceOf(IllegalStateException.class, exception.getCause());
    }

    @Test
    void closeReturnsPromptlyWhenNeverConnected() throws Exception {
        final var bus = new EventBus();
        client = new DefaultRustRconClient(new RustWebSocketClient("127.0.0.1", FakeRconServer.freePort(), "secret", bus), bus);
        client.connect();

        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> client.close());
    }

    @Test
    void closeReturnsPromptlyWhenConnected() throws Exception {
        connectedClient(Duration.ofSeconds(5));

        assertTimeoutPreemptively(Duration.ofSeconds(6), () -> client.close());
    }

    @Test
    void rejectsNonPositiveRequestTimeout() {
        final var bus = new EventBus();
        try (var webSocketClient = new RustWebSocketClient("127.0.0.1", server.getPort(), "secret", bus)) {
            assertThrows(IllegalArgumentException.class, () -> new DefaultRustRconClient(webSocketClient, bus, Duration.ZERO));
        }
    }

    private EventBus connectedClient(Duration requestTimeout) throws InterruptedException {
        final var bus = new EventBus();
        final var opened = new CountDownLatch(1);
        bus.register(new OpenedListener(opened));
        client = new DefaultRustRconClient(new RustWebSocketClient("127.0.0.1", server.getPort(), "secret", bus), bus, requestTimeout);
        client.connect();
        assertTrue(opened.await(5, TimeUnit.SECONDS), "connection not opened");
        return bus;
    }

    public static final class OpenedListener {
        private final CountDownLatch opened;

        OpenedListener(CountDownLatch opened) {
            this.opened = opened;
        }

        @Subscribe
        public void onOpened(WsOpenedEvent event) {
            opened.countDown();
        }
    }

    public static final class ReceivedEventCollector {
        private final BlockingQueue<RconReceivedEvent> events;

        ReceivedEventCollector(BlockingQueue<RconReceivedEvent> events) {
            this.events = events;
        }

        @Subscribe
        public void onReceived(RconReceivedEvent event) {
            events.add(event);
        }
    }
}
