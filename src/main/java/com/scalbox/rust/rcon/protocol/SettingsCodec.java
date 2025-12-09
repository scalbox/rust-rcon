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
import com.scalbox.rust.rcon.protocol.util.Animals;
import com.scalbox.rust.rcon.protocol.util.Vehicles;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public interface SettingsCodec {
    CompletableFuture<RustRconResponse> decayScale(@NonNull BigDecimal amount);

    CompletableFuture<RustRconResponse> decayUpkeepEnabled(@NonNull Boolean enabled);

    CompletableFuture<RustRconResponse> stabilityEnabled(@NonNull Boolean enabled);

    CompletableFuture<RustRconResponse> radiationEnabled(@NonNull Boolean enabled);

    CompletableFuture<RustRconResponse> globalChatEnabled(@NonNull Boolean enabled);

    CompletableFuture<RustRconResponse> animalPopulation(@NonNull Animals animal, BigDecimal amount);

    CompletableFuture<RustRconResponse> vehiclePopulation(@NonNull Vehicles vehicle, BigDecimal amount);
}
