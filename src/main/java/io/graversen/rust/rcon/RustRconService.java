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

package io.graversen.rust.rcon;

import io.graversen.rust.rcon.protocol.Codec;
import io.graversen.rust.rcon.protocol.broadcast.BroadcastManagement;
import io.graversen.rust.rcon.protocol.dto.ServerInfoDTO;
import io.graversen.rust.rcon.protocol.oxide.OxideManagement;
import io.graversen.rust.rcon.protocol.player.PlayerManagement;
import io.graversen.rust.rcon.event.RustEventCapabilities;
import io.graversen.rust.rcon.event.umod.UmodBridgeManagement;
import io.graversen.rust.rcon.tasks.RconTask;
import io.graversen.rust.rcon.util.EventEmitter;
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

    UmodBridgeManagement umodBridgeManagement();

    BroadcastManagement broadcastManagement();

    PlayerManagement playerManagement();

    void schedule(@NonNull RconTask task, @NonNull Duration fixedDelay, @Nullable Duration initialDelay);

    Optional<RustDiagnostics> diagnostics();

    List<FullRustPlayer> players();

    List<RustTeam> teams();

    RustEventCapabilities eventCapabilities();
}
