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

package io.graversen.rust.rcon.event.player;

import io.graversen.rust.rcon.RustRconResponse;
import io.graversen.rust.rcon.event.BaseRustEventParser;
import io.graversen.rust.rcon.protocol.util.PlayerName;
import io.graversen.rust.rcon.protocol.util.SteamId64;
import lombok.NonNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Parses the console line written when a player dies in a minicopter crash, in the form
 * {@code PlayerName[SteamID64] was killed by minicopter.entity (entity) at (x, y, z)}.
 */
public class PlayerMiniCopterCrashedEventParser extends BaseRustEventParser<PlayerMiniCopterCrashedEvent> {
    private static final Pattern DETAILS_PATTERN = Pattern.compile("^(.+)\\[(\\d{17})\\] was killed by minicopter\\.entity \\(entity\\)");

    /**
     * Tells whether the message is a minicopter crash line. Chat messages are never supported, even if their text
     * looks like a crash line.
     *
     * @param payload the RCON message
     * @return {@code true} if the message is a minicopter crash line
     */
    @Override
    public boolean supports(@NonNull RustRconResponse payload) {
        return !PlayerChatEventParser.isChat(payload) && DETAILS_PATTERN.matcher(payload.getMessage()).find();
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
