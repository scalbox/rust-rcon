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

package com.scalbox.rust.rcon.event.rcon;

import com.scalbox.rust.rcon.RustRconRequest;
import com.scalbox.rust.rcon.RustRconResponse;
import com.scalbox.rust.rcon.event.RustEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.Duration;

@Getter
@ToString
@RequiredArgsConstructor
public class RconProtocolExchangeEvent extends RustEvent {
    private final @NonNull RustRconRequest request;
    private final @NonNull RustRconResponse response;

    @ToString.Include
    public Duration latency() {
        return Duration.between(request.getCreatedAt(), response.getCreatedAt());
    }

    public String responseMessage() {
        return response.getMessage();
    }
}
