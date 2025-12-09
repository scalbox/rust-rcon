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

public enum CombatTypes {
    PVP,
    PVE,
    SUICIDE,
    TRAP,
    UNKNOWN;

    public static CombatTypes resolve(String killerEntityType, String victimEntityType) {
        if (Objects.isNull(killerEntityType) || Objects.isNull(victimEntityType)) {
            return CombatTypes.UNKNOWN;
        }

        if (EntityTypes.isPlayer(killerEntityType) && EntityTypes.isPlayer(victimEntityType)) {
            return CombatTypes.PVP;
        } else if ((EntityTypes.isPlayer(killerEntityType) && EntityTypes.isNonPlayerCharacter(victimEntityType))
                || (EntityTypes.isNonPlayerCharacter(killerEntityType) && EntityTypes.isPlayer(victimEntityType))) {
            return CombatTypes.PVE;
        } else if (EntityTypes.isTrap(killerEntityType)) {
            return CombatTypes.TRAP;
        } else {
            return CombatTypes.UNKNOWN;
        }
    }
}
