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

package io.graversen.rust.rcon.event.server;

import io.graversen.rust.rcon.event.RustEvent;
import io.graversen.rust.rcon.protocol.util.PlayerName;
import io.graversen.rust.rcon.protocol.util.SteamId64;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
public class ExplosiveUseEvent extends RustEvent {
    private final @NonNull SteamId64 steamId;
    private final @NonNull PlayerName playerName;
    private final @NonNull ExplosiveUseTypes explosiveUseType;
    private final @NonNull String weapon;
    private final @NonNull String entity;
    private final @NonNull String position;

    public ExplosiveUseEvent(
            @NonNull SteamId64 steamId,
            @NonNull PlayerName playerName,
            @NonNull ExplosiveUseTypes explosiveUseType,
            @NonNull String weapon,
            @NonNull String entity,
            @NonNull String position
    ) {
        this.steamId = steamId;
        this.playerName = playerName;
        this.explosiveUseType = explosiveUseType;
        this.weapon = weapon;
        this.entity = entity;
        this.position = position;
    }
}
