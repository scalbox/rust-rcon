package com.scalbox.rust.rcon.event;

import com.scalbox.rust.rcon.event.rcon.RconReceivedEvent;
import lombok.NonNull;

public interface RustEventService {
    void onRconReceived(@NonNull RconReceivedEvent event);

    void configure();
}
