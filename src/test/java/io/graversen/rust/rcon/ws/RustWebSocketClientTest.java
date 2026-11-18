/*
 * Copyright (c) scalbox.
 *
 * Distributed under the terms of the Apache License, Version 2.0.
 * For more details, see the LICENSE file included in this repository.
 */

package io.graversen.rust.rcon.ws;

import com.google.common.eventbus.EventBus;
import io.graversen.rust.rcon.FakeRconServer;
import org.junit.jupiter.api.Test;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RustWebSocketClientTest {
    private static final String SPECIAL_PASSWORD = "p#ss w/rd%?é";

    @Test
    void plainPasswordIsUsedAsTheRequestPath() {
        try (var client = new RustWebSocketClient("127.0.0.1", 28016, "secret", new EventBus())) {
            assertEquals("/secret", client.getURI().getRawPath());
        }
    }

    @Test
    void specialCharactersInThePasswordArePercentEncoded() {
        try (var client = new RustWebSocketClient("127.0.0.1", 28016, SPECIAL_PASSWORD, new EventBus())) {
            final var uri = client.getURI();

            assertEquals("/p%23ss%20w%2Frd%25%3F%C3%A9", uri.getRawPath());
            assertEquals("127.0.0.1", uri.getHost());
            assertEquals(28016, uri.getPort());
            assertTrue(uri.getRawQuery() == null && uri.getRawFragment() == null, uri.toString());
        }
    }

    @Test
    void charactersAllowedInAPathAreKeptAsTheyAre() {
        final var password = "Ab9-._~!$&'()*+,;=:@";
        try (var client = new RustWebSocketClient("127.0.0.1", 28016, password, new EventBus())) {
            assertEquals("/" + password, client.getURI().getRawPath());
        }
    }

    @Test
    void redactedUriNeverContainsThePassword() {
        try (var client = new RustWebSocketClient("127.0.0.1", 28016, SPECIAL_PASSWORD, new EventBus())) {
            assertEquals("ws://127.0.0.1:28016/*", client.getServerUri());
            assertFalse(client.toString().contains("p%23ss"), client.toString());
        }
    }

    @Test
    void serverReceivesTheWholeEncodedPassword() throws Exception {
        try (var server = FakeRconServer.startNew();
             var client = new RustWebSocketClient("127.0.0.1", server.getPort(), SPECIAL_PASSWORD, new EventBus())) {
            assertTrue(client.connectBlocking(5, TimeUnit.SECONDS), "connection not opened");

            final var requestPath = server.lastResourceDescriptor();
            assertNotNull(requestPath);
            assertEquals("/" + SPECIAL_PASSWORD, URLDecoder.decode(requestPath.replace("+", "%2B"), StandardCharsets.UTF_8));
            client.shutdown(Duration.ofSeconds(5));
        }
    }
}
