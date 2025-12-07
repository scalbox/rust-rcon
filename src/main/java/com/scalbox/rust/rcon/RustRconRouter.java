package com.scalbox.rust.rcon;

import com.scalbox.rust.rcon.protocol.RustRconMessage;
import lombok.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface RustRconRouter {
    <T> CompletableFuture<T> send(@NonNull RustRconMessage rustRconMessage, @NonNull Function<RustRconResponse, T> mapper);

    CompletableFuture<RustRconResponse> send(@NonNull RustRconMessage rustRconMessage);

    void start();

    void stop();
}
