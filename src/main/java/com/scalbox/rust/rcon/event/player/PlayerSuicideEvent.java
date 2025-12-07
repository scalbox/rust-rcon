package com.scalbox.rust.rcon.event.player;

import com.scalbox.rust.rcon.protocol.util.SteamId64;
import com.scalbox.rust.rcon.protocol.util.SuicideCauses;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class PlayerSuicideEvent extends PlayerEvent {
    private final @NonNull SuicideCauses suicideCause;

    PlayerSuicideEvent(@NonNull SteamId64 steamId, @NonNull SuicideCauses suicideCause) {
        super(steamId);
        this.suicideCause = suicideCause;
    }
}
