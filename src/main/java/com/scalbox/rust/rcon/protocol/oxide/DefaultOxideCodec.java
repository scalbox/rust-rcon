/*
 * This file is based on the open-source project "Rust RCON"
 * (https://github.com/MrGraversen/rust-rcon), created by
 * Martin Graversen and originally released under the MIT license.
 *
 * Original base package: io.graversen.rust.rcon
 *
 * The code in this fork
 * (https://github.com/scalbox/rust-rcon) has been adapted and is
 * maintained by scalbox and is distributed under the terms of the
 * Apache License, Version 2.0. For more details, see the LICENSE
 * file included in this repository.
 */

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
