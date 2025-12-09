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

package com.scalbox.rust.rcon.event;

import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

@ToString
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Event {
    @Getter
    private final @NonNull LocalDateTime emittedAt = LocalDateTime.now();
    private final @NonNull Set<EventHandler> eventHandlers = new HashSet<>();

    public Set<EventHandler> getEventHandlers() {
        return Set.copyOf(eventHandlers);
    }

    protected Function<Object, String> eventHandlerNameMapper() {
        return object -> object.getClass().getSimpleName();
    }

    void registerEventHandler(@NonNull Object handler) {
        final var handlerName = eventHandlerNameMapper().apply(handler);
        eventHandlers.add(new EventHandler(handlerName));
    }
}
