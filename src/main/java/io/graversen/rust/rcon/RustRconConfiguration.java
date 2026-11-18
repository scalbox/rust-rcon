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
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;

import java.util.Objects;

/**
 * Connection settings of a Rust server: host, RCON port, RCON password and the source of the game events.
 * <p>
 * {@link #toString()} never includes the password, so the configuration can be logged safely.
 */
@Value
public class RustRconConfiguration {
    @NonNull String hostname;
    @NonNull Integer port;
    @ToString.Exclude
    @NonNull String password;
    @NonNull RustEventSourceStrategy eventSourceStrategy;

    /**
     * Creates a configuration that parses the game events from the RCON console output
     * ({@link RustEventSourceStrategy#RCON}).
     *
     * @param hostname host name or IP address of the Rust server
     * @param port     RCON port of the Rust server
     * @param password RCON password
     */
    public RustRconConfiguration(
            @NonNull String hostname,
            @NonNull Integer port,
            @NonNull String password
    ) {
        this(hostname, port, password, RustEventSourceStrategy.RCON);
    }

    /**
     * Creates a configuration.
     *
     * @param hostname            host name or IP address of the Rust server
     * @param port                RCON port of the Rust server
     * @param password            RCON password
     * @param eventSourceStrategy source of the game events
     */
    public RustRconConfiguration(
            @NonNull String hostname,
            @NonNull Integer port,
            @NonNull String password,
            @NonNull RustEventSourceStrategy eventSourceStrategy
    ) {
        this.hostname = Objects.requireNonNull(hostname);
        this.port = Objects.requireNonNull(port);
        this.password = Objects.requireNonNull(password);
        this.eventSourceStrategy = Objects.requireNonNull(eventSourceStrategy);
    }
}
