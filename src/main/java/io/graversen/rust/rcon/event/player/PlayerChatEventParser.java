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
import io.graversen.rust.rcon.protocol.util.ChatChannels;
import io.graversen.rust.rcon.protocol.util.PlayerName;
import io.graversen.rust.rcon.protocol.util.SteamId64;
import lombok.NonNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

public class PlayerChatEventParser extends BaseRustEventParser<PlayerChatEvent> {
    /**
     * RCON message type of the chat messages sent by the server as JSON.
     */
    public static final String CHAT_MESSAGE_TYPE = "Chat";

    private static final String[] CHAT_PREFIXES = {"[CHAT] ", "[Global] ", "[TEAM CHAT] ", "[Team] "};

    private final Pattern CHAT_PATTERN = Pattern.compile("\\[CHAT\\] (.+)\\[(\\d+)\\] : (.+)");
    private final Pattern GLOBAL_CHAT_PATTERN = Pattern.compile("\\[Global\\] (.+?) : (.*)");
    private final Pattern TEAM_CHAT_PATTERN = Pattern.compile("\\[TEAM CHAT\\] (.+)\\[(\\d+)\\] : (.+)");
    private final Pattern SIMPLE_TEAM_CHAT_PATTERN = Pattern.compile("\\[Team\\] (.+?) : (.*)");

    /**
     * Tells whether a console line is a chat line, that is, whether it starts with {@code [CHAT] },
     * {@code [Global] }, {@code [TEAM CHAT] } or {@code [Team] }.
     *
     * @param message the console line
     * @return {@code true} if the line is a chat line
     */
    public static boolean hasChatPrefix(@NonNull String message) {
        for (var prefix : CHAT_PREFIXES) {
            if (message.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tells whether an RCON message carries text written by a player in chat: either a console chat line (see
     * {@link #hasChatPrefix(String)}) or a message of type {@value #CHAT_MESSAGE_TYPE}. Parsers of other events use
     * it to ignore text that a player could craft to look like a server line.
     *
     * @param payload the RCON message
     * @return {@code true} if the message comes from chat
     */
    public static boolean isChat(@NonNull RustRconResponse payload) {
        return CHAT_MESSAGE_TYPE.equalsIgnoreCase(payload.getType()) || hasChatPrefix(payload.getMessage());
    }

    @Override
    public boolean supports(@NonNull RustRconResponse payload) {
        return hasChatPrefix(payload.getMessage());
    }

    @Override
    public Class<PlayerChatEvent> eventClass() {
        return PlayerChatEvent.class;
    }

    @Override
    protected Function<RustRconResponse, Optional<PlayerChatEvent>> eventParser() {
        return rconResponse -> {
            final var message = rconResponse.getMessage();

            final var chatMatcher = CHAT_PATTERN.matcher(message);
            final var globalChatMatcher = GLOBAL_CHAT_PATTERN.matcher(message);
            final var teamChatMatcher = TEAM_CHAT_PATTERN.matcher(message);
            final var simpleTeamChatMatcher = SIMPLE_TEAM_CHAT_PATTERN.matcher(message);

            if (chatMatcher.find()) {
                final var playerName = chatMatcher.group(1).trim();
                final var steamId64 = chatMatcher.group(2).trim();
                final var chatMessage = chatMatcher.group(3).trim();

                final var playerChatEvent = new PlayerChatEvent(
                        new SteamId64(steamId64),
                        PlayerName.ofNullable(playerName),
                        chatMessage,
                        ChatChannels.DEFAULT
                );

                return Optional.of(playerChatEvent);
            } else if (globalChatMatcher.find()) {
                final var playerName = globalChatMatcher.group(1).trim();
                final var chatMessage = globalChatMatcher.group(2).trim();

                final var playerChatEvent = new PlayerChatEvent(
                        null,
                        PlayerName.ofNullable(playerName),
                        chatMessage,
                        ChatChannels.DEFAULT
                );

                return Optional.of(playerChatEvent);
            } else if (teamChatMatcher.find()) {
                final var playerName = teamChatMatcher.group(1).trim();
                final var steamId64 = teamChatMatcher.group(2).trim();
                final var chatMessage = teamChatMatcher.group(3).trim();

                final var playerChatEvent = new PlayerChatEvent(
                        new SteamId64(steamId64),
                        PlayerName.ofNullable(playerName),
                        chatMessage,
                        ChatChannels.TEAM
                );

                return Optional.of(playerChatEvent);
            } else if (simpleTeamChatMatcher.find()) {
                final var playerName = simpleTeamChatMatcher.group(1).trim();
                final var chatMessage = simpleTeamChatMatcher.group(2).trim();

                final var playerChatEvent = new PlayerChatEvent(
                        null,
                        PlayerName.ofNullable(playerName),
                        chatMessage,
                        ChatChannels.TEAM
                );

                return Optional.of(playerChatEvent);
            }

            return Optional.empty();
        };
    }
}
