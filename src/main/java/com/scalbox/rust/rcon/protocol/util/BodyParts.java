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

public enum BodyParts {
    HEAD,
    BODY,
    LEG,
    ARM,
    HAND,
    STOMACH,
    CHEST,
    UNKNOWN;

    public static BodyParts parse(String bodyPart) {
        if (Objects.isNull(bodyPart)) {
            return UNKNOWN;
        }

        try {
            return BodyParts.valueOf(bodyPart.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
