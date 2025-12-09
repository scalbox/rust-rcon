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

package com.scalbox.rust.rcon;

import com.google.common.eventbus.Subscribe;
import com.scalbox.rust.rcon.event.server.ServerInfoEvent;
import com.scalbox.rust.rcon.util.CommonUtils;
import com.scalbox.rust.rcon.util.RustUtils;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ServerInfoDiagnosticsEventListener {
    private final @NonNull Consumer<RustDiagnostics> diagnosticsConsumer;

    @Subscribe
    public void onServerInfo(ServerInfoEvent serverInfoEvent) {
        final var rustDiagnostics = mapRustDiagnostics().apply(serverInfoEvent);
        diagnosticsConsumer.accept(rustDiagnostics);
    }

    Function<ServerInfoEvent, RustDiagnostics> mapRustDiagnostics() {
        return serverInfoEvent -> new SimpleRustRconDiagnostics(
                CommonUtils.now(),
                Duration.ofSeconds(serverInfoEvent.getServerInfo().getUptimeSeconds()),
                RustUtils.parseRustDateTime(serverInfoEvent.getServerInfo().getSaveCreatedTime()),
                String.valueOf(serverInfoEvent.getServerInfo().getVersion()),
                serverInfoEvent.getServerInfo().getProtocol(),
                serverInfoEvent.getServerInfo().getMaxPlayers(),
                serverInfoEvent.getServerInfo().getCurrentPlayers(),
                RustUtils.parseRustDateTime(serverInfoEvent.getServerInfo().getGameDateTime()),
                BigDecimal.valueOf(serverInfoEvent.getServerInfo().getFrameRate())
        );
    }
}
