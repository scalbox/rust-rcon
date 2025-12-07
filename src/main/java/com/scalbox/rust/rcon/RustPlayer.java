package com.scalbox.rust.rcon;

import com.scalbox.rust.rcon.protocol.util.PlayerName;
import com.scalbox.rust.rcon.protocol.util.SteamId64;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class RustPlayer {
    private final @NonNull SteamId64 steamId;
    private final @NonNull PlayerName playerName;
}
