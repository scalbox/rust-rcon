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
