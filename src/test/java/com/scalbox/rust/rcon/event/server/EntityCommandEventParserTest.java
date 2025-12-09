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

package com.scalbox.rust.rcon.event.server;

import com.scalbox.rust.rcon.TestRustRconResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.*;

class EntityCommandEventParserTest {
    private final EntityCommandEventParser entityCommandEventParser = new EntityCommandEventParser();

    @ParameterizedTest
    @CsvFileSource(resources = "EntityCommandEvents_Verification.txt", numLinesToSkip = 1)
    void parseEvent_verification(String payload, String command) {
        final var event = entityCommandEventParser.parseEvent(new TestRustRconResponse(payload));
        assertTrue(event.isPresent());
        assertEquals(command, event.get().getCommand());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "EntityCommandEvents_Verification.txt", numLinesToSkip = 1)
    void supports_verification(String payload) {
        final var supports = entityCommandEventParser.supports(new TestRustRconResponse(payload));
        assertTrue(supports);
    }
}