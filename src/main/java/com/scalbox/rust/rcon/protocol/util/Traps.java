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

package com.scalbox.rust.rcon.protocol.util;

import java.util.Objects;

public enum Traps {
    GUN_TRAP,
    AUTO_TURRET,
    FLAME_TURRET,
    LANDMINE,
    UNKNOWN;

    public static Traps parse(String string) {
        if (Objects.isNull(string)) {
            return UNKNOWN;
        }

        try {
            return Traps.valueOf(string.toUpperCase().replaceAll("\\s", "_"));
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
