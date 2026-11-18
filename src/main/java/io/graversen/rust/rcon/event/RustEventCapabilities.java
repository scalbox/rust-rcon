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

package io.graversen.rust.rcon.event;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Set;

@Getter
@ToString
public class RustEventCapabilities {
    private final @NonNull RustEventSourceStrategy strategy;
    private final @NonNull Set<Class<? extends RustEvent>> supportedEvents;

    public RustEventCapabilities(
            @NonNull RustEventSourceStrategy strategy,
            @NonNull Set<Class<? extends RustEvent>> supportedEvents
    ) {
        this.strategy = strategy;
        this.supportedEvents = Set.copyOf(supportedEvents);
    }

    public boolean supports(@NonNull Class<? extends RustEvent> eventClass) {
        return supportedEvents.contains(eventClass);
    }
}
