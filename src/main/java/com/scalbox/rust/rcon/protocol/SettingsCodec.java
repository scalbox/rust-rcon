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
