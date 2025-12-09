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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class RustRconRequestDTO {
    @JsonProperty("Identifier")
    private final @NonNull Integer identifier;
    @JsonProperty("Message")
    private final @NonNull String message;
    @JsonProperty("Name")
    private final @NonNull String name;

    @JsonCreator
    public RustRconRequestDTO(
            @JsonProperty("Identifier") @NonNull Integer identifier,
            @JsonProperty("Message") @NonNull String message,
            @JsonProperty("Name") @NonNull String name) {
        this.identifier = identifier;
        this.message = message;
        this.name = name;
    }
}
