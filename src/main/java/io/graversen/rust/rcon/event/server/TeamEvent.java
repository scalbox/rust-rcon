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

import org.jspecify.annotations.Nullable;
import java.util.Set;

@Getter
@ToString
public class TeamEvent extends RustEvent {
    private final long teamId;
    private final long leaderSteamId;
    private final @NonNull TeamEventTypes teamEventType;
    private final @Nullable SteamId64 actorSteamId;
    private final @NonNull PlayerName actorName;
    private final @Nullable SteamId64 targetSteamId;
    private final @NonNull Set<String> members;

    public TeamEvent(
            long teamId,
            long leaderSteamId,
            @NonNull TeamEventTypes teamEventType,
            @Nullable SteamId64 actorSteamId,
            @NonNull PlayerName actorName,
            @Nullable SteamId64 targetSteamId,
            @NonNull Set<String> members
    ) {
        this.teamId = teamId;
        this.leaderSteamId = leaderSteamId;
        this.teamEventType = teamEventType;
        this.actorSteamId = actorSteamId;
        this.actorName = actorName;
        this.targetSteamId = targetSteamId;
        this.members = members;
    }
}
