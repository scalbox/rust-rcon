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

import com.scalbox.rust.rcon.protocol.util.OperatingSystems;
import com.scalbox.rust.rcon.protocol.util.PlayerName;
import com.scalbox.rust.rcon.protocol.util.SteamId64;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class PlayerConnectedEvent extends PlayerEvent {
    private final @NonNull PlayerName playerName;
    private final @NonNull OperatingSystems operatingSystem;
    private final @NonNull String ipAddress;

    PlayerConnectedEvent(
            @NonNull SteamId64 steamId,
            @NonNull PlayerName playerName,
            @NonNull OperatingSystems operatingSystem,
            @NonNull String ipAddress
    ) {
        super(steamId);
        this.playerName = playerName;
        this.operatingSystem = operatingSystem;
        this.ipAddress = ipAddress;
    }
}
