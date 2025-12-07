package com.scalbox.rust.rcon.event.player;

import com.scalbox.rust.rcon.protocol.util.PlayerName;
import com.scalbox.rust.rcon.protocol.util.SteamId64;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class PlayerMiniCopterCrashedEvent extends PlayerEvent{
    private final @NonNull PlayerName playerName;

    public PlayerMiniCopterCrashedEvent(@NonNull SteamId64 steamId, @NonNull PlayerName playerName) {
        super(steamId);
        this.playerName = playerName;
    }
}
