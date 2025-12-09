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

import lombok.NonNull;

public class TestRustRconResponse extends RustRconResponse {
    public TestRustRconResponse(@NonNull String message) {
        super(0, message, "type", testServer(), new RustRconRequest(0, message));
    }

    private static RustServer testServer() {
        return new SimpleRustServer("test", "ws://test");
    }
}
