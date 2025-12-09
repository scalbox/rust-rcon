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

class PlayerConnectedEventParserTest {
    private final PlayerConnectedEventParser playerConnectedEventParser = new PlayerConnectedEventParser();

    @ParameterizedTest
    @CsvFileSource(resources = "PlayerConnectedEvents_Valid.txt")
    void supports_valid(String payload) {
        final var supports = playerConnectedEventParser.supports(new TestRustRconResponse(payload));
        assertTrue(supports);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "PlayerConnectedEvents_Valid.txt")
    void parseEvent_valid(String payload) {
        final var event = playerConnectedEventParser.parseEvent(new TestRustRconResponse(payload));
        assertTrue(event.isPresent());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "PlayerConnectedEvents_Verification.txt", numLinesToSkip = 1)
    void parseEvent_verification(String payload, String ipAddress, String steamId, String playerName, String operatingSystem) {
        final var event = playerConnectedEventParser.parseEvent(new TestRustRconResponse(payload));
        assertTrue(event.isPresent());
        assertEquals(ipAddress, event.get().getIpAddress());
        assertEquals(steamId, event.get().getSteamId().getValue());
        assertEquals(playerName, event.get().getPlayerName().getPlayerName());
        assertEquals(operatingSystem, event.get().getOperatingSystem().toString());
    }
}