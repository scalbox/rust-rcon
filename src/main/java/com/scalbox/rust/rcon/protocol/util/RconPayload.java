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

package com.scalbox.rust.rcon.protocol.util;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.commons.text.StringSubstitutor;

import java.util.Map;
import java.util.function.Supplier;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RconPayload implements Supplier<String> {
    @NonNull String payload;

    static RconPayload raw(@NonNull String payload) {
        return new RconPayload(payload);
    }

    static RconPayload build(@NonNull String payloadTemplate, Object... args) {
        final var payload = String.format(payloadTemplate, args);
        return new RconPayload(payload);
    }

    static RconPayload build(@NonNull String payloadTemplate, Map<String, Object> args) {
        final var payload = StringSubstitutor.replace(payloadTemplate, args);
        return new RconPayload(payload);
    }

    @Override
    public String get() {
        return payload;
    }
}
