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

package com.scalbox.rust.rcon.event.ws;

import com.google.common.eventbus.Subscribe;
import com.scalbox.rust.rcon.event.BaseEventHandler;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WsDebugSubscriber extends BaseEventHandler {
    @Subscribe
    public void onWsErrorEvent(@NonNull WsErrorEvent event) {
        handleEvent(event);
        log.debug("{}", event);
    }

    @Subscribe
    public void onWsClosedEvent(@NonNull WsClosedEvent event) {
        handleEvent(event);
        log.debug("{}", event);
    }

    @Subscribe
    public void onWsMessageEvent(@NonNull WsMessageEvent event) {
        handleEvent(event);
        log.debug("{}", event);
    }

    @Subscribe
    public void onWsOpenedEvent(@NonNull WsOpenedEvent event) {
        handleEvent(event);
        log.debug("{}", event);
    }
}
