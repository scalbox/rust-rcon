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
