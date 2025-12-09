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

@Getter
public class RustRconResponseDTO {
    @JsonProperty("Identifier")
    private final Integer identifier;
    @JsonProperty("Message")
    private final String message;
    @JsonProperty("Type")
    private final String type;
    @JsonProperty("Stacktrace")
    private final String stackTrace;

    @JsonCreator
    public RustRconResponseDTO(
            @JsonProperty("Identifier") Integer identifier,
            @JsonProperty("Message") String message,
            @JsonProperty("Type") String type,
            @JsonProperty("Stacktrace") String stackTrace) {
        this.identifier = identifier;
        this.message = message;
        this.type = type;
        this.stackTrace = stackTrace;
    }
}
