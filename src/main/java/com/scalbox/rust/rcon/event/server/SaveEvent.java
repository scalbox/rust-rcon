package com.scalbox.rust.rcon.event.server;

import com.scalbox.rust.rcon.RustServer;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class SaveEvent extends ServerEvent {
    SaveEvent(@NonNull RustServer server) {
        super(server);
    }
}
