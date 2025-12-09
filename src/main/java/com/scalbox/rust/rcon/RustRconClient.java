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

import com.scalbox.rust.rcon.util.EventEmitter;
import com.scalbox.rust.rcon.util.JsonMapper;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface RustRconClient extends EventEmitter, Closeable {
    CompletableFuture<RustRconResponse> send(@NonNull RustRconRequest rustRconRequest);

    Function<RustRconRequest, RustRconRequestDTO> mapRequest();

    Function<RustRconResponseDTO, RustRconResponse> mapResponse(@Nullable RustRconRequest request);

    JsonMapper jsonMapper();

    void connect();

    RustServer rustServer();

    default String name() {
        return "rust-rcon";
    }

    default Integer initialMessageIdentifier() {
        return 1;
    }
}
