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

import lombok.*;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

@Getter
@ToString
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class RustRconResponse {
    private final @NonNull LocalDateTime createdAt = LocalDateTime.now();
    private final @NonNull Integer identifier;
    private final @NonNull String message;
    private final @NonNull String type;
    private final @NonNull RustServer server;
    private final @Nullable RustRconRequest request;
}
