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

import com.scalbox.rust.rcon.protocol.Codec;
import com.scalbox.rust.rcon.protocol.dto.ServerInfoDTO;
import com.scalbox.rust.rcon.protocol.oxide.OxideManagement;
import com.scalbox.rust.rcon.protocol.player.PlayerManagement;
import com.scalbox.rust.rcon.tasks.RconTask;
import com.scalbox.rust.rcon.util.EventEmitter;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface RustRconService extends EventEmitter {
    Codec codec();

    void start();

    void stop();

    CompletableFuture<ServerInfoDTO> serverInfo();

    OxideManagement oxideManagement();

    PlayerManagement playerManagement();

    void schedule(@NonNull RconTask task, @NonNull Duration fixedDelay, @Nullable Duration initialDelay);

    Optional<RustDiagnostics> diagnostics();

    List<FullRustPlayer> players();

    List<RustTeam> teams();
}
