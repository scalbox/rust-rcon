/*
 * Copyright (c) scalbox.
 *
 * Distributed under the terms of the Apache License, Version 2.0.
 * For more details, see the LICENSE file included in this repository.
 */

package io.graversen.rust.rcon.event.player;

import io.graversen.rust.rcon.TestRustRconResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerDisconnectedEventParserTest {
    private static final String DISCONNECTED_LINE = "93.184.216.34:51234/76561197979952036/Doctor Delete disconnecting: Disconnected";

    private final PlayerDisconnectedEventParser parser = new PlayerDisconnectedEventParser();

    @Test
    void parsesDisconnectionLine() {
        final var payload = new TestRustRconResponse(DISCONNECTED_LINE);

        assertTrue(parser.supports(payload));
        final var event = parser.parseEvent(payload);
        assertTrue(event.isPresent());
        assertEquals("76561197979952036", event.get().getSteamId().get());
        assertEquals("Disconnected", event.get().getReason());
    }

    @Test
    void ignoresChatLineImitatingADisconnection() {
        final var payload = new TestRustRconResponse("[CHAT] Prankster[76561197979952037] : x/76561197979952036/ disconnecting: hacked");

        assertFalse(parser.supports(payload));
    }

    @Test
    void ignoresChatMessageType() {
        final var payload = new TestRustRconResponse(DISCONNECTED_LINE, PlayerChatEventParser.CHAT_MESSAGE_TYPE);

        assertFalse(parser.supports(payload));
    }

    @Test
    void ignoresUnrelatedLinesContainingTheKeyword() {
        assertFalse(parser.supports(new TestRustRconResponse("Kicking someone disconnecting: now")));
    }
}
