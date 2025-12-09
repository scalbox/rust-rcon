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

package com.scalbox.rust.rcon.event.server;


import com.scalbox.rust.rcon.RustServer;
import com.scalbox.rust.rcon.protocol.dto.ServerInfoDTO;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class ServerInfoEvent extends ServerEvent{
    private final @NonNull ServerInfoDTO serverInfo;

    public ServerInfoEvent(@NonNull RustServer server, @NonNull ServerInfoDTO serverInfo) {
        super(server);
        this.serverInfo = serverInfo;
    }
}
