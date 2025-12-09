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

package com.scalbox.rust.rcon;

import com.scalbox.rust.rcon.protocol.util.PlayerName;
import com.scalbox.rust.rcon.protocol.util.SteamId64;
import com.scalbox.rust.rcon.util.CommonUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;

@Getter
@EqualsAndHashCode(callSuper = true)
public class FullRustPlayer extends RustPlayer {
    private final @NonNull String ping;
    private final @NonNull String ipAddress;
    private final @NonNull Duration connectedDuration;
    private final @NonNull BigDecimal health;

    public FullRustPlayer(
            @NonNull SteamId64 steamId,
            @NonNull PlayerName playerName,
            @NonNull String ping,
            @NonNull String ipAddress,
            @NonNull Duration connectedDuration,
            @NonNull BigDecimal health
    ) {
        super(steamId, playerName);
        this.ping = ping;
        this.ipAddress = ipAddress;
        this.connectedDuration = connectedDuration;
        this.health = health;
    }

    public ZonedDateTime connectedAt() {
        return CommonUtils.now().minus(connectedDuration);
    }
}
