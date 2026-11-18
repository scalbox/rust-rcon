/*
 * Copyright (c) scalbox.
 *
 * Distributed under the terms of the Apache License, Version 2.0.
 * For more details, see the LICENSE file included in this repository.
 */

package io.graversen.rust.rcon.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommonUtilsTest {
    @Test
    void substituteReplacesVariables() {
        assertEquals("kick 1 bye", CommonUtils.substitute("kick ${id} ${reason}", Map.of("id", 1, "reason", "bye")));
    }

    @Test
    void substituteDoesNotResolveVariablesInsideValues() {
        final var result = CommonUtils.substitute("${name} ${reason}", Map.of("name", "${reason}", "reason", "cheating"));

        assertEquals("${reason} cheating", result);
    }

    @Test
    void substituteDoesNotLoopOnSelfReferencingValues() {
        final var result = CommonUtils.substitute("[${message}]", Map.of("message", "x ${message}"));

        assertEquals("[x ${message}]", result);
    }

    @Test
    void substituteLeavesUnknownVariablesUnchanged() {
        assertEquals("${missing}", CommonUtils.substitute("${missing}", Map.of()));
    }
}
