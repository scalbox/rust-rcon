package com.scalbox.rust.rcon.event.server;

import com.scalbox.rust.rcon.RustServer;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class EntityCommandEvent extends ServerEvent{
    private final @NonNull String command;

    public EntityCommandEvent(@NonNull RustServer server, @NonNull String command) {
        super(server);
        this.command = command;
    }
}
