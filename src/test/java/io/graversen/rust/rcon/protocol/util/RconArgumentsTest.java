/*
 * Copyright (c) scalbox.
 *
 * Distributed under the terms of the Apache License, Version 2.0.
 * For more details, see the LICENSE file included in this repository.
 */

package io.graversen.rust.rcon.protocol.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RconArgumentsTest {
    @Test
    void escapeLeavesPlainTextUnchanged() {
        assertEquals("Doctor Delete", RconArguments.escape("Doctor Delete"));
    }

    @Test
    void escapeEscapesQuotesAndBackslashes() {
        assertEquals("a\\\"b\\\\c", RconArguments.escape("a\"b\\c"));
    }

    @Test
    void escapeHandlesBackslashBeforeQuote() {
        assertEquals("end\\\\\\\"", RconArguments.escape("end\\\""));
    }

    @Test
    void quoteWrapsTheEscapedValue() {
        assertEquals("\"say \\\"hi\\\"\"", RconArguments.quote("say \"hi\""));
    }
}
