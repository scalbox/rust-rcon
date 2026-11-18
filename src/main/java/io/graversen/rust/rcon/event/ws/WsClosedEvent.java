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

package io.graversen.rust.rcon.event.ws;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Posted when the WebSocket connection is closed, or when a connection attempt fails.
 */
@Getter
@ToString(callSuper = true)
public class WsClosedEvent extends WsEvent {
    private final @NonNull Integer code;
    private final @NonNull String reason;
    private final boolean remote;

    /**
     * Creates an event for a close initiated by this client.
     *
     * @param serverUri redacted URI of the server
     * @param code      WebSocket close code
     * @param reason    close reason
     */
    public WsClosedEvent(@NonNull String serverUri, @NonNull Integer code, @NonNull String reason) {
        this(serverUri, code, reason, false);
    }

    /**
     * Creates an event.
     *
     * @param serverUri redacted URI of the server
     * @param code      WebSocket close code
     * @param reason    close reason
     * @param remote    {@code true} if the close was initiated by the server
     */
    public WsClosedEvent(@NonNull String serverUri, @NonNull Integer code, @NonNull String reason, boolean remote) {
        super(serverUri);
        this.code = code;
        this.reason = reason;
        this.remote = remote;
    }
}
