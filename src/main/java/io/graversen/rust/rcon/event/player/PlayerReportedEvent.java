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

import io.graversen.rust.rcon.event.RustEvent;
import io.graversen.rust.rcon.protocol.util.PlayerName;
import io.graversen.rust.rcon.protocol.util.SteamId64;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import org.jspecify.annotations.Nullable;

@Getter
@ToString
public class PlayerReportedEvent extends RustEvent {
    private final @NonNull SteamId64 reporterSteamId;
    private final @NonNull PlayerName reporterName;
    private final @Nullable SteamId64 targetSteamId;
    private final @NonNull PlayerName targetName;
    private final @NonNull String subject;
    private final @NonNull String message;
    private final @NonNull String reportType;

    public PlayerReportedEvent(
            @NonNull SteamId64 reporterSteamId,
            @NonNull PlayerName reporterName,
            @Nullable SteamId64 targetSteamId,
            @NonNull PlayerName targetName,
            @NonNull String subject,
            @NonNull String message,
            @NonNull String reportType
    ) {
        this.reporterSteamId = reporterSteamId;
        this.reporterName = reporterName;
        this.targetSteamId = targetSteamId;
        this.targetName = targetName;
        this.subject = subject;
        this.message = message;
        this.reportType = reportType;
    }
}
