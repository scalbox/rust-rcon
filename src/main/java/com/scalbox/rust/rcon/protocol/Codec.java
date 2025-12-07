package com.scalbox.rust.rcon.protocol;

import com.scalbox.rust.rcon.RustRconResponse;
import com.scalbox.rust.rcon.protocol.oxide.OxideCodec;
import lombok.NonNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface Codec {
    AdminCodec admin();

    SettingsCodec settings();

    EventCodec event();

    OxideCodec oxide();

    CompletableFuture<RustRconResponse> send(@NonNull RustRconMessage rustRconMessage);

    <T> CompletableFuture<T> send(@NonNull RustRconMessage rustRconMessage, @NonNull Function<RustRconResponse, T> mapper);

    default RustRconMessage raw(@NonNull String template) {
        return raw(template, Map.of());
    }

    RustRconMessage raw(@NonNull String template, @NonNull Map<String, String> values);
}
