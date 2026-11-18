/*
 * Copyright (c) scalbox.
 *
 * Distributed under the terms of the Apache License, Version 2.0.
 * For more details, see the LICENSE file included in this repository.
 */

package io.graversen.rust.rcon.protocol.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Helpers for inserting values into RCON console commands as double-quoted arguments.
 */
@UtilityClass
public class RconArguments {
    /**
     * Escapes a value so that it can be placed between double quotes in a console command: every backslash is
     * doubled and every double quote is preceded by a backslash. The quotes themselves are not added.
     *
     * @param value the value to escape
     * @return the escaped value
     */
    public static String escape(@NonNull String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    /**
     * Escapes a value as described in {@link #escape(String)} and wraps it in double quotes.
     *
     * @param value the value to quote
     * @return the quoted value, ready to be used as a single command argument
     */
    public static String quote(@NonNull String value) {
        return "\"" + escape(value) + "\"";
    }
}
