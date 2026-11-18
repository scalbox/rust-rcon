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

package io.graversen.rust.rcon.event.player;

import io.graversen.rust.rcon.TestRustRconResponse;
import io.graversen.rust.rcon.protocol.util.ChatChannels;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerChatEventParserTest {
    private final PlayerChatEventParser playerChatEventParser = new PlayerChatEventParser();

    @ParameterizedTest
    @CsvFileSource(resources = "PlayerChatEvents_Verification.txt", numLinesToSkip = 1, delimiter = ';')
    void parseEvent_verification(String payload, String steamId, String playerName, String message) {
        final var event = playerChatEventParser.parseEvent(new TestRustRconResponse(payload));
        assertTrue(event.isPresent());
        assertEquals(steamId, event.get().getSteamId().get());
        assertEquals(playerName, event.get().getPlayerName().get());
        assertEquals(message, event.get().getMessage());
        assertEquals(ChatChannels.DEFAULT, event.get().getChatChannel());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "PlayerChatEvents_Verification.txt", numLinesToSkip = 1, delimiter = ';')
    void supports_verification(String payload) {
        final var supports = playerChatEventParser.supports(new TestRustRconResponse(payload));
        assertTrue(supports);
    }

    @Test
    void parseEvent_globalChatWithoutSteamId() {
        final var event = playerChatEventParser.parseEvent(new TestRustRconResponse("[Global] Doctor Delete : Hi"));
        assertTrue(event.isPresent());
        assertNull(event.get().getSteamId());
        assertEquals("Doctor Delete", event.get().getPlayerName().get());
        assertEquals("Hi", event.get().getMessage());
        assertEquals(ChatChannels.DEFAULT, event.get().getChatChannel());
    }

    @Test
    void supports_globalChatWithoutSteamId() {
        final var supports = playerChatEventParser.supports(new TestRustRconResponse("[Global] Doctor Delete : Hi"));
        assertTrue(supports);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "PlayerChatEvents_Team_Verification.txt", numLinesToSkip = 1, delimiter = ';')
    void parseEvent_team_verification(String payload, String steamId, String playerName, String message) {
        final var event = playerChatEventParser.parseEvent(new TestRustRconResponse(payload));
        assertTrue(event.isPresent());
        assertEquals(steamId, event.get().getSteamId().get());
        assertEquals(playerName, event.get().getPlayerName().get());
        assertEquals(message, event.get().getMessage());
        assertEquals(ChatChannels.TEAM, event.get().getChatChannel());
    }

    @Test
    void parseEvent_teamChatWithoutSteamId() {
        final var event = playerChatEventParser.parseEvent(new TestRustRconResponse("[Team] Doctor Delete : Hi"));
        assertTrue(event.isPresent());
        assertNull(event.get().getSteamId());
        assertEquals("Doctor Delete", event.get().getPlayerName().get());
        assertEquals("Hi", event.get().getMessage());
        assertEquals(ChatChannels.TEAM, event.get().getChatChannel());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "PlayerChatEvents_Team_Verification.txt", numLinesToSkip = 1, delimiter = ';')
    void supports_team_verification(String payload) {
        final var supports = playerChatEventParser.supports(new TestRustRconResponse(payload));
        assertTrue(supports);
    }

    @Test
    void supports_teamChatWithoutSteamId() {
        final var supports = playerChatEventParser.supports(new TestRustRconResponse("[Team] Doctor Delete : Hi"));
        assertTrue(supports);
    }
}
