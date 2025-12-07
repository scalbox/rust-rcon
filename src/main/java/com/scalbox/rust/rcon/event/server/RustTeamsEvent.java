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
