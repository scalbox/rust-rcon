package com.scalbox.rust.rcon.protocol.oxide;

import com.scalbox.rust.rcon.RustRconResponse;
import com.scalbox.rust.rcon.RustRconRouter;
import com.scalbox.rust.rcon.protocol.DefaultRustCodec;
import lombok.NonNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.scalbox.rust.rcon.protocol.RustProtocolTemplates.OxideProtocol.*;
import static com.scalbox.rust.rcon.protocol.RustProtocolTemplates.Placeholders.OxidePlaceholders.*;
import static com.scalbox.rust.rcon.protocol.RustProtocolTemplates.Placeholders.stripped;


public class DefaultOxideCodec extends DefaultRustCodec implements OxideCodec {
    public DefaultOxideCodec(@NonNull RustRconRouter rustRconRouter) {
        super(rustRconRouter);
    }

    @Override
    public CompletableFuture<RustRconResponse> oxidePlugins() {
        final var rconMessage = compile(PLUGINS);
        return send(rconMessage);
    }

    @Override
    public CompletableFuture<RustRconResponse> grant(@NonNull OxidePermission permission) {
        final var rconMessage = compile(
                GRANT,
                Map.of(
                        stripped(PERMISSION_TYPE), permission.permissionType().name().toLowerCase(),
                        stripped(NAME), permission.name(),
                        stripped(PERMISSION), permission.permission()
                )
        );
        return send(rconMessage);
    }

    @Override
    public CompletableFuture<RustRconResponse> revoke(@NonNull OxidePermission permission) {
        final var rconMessage = compile(
                REVOKE,
                Map.of(
                        stripped(PERMISSION_TYPE), permission.permissionType().name().toLowerCase(),
                        stripped(NAME), permission.name(),
                        stripped(PERMISSION), permission.permission()
                )
        );
        return send(rconMessage);
    }
}
