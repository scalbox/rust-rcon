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

package com.scalbox.rust.rcon.protocol;

import com.scalbox.rust.rcon.RustRconResponse;
import com.scalbox.rust.rcon.protocol.util.SteamId64;
import lombok.NonNull;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public interface EventCodec {
    CompletableFuture<RustRconResponse> callAirDrop();

    CompletableFuture<RustRconResponse> callPatrolHelicopter();

    CompletableFuture<RustRconResponse> strafePatrolHelicopter(@NonNull SteamId64 steamId64);

    CompletableFuture<RustRconResponse> patrolHelicopterLifetime(@NonNull Duration lifetime);
}
