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

package com.scalbox.rust.rcon.event.server;

import com.scalbox.rust.rcon.RustRconResponse;
import com.scalbox.rust.rcon.event.BaseRustEventParser;
import com.scalbox.rust.rcon.protocol.util.WorldEvents;
import lombok.NonNull;

import java.util.Optional;
import java.util.function.Function;

public class WorldEventParser extends BaseRustEventParser<WorldEvent> {
    @Override
    public boolean supports(@NonNull RustRconResponse payload) {
        return payload.getMessage().startsWith("[event] assets/prefabs");
    }

    @Override
    protected Function<RustRconResponse, Optional<WorldEvent>> eventParser() {
        return payload -> {
            final var message = payload.getMessage();
            if (message.equalsIgnoreCase("[event] assets/prefabs/npc/cargo plane/cargo_plane.prefab")) {
                return Optional.of(new WorldEvent(payload.getServer(), WorldEvents.CARGO_PLANE));
            } else if (message.equalsIgnoreCase("[event] assets/prefabs/npc/ch47/ch47scientists.entity.prefab")) {
                return Optional.of(new WorldEvent(payload.getServer(), WorldEvents.CH47_SCIENTISTS));
            } else if (message.equalsIgnoreCase("[event] assets/prefabs/npc/patrol helicopter/patrolhelicopter.prefab")) {
                return Optional.of(new WorldEvent(payload.getServer(), WorldEvents.PATROL_HELICOPTER));
            } else {
                return Optional.of(new WorldEvent(payload.getServer(), WorldEvents.UNKNOWN));
            }
        };
    }

    @Override
    public Class<WorldEvent> eventClass() {
        return WorldEvent.class;
    }
}
