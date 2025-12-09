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

import com.google.common.eventbus.Subscribe;
import com.scalbox.rust.rcon.event.server.RustPlayersEvent;
import com.scalbox.rust.rcon.protocol.dto.RustPlayerDTO;
import com.scalbox.rust.rcon.protocol.util.PlayerName;
import com.scalbox.rust.rcon.protocol.util.SteamId64;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class RustPlayerEventListener {
    private final @NonNull Consumer<List<FullRustPlayer>> rustPlayersConsumer;

    @Subscribe
    public void onRustPlayers(RustPlayersEvent rustPlayersEvent) {
        final var rustPlayers = mapRustPlayers().apply(rustPlayersEvent);
        rustPlayersConsumer.accept(rustPlayers);
    }

    Function<RustPlayersEvent, List<FullRustPlayer>> mapRustPlayers() {
        return rustPlayersEvent -> {
            final var rustPlayers = rustPlayersEvent.getRustPlayers();
            return rustPlayers.stream()
                    .map(mapRustPlayer())
                    .toList();
        };
    }

    Function<RustPlayerDTO, FullRustPlayer> mapRustPlayer() {
        return rustPlayerDTO -> new FullRustPlayer(
                SteamId64.parseOrFail(rustPlayerDTO.getSteamId()),
                new PlayerName(rustPlayerDTO.getPlayerName()),
                rustPlayerDTO.getPing(),
                rustPlayerDTO.getIpAddress().split(":")[0],
                Duration.ofSeconds(rustPlayerDTO.getConnectedSeconds()),
                BigDecimal.valueOf(rustPlayerDTO.getHealth())
        );
    }
}
