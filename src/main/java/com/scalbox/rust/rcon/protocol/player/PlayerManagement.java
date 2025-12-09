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

package com.scalbox.rust.rcon.protocol.player;

import com.scalbox.rust.rcon.FullRustPlayer;
import com.scalbox.rust.rcon.RustPlayer;
import com.scalbox.rust.rcon.RustTeam;
import com.scalbox.rust.rcon.protocol.util.SteamId64;
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
