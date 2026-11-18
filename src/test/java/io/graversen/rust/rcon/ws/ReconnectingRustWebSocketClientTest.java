/*
 * Copyright (c) scalbox.
 *
 * Distributed under the terms of the Apache License, Version 2.0.
 * For more details, see the LICENSE file included in this repository.
 */

package io.graversen.rust.rcon.ws;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import io.graversen.rust.rcon.FakeRconServer;
import io.graversen.rust.rcon.event.ws.WsClosedEvent;
import io.graversen.rust.rcon.event.ws.WsOpenedEvent;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReconnectingRustWebSocketClientTest {
    @Test
    void reconnectDelayGrowsExponentiallyWithinBounds() {
        try (var client = new ReconnectingRustWebSocketClient("127.0.0.1", 1, "secret", new EventBus())) {
            final long min = client.minReconnectDelay().toMillis();
            final long max = client.maxReconnectDelay().toMillis();

            for (int i = 0; i < 100; i++) {
                assertEquals(min, client.reconnectDelay(0).toMillis());

                final long second = client.reconnectDelay(1).toMillis();
                assertTrue(second >= min && second <= 2 * min, "attempt 1: " + second);

                final long fourth = client.reconnectDelay(3).toMillis();
                assertTrue(fourth >= 4 * min && fourth <= 8 * min, "attempt 3: " + fourth);

                final long capped = client.reconnectDelay(1000).toMillis();
                assertTrue(capped >= max / 2 && capped <= max, "attempt 1000: " + capped);
            }
        }
    }

    @Test
    void reconnectsAfterTheConnectionIsLost() throws Exception {
        try (var server = FakeRconServer.startNew()) {
            final var bus = new EventBus();
            final var opened = new Semaphore(0);
            bus.register(new OpenedListener(opened));
            try (var client = new ReconnectingRustWebSocketClient("127.0.0.1", server.getPort(), "secret", bus)) {
                client.connect();
                assertTrue(opened.tryAcquire(5, TimeUnit.SECONDS), "first connection not opened");

                server.dropConnections();

                assertTrue(opened.tryAcquire(10, TimeUnit.SECONDS), "connection not reopened");
                assertEquals(2, server.openedConnections());
                client.shutdown(Duration.ofSeconds(5));
            }
        }
    }

    @Test
    void noReconnectAfterShutdown() throws Exception {
        try (var server = FakeRconServer.startNew()) {
            final var bus = new EventBus();
            final var opened = new Semaphore(0);
            bus.register(new OpenedListener(opened));
            try (var client = new ReconnectingRustWebSocketClient("127.0.0.1", server.getPort(), "secret", bus)) {
                client.connect();
                assertTrue(opened.tryAcquire(5, TimeUnit.SECONDS), "connection not opened");

                assertTimeoutPreemptively(Duration.ofSeconds(6), () -> client.shutdown(Duration.ofSeconds(5)));
                server.dropConnections();

                Thread.sleep(2500);
                assertEquals(1, server.openedConnections());
                assertTrue(client.isShutdown());
            }
        }
    }

    @Test
    void unreachableServerStopsRetryingAfterShutdown() throws Exception {
        final var bus = new EventBus();
        final var closedEvents = new AtomicInteger();
        bus.register(new ClosedCounter(closedEvents));
        try (var client = new ReconnectingRustWebSocketClient("127.0.0.1", FakeRconServer.freePort(), "secret", bus)) {
            client.connect();
            assertTimeoutPreemptively(Duration.ofSeconds(1), () -> client.shutdown(Duration.ofSeconds(5)));

            Thread.sleep(4000);
            assertTrue(closedEvents.get() <= 1, "close events: " + closedEvents.get());
        }
    }

    @Test
    void reconnectsWhenTheServerClosesNormally() throws Exception {
        try (var server = FakeRconServer.startNew()) {
            final var bus = new EventBus();
            final var opened = new Semaphore(0);
            bus.register(new OpenedListener(opened));
            try (var client = new ReconnectingRustWebSocketClient("127.0.0.1", server.getPort(), "secret", bus)) {
                client.connect();
                assertTrue(opened.tryAcquire(5, TimeUnit.SECONDS), "first connection not opened");

                server.closeConnectionsNormally();

                assertTrue(opened.tryAcquire(10, TimeUnit.SECONDS), "connection not reopened");
                assertEquals(2, server.openedConnections());
                client.shutdown(Duration.ofSeconds(5));
            }
        }
    }

    @Test
    void noReconnectAfterACloseRequestedByTheClient() throws Exception {
        try (var server = FakeRconServer.startNew()) {
            final var bus = new EventBus();
            final var opened = new Semaphore(0);
            bus.register(new OpenedListener(opened));
            try (var client = new ReconnectingRustWebSocketClient("127.0.0.1", server.getPort(), "secret", bus)) {
                client.connect();
                assertTrue(opened.tryAcquire(5, TimeUnit.SECONDS), "connection not opened");

                client.closeBlocking();

                Thread.sleep(2500);
                assertEquals(1, server.openedConnections());
            }
        }
    }

    @Test
    void detectsADeadConnectionAndReconnects() throws Exception {
        try (var server = FakeRconServer.startNew()) {
            final var bus = new EventBus();
            final var opened = new Semaphore(0);
            bus.register(new OpenedListener(opened));
            final var closedEvents = new AtomicInteger();
            bus.register(new ClosedCounter(closedEvents));
            try (var client = new ReconnectingRustWebSocketClient("127.0.0.1", server.getPort(), "secret", bus) {
                @Override
                protected Duration connectionLostTimeout() {
                    return Duration.ofSeconds(1);
                }
            }) {
                client.connect();
                assertTrue(opened.tryAcquire(5, TimeUnit.SECONDS), "first connection not opened");

                server.ignorePings();

                assertTrue(opened.tryAcquire(15, TimeUnit.SECONDS), "dead connection not detected and reopened");
                assertTrue(closedEvents.get() >= 1, "close events: " + closedEvents.get());
                client.shutdown(Duration.ofSeconds(5));
            }
        }
    }

    public static final class OpenedListener {
        private final Semaphore opened;

        OpenedListener(Semaphore opened) {
            this.opened = opened;
        }

        @Subscribe
        public void onOpened(WsOpenedEvent event) {
            opened.release();
        }
    }

    public static final class ClosedCounter {
        private final AtomicInteger closed;

        ClosedCounter(AtomicInteger closed) {
            this.closed = closed;
        }

        @Subscribe
        public void onClosed(WsClosedEvent event) {
            closed.incrementAndGet();
        }
    }
}
