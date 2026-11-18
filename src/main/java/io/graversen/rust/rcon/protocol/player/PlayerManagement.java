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

package io.graversen.rust.rcon.protocol.player;

import io.graversen.rust.rcon.FullRustPlayer;
import io.graversen.rust.rcon.RustPlayer;
import io.graversen.rust.rcon.RustTeam;
import io.graversen.rust.rcon.protocol.util.SteamId64;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface PlayerManagement {
    CompletableFuture<List<FullRustPlayer>> players();

    CompletableFuture<List<RustPlayer>> sleepingPlayers();

    CompletableFuture<Optional<RustTeam>> team(@NonNull SteamId64 steamId64);

    CompletableFuture<List<RustTeam>> getTeams();
}
