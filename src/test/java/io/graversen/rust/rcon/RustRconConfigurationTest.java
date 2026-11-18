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

package io.graversen.rust.rcon;

import io.graversen.rust.rcon.event.RustEventSourceStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RustRconConfigurationTest {
    @Test
    void defaultsEventSourceStrategyToRcon() {
        final var configuration = new RustRconConfiguration("localhost", 28016, "password");

        assertEquals(RustEventSourceStrategy.RCON, configuration.getEventSourceStrategy());
    }

    @Test
    void supportsExplicitUmodEventSourceStrategy() {
        final var configuration = new RustRconConfiguration(
                "localhost",
                28016,
                "password",
                RustEventSourceStrategy.UMOD
        );

        assertEquals(RustEventSourceStrategy.UMOD, configuration.getEventSourceStrategy());
    }
}
