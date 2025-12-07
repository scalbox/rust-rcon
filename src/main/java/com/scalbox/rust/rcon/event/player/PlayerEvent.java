package com.scalbox.rust.rcon.event.player;

import com.scalbox.rust.rcon.event.RustEvent;
import com.scalbox.rust.rcon.protocol.util.SteamId64;
import lombok.*;

@Getter
@ToString
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class PlayerEvent extends RustEvent {
    private final @NonNull SteamId64 steamId;
}
