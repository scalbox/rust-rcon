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
import com.scalbox.rust.rcon.RustTeam;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;

@Getter
@ToString(callSuper = true)
public class RustTeamsEvent extends ServerEvent {
    private final @NonNull List<RustTeam> rustTeams;

    public RustTeamsEvent(@NonNull RustServer server, @NonNull List<RustTeam> rustTeams) {
        super(server);
        this.rustTeams = rustTeams;
    }
}
