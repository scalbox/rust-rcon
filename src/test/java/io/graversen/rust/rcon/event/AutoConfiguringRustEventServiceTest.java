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

package io.graversen.rust.rcon.event;

import com.google.common.eventbus.EventBus;
import io.graversen.rust.rcon.event.player.PlayerChatEvent;
import io.graversen.rust.rcon.event.player.PlayerConnectedEvent;
import io.graversen.rust.rcon.event.server.SaveEvent;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoConfiguringRustEventServiceTest {
    @Test
    void capabilitiesExposeConfiguredRconParserEvents() {
        final var eventService = new AutoConfiguringRustEventService(new EventBus());

        eventService.configure();

        final var capabilities = eventService.capabilities();
        assertEquals(RustEventSourceStrategy.RCON, capabilities.getStrategy());
        assertTrue(capabilities.supports(PlayerChatEvent.class));
        assertTrue(capabilities.supports(PlayerConnectedEvent.class));
        assertTrue(capabilities.supports(SaveEvent.class));
    }

    @Test
    void everyInstanceLoadsAllTheBuiltInParsers() {
        final var first = new AutoConfiguringRustEventService(new EventBus());
        final var second = new AutoConfiguringRustEventService(new EventBus());

        first.configure();
        second.configure();

        assertEquals(12, first.capabilities().getSupportedEvents().size());
        assertEquals(first.capabilities().getSupportedEvents(), second.capabilities().getSupportedEvents());
    }

    @Test
    void packageWithoutParsersYieldsNoCapabilities() {
        final var eventService = new AutoConfiguringRustEventService(new EventBus()) {
            @Override
            protected Set<String> eventParserPackages() {
                return Set.of("io.graversen.rust.rcon.does.not.exist");
            }
        };

        eventService.configure();

        assertTrue(eventService.capabilities().getSupportedEvents().isEmpty());
    }
}
