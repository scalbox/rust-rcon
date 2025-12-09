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

package com.scalbox.rust.rcon.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RustUtilsTest {
    @Test
    void parseDateTime() {
        final var parsedDateTime = RustUtils.parseRustDateTime("09/07/2023 17:52:24");
        assertNotNull(parsedDateTime);
        assertEquals(CommonUtils.utc(), parsedDateTime.getZone());
        assertEquals("2023-09-07T17:52:24Z[UTC]", parsedDateTime.toString());
    }
}