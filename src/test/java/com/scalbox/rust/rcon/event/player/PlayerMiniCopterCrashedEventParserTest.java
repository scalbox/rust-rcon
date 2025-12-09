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

package com.scalbox.rust.rcon.event.player;

import com.scalbox.rust.rcon.TestRustRconResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerMiniCopterCrashedEventParserTest {
    private final PlayerMiniCopterCrashedEventParser playerMiniCopterCrashedEventParser = new PlayerMiniCopterCrashedEventParser();

    @ParameterizedTest
    @CsvFileSource(resources = "PlayerMiniCopterCrashedEvents_Verification.txt", numLinesToSkip = 1, delimiter = ';')
    void parseEvent_verification(String payload, String steamId, String playerName) {
        final var event = playerMiniCopterCrashedEventParser.parseEvent(new TestRustRconResponse(payload));
        assertTrue(event.isPresent());
        assertEquals(steamId, event.get().getSteamId().get());
        assertEquals(playerName, event.get().getPlayerName().get());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "PlayerMiniCopterCrashedEvents_Verification.txt", numLinesToSkip = 1, delimiter = ';')
    void supports_verification(String payload) {
        final var supports = playerMiniCopterCrashedEventParser.supports(new TestRustRconResponse(payload));
        assertTrue(supports);
    }
}