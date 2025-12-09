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
import lombok.NonNull;

import java.util.Optional;
import java.util.function.Function;

public class SaveEventParser extends BaseRustEventParser<SaveEvent> {
    @Override
    public boolean supports(@NonNull RustRconResponse payload) {
        return payload.getMessage().equalsIgnoreCase("saving complete");
    }

    @Override
    protected Function<RustRconResponse, Optional<SaveEvent>> eventParser() {
        return payload -> Optional.of(new SaveEvent(payload.getServer()));
    }

    @Override
    public Class<SaveEvent> eventClass() {
        return SaveEvent.class;
    }
}
