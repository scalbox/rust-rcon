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

package io.graversen.rust.rcon.event.umod;

import io.graversen.rust.rcon.protocol.oxide.OxideManagement;
import io.graversen.rust.rcon.protocol.oxide.OxidePlugin;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class DefaultUmodBridgeManagement implements UmodBridgeManagement {
    private final @NonNull OxideManagement oxideManagement;

    @Override
    public CompletableFuture<Optional<OxidePlugin>> rustRconBridgePlugin() {
        return oxideManagement.oxidePlugins()
                .thenApply(plugins -> plugins.stream()
                        .filter(this::isRustRconBridgePlugin)
                        .findFirst());
    }

    @Override
    public CompletableFuture<Boolean> isRustRconBridgeInstalled() {
        return rustRconBridgePlugin().thenApply(Optional::isPresent);
    }

    private boolean isRustRconBridgePlugin(@NonNull OxidePlugin plugin) {
        return RustRconBridgePlugin.PLUGIN_NAME.equalsIgnoreCase(plugin.getPluginName())
                || RustRconBridgePlugin.PLUGIN_FILE.equalsIgnoreCase(plugin.getPluginFile());
    }
}
