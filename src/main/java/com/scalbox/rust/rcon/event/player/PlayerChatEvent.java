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

import com.scalbox.rust.rcon.protocol.util.ChatChannels;
import com.scalbox.rust.rcon.protocol.util.PlayerName;
import com.scalbox.rust.rcon.protocol.util.SteamId64;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class PlayerChatEvent extends PlayerEvent {
    private final @NonNull PlayerName playerName;
    private final @NonNull String message;
    private final @NonNull ChatChannels chatChannel;

    public PlayerChatEvent(
            @NonNull SteamId64 steamId,
            @NonNull PlayerName playerName,
            @NonNull String message,
            @NonNull ChatChannels chatChannel
    ) {
        super(steamId);
        this.playerName = playerName;
        this.message = message;
        this.chatChannel = chatChannel;
    }
}
