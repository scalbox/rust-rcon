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

/**
 * High-level access to a single Rust server over RCON: connection lifecycle, management APIs, periodic tasks,
 * snapshots of players, teams and diagnostics, and the events parsed from the server output.
 */
public interface RustRconService extends EventEmitter {
    /**
     * Returns the codec used to build and send the raw RCON commands.
     *
     * @return the codec
     */
    Codec codec();

    /**
     * Starts connecting to the server and schedules the internal tasks, without waiting for the connection to be
     * established.
     */
    void start();

    /**
     * Stops the scheduled tasks and closes the connection to the server.
     */
    void stop();

    /**
     * Requests the server information.
     *
     * @return a future completed with the server information, or exceptionally if the request fails
     */
    CompletableFuture<ServerInfoDTO> serverInfo();

    /**
     * Returns the API for managing Oxide/uMod plugins and permissions.
     *
     * @return the Oxide management API
     */
    OxideManagement oxideManagement();

    /**
     * Returns the API for managing the uMod bridge plugin.
     *
     * @return the uMod bridge management API
     */
    UmodBridgeManagement umodBridgeManagement();

    /**
     * Returns the API for broadcasting messages to the players.
     *
     * @return the broadcast management API
     */
    BroadcastManagement broadcastManagement();

    /**
     * Returns the API for managing players and teams.
     *
     * @return the player management API
     */
    PlayerManagement playerManagement();

    /**
     * Schedules a task to run repeatedly, with a fixed delay between the end of one run and the start of the next.
     *
     * @param task         the task to run
     * @param fixedDelay   delay between two consecutive runs
     * @param initialDelay delay before the first run; {@code null} means no delay
     */
    void schedule(@NonNull RconTask task, @NonNull Duration fixedDelay, @Nullable Duration initialDelay);

    /**
     * Returns the latest diagnostics derived from the server information.
     *
     * @return the diagnostics, or empty until the first server information has been received
     */
    Optional<RustDiagnostics> diagnostics();

    /**
     * Returns the players currently connected.
     *
     * @return an immutable snapshot of the players
     */
    List<FullRustPlayer> players();

    /**
     * Returns the teams on the server.
     *
     * @return an immutable snapshot of the teams
     */
    List<RustTeam> teams();

    /**
     * Returns the events that the configured event source can emit.
     *
     * @return the event capabilities
     */
    RustEventCapabilities eventCapabilities();
}
