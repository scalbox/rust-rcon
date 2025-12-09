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

package com.scalbox.rust.rcon.event.player;

import com.scalbox.rust.rcon.RustRconResponse;
import com.scalbox.rust.rcon.event.BaseRustEventParser;
import com.scalbox.rust.rcon.protocol.util.PlayerName;
import com.scalbox.rust.rcon.protocol.util.SteamId64;
import lombok.NonNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

public class PlayerMiniCopterCrashedEventParser extends BaseRustEventParser<PlayerMiniCopterCrashedEvent> {
    private static final Pattern DETAILS_PATTERN = Pattern.compile("^(.+?)\\[(\\d+)\\]");

    @Override
    public boolean supports(@NonNull RustRconResponse payload) {
        return payload.getMessage().contains("was killed by minicopter.entity (entity)");
    }

    @Override
    protected Function<RustRconResponse, Optional<PlayerMiniCopterCrashedEvent>> eventParser() {
        return rconResponse -> {
            final var message = rconResponse.getMessage();
            final var matcher = DETAILS_PATTERN.matcher(message);

            if (matcher.find()) {
                final var playerName = matcher.group(1);
                final var steamId64 = matcher.group(2);

                final var event = new PlayerMiniCopterCrashedEvent(
                        new SteamId64(steamId64),
                        PlayerName.ofNullable(playerName)
                );

                return Optional.of(event);
            }

            return Optional.empty();
        };
    }

    @Override
    public Class<PlayerMiniCopterCrashedEvent> eventClass() {
        return PlayerMiniCopterCrashedEvent.class;
    }
}
