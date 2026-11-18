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

package io.graversen.rust.rcon.event.player;

import io.graversen.rust.rcon.protocol.util.PlayerName;
import io.graversen.rust.rcon.protocol.util.SteamId64;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class PlayerBannedEvent extends PlayerEvent {
    private final @NonNull PlayerName playerName;
    private final @NonNull String ipAddress;
    private final @NonNull String reason;
    private final long expiry;

    public PlayerBannedEvent(
            @NonNull SteamId64 steamId,
            @NonNull PlayerName playerName,
            @NonNull String ipAddress,
            @NonNull String reason,
            long expiry
    ) {
        super(steamId);
        this.playerName = playerName;
        this.ipAddress = ipAddress;
        this.reason = reason;
        this.expiry = expiry;
    }
}
