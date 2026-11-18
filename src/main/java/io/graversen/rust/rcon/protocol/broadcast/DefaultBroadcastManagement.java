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

package io.graversen.rust.rcon.protocol.broadcast;

import io.graversen.rust.rcon.RustRconResponse;
import io.graversen.rust.rcon.protocol.Codec;
import io.graversen.rust.rcon.protocol.util.RconArguments;
import io.graversen.rust.rcon.protocol.util.SteamId64;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class DefaultBroadcastManagement implements BroadcastManagement {
    private final @NonNull Codec codec;

    @Override
    public CompletableFuture<RustRconResponse> broadcast(@NonNull String message, @NonNull SteamId64 speakerSteamId) {
        final var rconMessage = codec.raw("broadcast %s %s".formatted(RconArguments.quote(message), speakerSteamId.get()));
        return codec.send(rconMessage);
    }

    @Override
    public CompletableFuture<RustRconResponse> whisper(@NonNull String message, @NonNull SteamId64 speakerSteamId, @NonNull SteamId64 targetSteamId) {
        final var rconMessage = codec.raw("whisper %s %s %s".formatted(RconArguments.quote(message), speakerSteamId.get(), targetSteamId.get()));
        return codec.send(rconMessage);
    }
}
