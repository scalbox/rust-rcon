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
