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

package com.scalbox.rust.rcon.event.server;

import com.scalbox.rust.rcon.RustServer;
import com.scalbox.rust.rcon.protocol.util.PlayerName;
import com.scalbox.rust.rcon.protocol.util.SteamId64;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class EasyAntiCheatEvent extends ServerEvent {
    private final @NonNull SteamId64 steamId;
    private final @NonNull PlayerName playerName;
    private final @NonNull String reason;

    public EasyAntiCheatEvent(
            @NonNull RustServer server,
            @NonNull SteamId64 steamId,
            @NonNull PlayerName playerName,
            @NonNull String reason
    ) {
        super(server);
        this.steamId = steamId;
        this.playerName = playerName;
        this.reason = reason;
    }
}
