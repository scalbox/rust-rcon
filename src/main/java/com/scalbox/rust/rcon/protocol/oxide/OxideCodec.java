package com.scalbox.rust.rcon.protocol.oxide;

import com.scalbox.rust.rcon.RustRconResponse;
import lombok.NonNull;

import java.util.concurrent.CompletableFuture;

public interface OxideCodec {
    CompletableFuture<RustRconResponse> oxidePlugins();

    CompletableFuture<RustRconResponse> grant(@NonNull OxidePermission permission);

    CompletableFuture<RustRconResponse> revoke(@NonNull OxidePermission permission);
}
