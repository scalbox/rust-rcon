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

package io.graversen.rust.rcon.event.server;

import io.graversen.rust.rcon.RustServer;
import io.graversen.rust.rcon.protocol.util.WorldEvents;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Map;

@Getter
@ToString(callSuper = true)
public class WorldEvent extends ServerEvent {
    private final @NonNull WorldEvents event;
    private final @NonNull Map<String, String> attributes;

    public WorldEvent(@NonNull RustServer server, @NonNull WorldEvents event) {
        this(server, event, Map.of());
    }

    public WorldEvent(@NonNull RustServer server, @NonNull WorldEvents event, @NonNull Map<String, String> attributes) {
        super(server);
        this.event = event;
        this.attributes = Map.copyOf(attributes);
    }
}
