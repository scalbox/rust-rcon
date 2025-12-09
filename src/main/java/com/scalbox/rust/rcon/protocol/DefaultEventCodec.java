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
import com.scalbox.rust.rcon.RustRconRouter;
import com.scalbox.rust.rcon.protocol.util.SteamId64;
import lombok.NonNull;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.scalbox.rust.rcon.protocol.RustProtocolTemplates.EventProtocol.*;
import static com.scalbox.rust.rcon.protocol.RustProtocolTemplates.Placeholders.*;

public class DefaultEventCodec extends DefaultRustCodec implements EventCodec {
    public DefaultEventCodec(@NonNull RustRconRouter rustRconRouter) {
        super(rustRconRouter);
    }

    @Override
    public CompletableFuture<RustRconResponse> callAirDrop() {
        final var rconMessage = compile(CALL_AIR_DROP);
        return send(rconMessage);
    }

    @Override
    public CompletableFuture<RustRconResponse> callPatrolHelicopter() {
        final var rconMessage = compile(CALL_PATROL_HELICOPTER);
        return send(rconMessage);
    }

    @Override
    public CompletableFuture<RustRconResponse> strafePatrolHelicopter(@NonNull SteamId64 steamId64) {
        final var rconMessage = compile(
                STRAFE_PATROL_HELICOPTER,
                Map.of(
                        stripped(STEAM_ID_64), steamId64.get()
                )
        );
        return send(rconMessage);
    }

    @Override
    public CompletableFuture<RustRconResponse> patrolHelicopterLifetime(@NonNull Duration lifetime) {
        final var rconMessage = compile(
                PATROL_HELICOPTER_LIFETIME,
                Map.of(
                        stripped(MINUTES), String.valueOf(lifetime.toMinutes())
                )
        );
        return send(rconMessage);
    }
}
