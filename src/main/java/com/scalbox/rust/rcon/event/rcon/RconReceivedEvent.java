package com.scalbox.rust.rcon.event.rcon;

import com.scalbox.rust.rcon.RustRconResponse;
import com.scalbox.rust.rcon.event.RustEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class RconReceivedEvent extends RustEvent {
    private final @NonNull String clientName;
    private final @NonNull RustRconResponse rconResponse;
}
