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

package io.graversen.rust.rcon.protocol;

import io.graversen.rust.rcon.RustRconResponse;
import io.graversen.rust.rcon.RustRconRouter;
import io.graversen.rust.rcon.protocol.util.PlayerName;
import io.graversen.rust.rcon.protocol.util.SteamId64;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static io.graversen.rust.rcon.protocol.DefaultPlaceholders.DEFAULT_PLAYER_NAME;
import static io.graversen.rust.rcon.protocol.DefaultPlaceholders.DEFAULT_REASON;
import static io.graversen.rust.rcon.protocol.RustProtocolTemplates.AdminProtocol.*;
import static io.graversen.rust.rcon.protocol.RustProtocolTemplates.Placeholders.*;
import static io.graversen.rust.rcon.protocol.util.RconArguments.escape;
import static java.util.Objects.requireNonNullElse;

/**
 * Default {@link AdminCodec}: builds the administration console commands. Every value placed between double quotes
 * in a command (Steam ID, player name, reason) is escaped, so that it is always passed as a single argument.
 */
public class DefaultAdminCodec extends DefaultRustCodec implements AdminCodec {
    public DefaultAdminCodec(@NonNull RustRconRouter rustRconRouter) {
        super(rustRconRouter);
    }

    @Override
    public CompletableFuture<RustRconResponse> kickPlayer(@NonNull SteamId64 steamId64, @Nullable String reason) {
        final var rconMessage = compile(
                KICK_PLAYER,
                Map.of(
                        stripped(STEAM_ID_64), escape(steamId64.get()),
                        stripped(REASON), escape(requireNonNullElse(reason, DEFAULT_REASON))
                )
        );
        return send(rconMessage);
    }

    @Override
    public CompletableFuture<RustRconResponse> kickAllPlayers() {
        final var rconMessage = compile(KICK_ALL_PLAYERS);
        return send(rconMessage);
    }

    @Override
    public CompletableFuture<RustRconResponse> banPlayer(@NonNull SteamId64 steamId64, @Nullable PlayerName playerName, @Nullable String reason) {
        final var rconMessage = compile(
                BAN_PLAYER,
                Map.of(
                        stripped(STEAM_ID_64), escape(steamId64.get()),
                        stripped(PLAYER_NAME), escape(requireNonNullElse(playerName, DEFAULT_PLAYER_NAME).get()),
                        stripped(REASON), escape(requireNonNullElse(reason, DEFAULT_REASON))
                )
        );
        return send(rconMessage);
    }

    @Override
    public CompletableFuture<RustRconResponse> unbanPlayer(@NonNull SteamId64 steamId64) {
        final var rconMessage = compile(
                UNBAN_PLAYER,
                Map.of(
                        stripped(STEAM_ID_64), escape(steamId64.get())
                )
        );
        return send(rconMessage);
    }

    @Override
    public CompletableFuture<RustRconResponse> addOwner(@NonNull SteamId64 steamId64, @Nullable PlayerName playerName) {
        final var rconMessage = compile(
                ADD_OWNER,
                Map.of(
                        stripped(STEAM_ID_64), escape(steamId64.get()),
                        stripped(PLAYER_NAME), escape(requireNonNullElse(playerName, DEFAULT_PLAYER_NAME).get())
                )
        );
        return send(rconMessage);
    }

    @Override
    public CompletableFuture<RustRconResponse> removeOwner(@NonNull SteamId64 steamId64) {
        final var rconMessage = compile(
                REMOVE_OWNER,
                Map.of(
                        stripped(STEAM_ID_64), escape(steamId64.get())
                )
        );
        return send(rconMessage);
    }

    @Override
    public CompletableFuture<RustRconResponse> mutePlayer(@NonNull SteamId64 steamId64) {
        final var rconMessage = compile(
                MUTE_PLAYER,
                Map.of(
                        stripped(STEAM_ID_64), escape(steamId64.get())
                )
        );
        return send(rconMessage);
    }

    @Override
    public CompletableFuture<RustRconResponse> unmutePlayer(@NonNull SteamId64 steamId64) {
        final var rconMessage = compile(
                UNMUTE_PLAYER,
                Map.of(
                        stripped(STEAM_ID_64), escape(steamId64.get())
                )
        );
        return send(rconMessage);
    }

    @Override
    public CompletableFuture<RustRconResponse> serverInfo() {
        final var rconMessage = compile(SERVER_INFO);
        return send(rconMessage);
    }

    @Override
    public CompletableFuture<RustRconResponse> playerList() {
        final var rconMessage = compile(PLAYER_LIST);
        return send(rconMessage);
    }

    @Override
    public CompletableFuture<RustRconResponse> sleepingPlayers() {
        final var rconMessage = compile(SLEEPING_PLAYERS);
        return send(rconMessage);
    }

    @Override
    public CompletableFuture<RustRconResponse> teamInfo(@NonNull SteamId64 steamId64) {
        final var rconMessage = compile(
                TEAM_INFO,
                Map.of(
                        stripped(STEAM_ID_64), escape(steamId64.get())
                )
        );
        return send(rconMessage);
    }
}
