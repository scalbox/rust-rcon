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

import io.graversen.rust.rcon.protocol.RustRconMessage;
import lombok.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Sends RCON messages to a Rust server, assigning each one a request identifier.
 */
public interface RustRconRouter {
    /**
     * Sends a message and maps the response.
     *
     * @param rustRconMessage the message to send
     * @param mapper          function applied to the response
     * @param <T>             type of the mapped response
     * @return a future completed with the mapped response, or exceptionally if the exchange fails
     */
    <T> CompletableFuture<T> send(@NonNull RustRconMessage rustRconMessage, @NonNull Function<RustRconResponse, T> mapper);

    /**
     * Sends a message.
     *
     * @param rustRconMessage the message to send
     * @return a future completed with the response, or exceptionally if the exchange fails
     */
    CompletableFuture<RustRconResponse> send(@NonNull RustRconMessage rustRconMessage);

    /**
     * Starts connecting to the server, without waiting for the connection to be established.
     */
    void start();

    /**
     * Closes the connection to the server.
     */
    void stop();
}
