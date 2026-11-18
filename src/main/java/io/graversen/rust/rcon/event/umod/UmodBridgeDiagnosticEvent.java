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

package io.graversen.rust.rcon.event.umod;

import io.graversen.rust.rcon.event.RustEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import org.jspecify.annotations.Nullable;

@Getter
@ToString(callSuper = true)
public class UmodBridgeDiagnosticEvent extends RustEvent {
    private final @NonNull UmodBridgeDiagnosticType diagnosticType;
    private final @NonNull String rawMessage;
    private final @Nullable String details;

    public UmodBridgeDiagnosticEvent(
            @NonNull UmodBridgeDiagnosticType diagnosticType,
            @NonNull String rawMessage,
            @Nullable String details
    ) {
        this.diagnosticType = diagnosticType;
        this.rawMessage = rawMessage;
        this.details = details;
    }
}
