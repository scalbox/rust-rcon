package com.scalbox.rust.rcon.event.server;

import com.scalbox.rust.rcon.RustServer;
import com.scalbox.rust.rcon.event.RustEvent;
import lombok.*;

@Getter
@ToString(callSuper = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ServerEvent extends RustEvent {
    private final @NonNull RustServer server;
}
