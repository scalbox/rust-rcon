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

package com.scalbox.rust.rcon;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;

@Value
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class SimpleRustRconDiagnostics implements RustDiagnostics {
    @NonNull ZonedDateTime lastUpdatedAt;
    @NonNull Duration uptime;
    @NonNull ZonedDateTime serverWipedAt;
    @NonNull String version;
    @NonNull String protocol;
    @NonNull Integer maxPlayers;
    @NonNull Integer currentPlayers;
    @NonNull ZonedDateTime gameClock;
    @NonNull BigDecimal framerate;
}
