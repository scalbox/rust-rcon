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
