package com.scalbox.rust.rcon.event.rcon;

import com.scalbox.rust.rcon.RustRconRequest;
import com.scalbox.rust.rcon.RustRconResponse;
import com.scalbox.rust.rcon.event.RustEvent;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.Duration;

@Getter
@ToString
@RequiredArgsConstructor
public class RconProtocolExchangeEvent extends RustEvent {
    private final @NonNull RustRconRequest request;
    private final @NonNull RustRconResponse response;

    @ToString.Include
    public Duration latency() {
        return Duration.between(request.getCreatedAt(), response.getCreatedAt());
    }

    public String responseMessage() {
        return response.getMessage();
    }
}
