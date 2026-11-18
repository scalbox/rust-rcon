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

import io.graversen.rust.rcon.util.EventEmitter;
import io.graversen.rust.rcon.util.JsonMapper;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Low-level RCON client: sends requests to a Rust server and correlates each response with its request through the
 * request identifier. Every message received from the server is also posted as an event.
 */
public interface RustRconClient extends EventEmitter, Closeable {
    /**
     * Sends a request to the server.
     *
     * @param rustRconRequest the request to send
     * @return a future completed with the response, or exceptionally if the request cannot be sent, the connection
     * is closed before the response arrives or no response arrives in time
     */
    CompletableFuture<RustRconResponse> send(@NonNull RustRconRequest rustRconRequest);

    /**
     * Returns the function that maps a request to the DTO serialized on the wire.
     *
     * @return the request mapper
     */
    Function<RustRconRequest, RustRconRequestDTO> mapRequest();

    /**
     * Returns the function that maps a DTO received from the server to a response.
     *
     * @param request the request the response belongs to, or {@code null} for unsolicited messages
     * @return the response mapper
     */
    Function<RustRconResponseDTO, RustRconResponse> mapResponse(@Nullable RustRconRequest request);

    /**
     * Returns the JSON mapper used to serialize requests and parse responses.
     *
     * @return the JSON mapper
     */
    JsonMapper jsonMapper();

    /**
     * Starts connecting to the server, without waiting for the connection to be established.
     */
    void connect();

    /**
     * Returns the server this client connects to.
     *
     * @return the server descriptor
     */
    RustServer rustServer();

    /**
     * Returns the client name sent with every request and attached to the received events.
     *
     * @return the client name, {@code rust-rcon} by default
     */
    default String name() {
        return "rust-rcon";
    }

    /**
     * Returns the identifier of the first request. Messages with a lower identifier are treated as unsolicited
     * server output.
     *
     * @return the first request identifier, 1 by default
     */
    default Integer initialMessageIdentifier() {
        return 1;
    }
}
