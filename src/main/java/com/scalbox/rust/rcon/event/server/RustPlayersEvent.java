package com.scalbox.rust.rcon.event.server;

import com.scalbox.rust.rcon.RustServer;
import com.scalbox.rust.rcon.protocol.dto.RustPlayerDTO;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;

@Getter
@ToString(callSuper = true)
public class RustPlayersEvent extends ServerEvent {
    private final @NonNull List<RustPlayerDTO> rustPlayers;

    public RustPlayersEvent(@NonNull RustServer server, @NonNull List<RustPlayerDTO> rustPlayers) {
        super(server);
        this.rustPlayers = rustPlayers;
    }
}
