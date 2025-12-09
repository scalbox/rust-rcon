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
import com.scalbox.rust.rcon.protocol.util.Animals;
import com.scalbox.rust.rcon.protocol.util.Vehicles;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.scalbox.rust.rcon.protocol.RustProtocolTemplates.Placeholders.*;
import static com.scalbox.rust.rcon.protocol.RustProtocolTemplates.SettingsProtocol.*;

public class DefaultSettingsCodec extends DefaultRustCodec implements SettingsCodec {
    public DefaultSettingsCodec(@NonNull RustRconRouter rustRconRouter) {
        super(rustRconRouter);
    }

    @Override
    public CompletableFuture<RustRconResponse> decayScale(@NonNull BigDecimal amount) {
        final var rconMessage = compile(
                DECAY_SCALE,
                Map.of(
                        stripped(AMOUNT), amount.toString()
                )
        );
        return send(rconMessage);
    }

    @Override
    public CompletableFuture<RustRconResponse> decayUpkeepEnabled(@NonNull Boolean enabled) {
        final var rconMessage = compile(
                DECAY_UPKEEP_ENABLED,
                Map.of(
                        stripped(ENABLED), String.valueOf(enabled)
                )
        );
        return send(rconMessage);
    }

    @Override
    public CompletableFuture<RustRconResponse> stabilityEnabled(@NonNull Boolean enabled) {
        final var rconMessage = compile(
                STABILITY_ENABLED,
                Map.of(
                        stripped(ENABLED), String.valueOf(enabled)
                )
        );
        return send(rconMessage);
    }

    @Override
    public CompletableFuture<RustRconResponse> radiationEnabled(@NonNull Boolean enabled) {
        final var rconMessage = compile(
                RADIATION_ENABLED,
                Map.of(
                        stripped(ENABLED), String.valueOf(enabled)
                )
        );
        return send(rconMessage);
    }

    @Override
    public CompletableFuture<RustRconResponse> globalChatEnabled(@NonNull Boolean enabled) {
        final var rconMessage = compile(
                GLOBAL_CHAT_ENABLED,
                Map.of(
                        stripped(ENABLED), String.valueOf(enabled)
                )
        );
        return send(rconMessage);
    }

    @Override
    public CompletableFuture<RustRconResponse> animalPopulation(@NonNull Animals animal, BigDecimal amount) {
        final var rconMessage = compile(
                ANIMAL_POPULATION,
                Map.of(
                        stripped(ANIMAL), animal.name().toLowerCase(),
                        stripped(AMOUNT), amount.toString()
                )
        );
        return send(rconMessage);
    }

    @Override
    public CompletableFuture<RustRconResponse> vehiclePopulation(@NonNull Vehicles vehicle, BigDecimal amount) {
        final var rconMessage = compile(
                VEHICLE_POPULATION,
                Map.of(
                        stripped(ANIMAL), vehicle.name().toLowerCase(),
                        stripped(AMOUNT), amount.toString()
                )
        );
        return send(rconMessage);
    }
}
