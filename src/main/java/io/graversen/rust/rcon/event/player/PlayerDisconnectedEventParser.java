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
import io.graversen.rust.rcon.protocol.util.SteamId64;
import io.graversen.rust.rcon.util.CommonUtils;
import lombok.NonNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Parses the console line written when a player disconnects, in the form
 * {@code address:port/SteamID64/PlayerName disconnecting: reason}.
 */
public class PlayerDisconnectedEventParser extends BaseRustEventParser<PlayerDisconnectedEvent> {
    private static final Pattern DISCONNECTED_PATTERN = Pattern.compile("^[^\\s/]+/\\d{17}/.*disconnecting:", Pattern.DOTALL);

    @Override
    protected Function<RustRconResponse, Optional<PlayerDisconnectedEvent>> eventParser() {
        return payload -> {
            final var message = payload.getMessage();
            final String[] splitInput = message.split("/");
            final String[] splitInputLastElement = CommonUtils.partialJoin("/", splitInput, 2, splitInput.length).split("disconnecting:");

            final String steamId64 = splitInput[1];
            final String reason = splitInputLastElement[1].trim();

            final var playerDisconnectedEvent = new PlayerDisconnectedEvent(
                    new SteamId64(steamId64),
                    reason
            );

            return Optional.of(playerDisconnectedEvent);
        };
    }

    /**
     * Tells whether the message is a disconnection line. The line must start with the client address followed by
     * the Steam ID, so chat messages are never supported, even if their text contains {@code disconnecting:}.
     *
     * @param payload the RCON message
     * @return {@code true} if the message is a disconnection line
     */
    @Override
    public boolean supports(@NonNull RustRconResponse payload) {
        return !PlayerChatEventParser.isChat(payload) && DISCONNECTED_PATTERN.matcher(payload.getMessage()).find();
    }

    @Override
    public Class<PlayerDisconnectedEvent> eventClass() {
        return PlayerDisconnectedEvent.class;
    }
}
