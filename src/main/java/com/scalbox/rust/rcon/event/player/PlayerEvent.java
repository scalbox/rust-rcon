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

import com.scalbox.rust.rcon.event.RustEvent;
import com.scalbox.rust.rcon.protocol.util.SteamId64;
import lombok.*;

@Getter
@ToString
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class PlayerEvent extends RustEvent {
    private final @NonNull SteamId64 steamId;
}
