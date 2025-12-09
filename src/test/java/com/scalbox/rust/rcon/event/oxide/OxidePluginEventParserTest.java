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

package com.scalbox.rust.rcon.event.oxide;

import com.scalbox.rust.rcon.TestRustRconResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OxidePluginEventParserTest {
    private final OxidePluginEventParser oxidePluginEventParser = new OxidePluginEventParser();

    @Test
    void parseEvent_smoothRestarter() {
        final var message = "[SmoothRestarter] Current Oxide.Rust version is up-to-date, scheduling check after 600 seconds...";
        final var oxidePluginEvent = oxidePluginEventParser.eventParser().apply(new TestRustRconResponse(message));
        assertNotNull(oxidePluginEvent);
        assertTrue(oxidePluginEvent.isPresent());
        assertEquals("SmoothRestarter", oxidePluginEvent.get().getPluginName());
        assertEquals("Current Oxide.Rust version is up-to-date, scheduling check after 600 seconds...", oxidePluginEvent.get().getMessage());
    }

    @Test
    void parseEvent_undertaker() {
        final var message = "[Undertaker (Ownzone)] {\"victim\":\"Boar\",\"killer\":\"Сэр Ланселап\",\"bodypart\":\"Body\",\"distance\":\"1.3 meters\",\"hp\":\"94.7\",\"weapon\":\"Waterpipe Shotgun\",\"attachments\":\"\",\"killerId\":\"76561198201020867\",\"victimId\":null,\"damageType\":\"Bullet\",\"killerEntityType\":\"Player\",\"victimEntityType\":\"Animal\"}";
        final var oxidePluginEvent = oxidePluginEventParser.eventParser().apply(new TestRustRconResponse(message));
        assertNotNull(oxidePluginEvent);
        assertTrue(oxidePluginEvent.isPresent());
        assertEquals("Undertaker (Ownzone)", oxidePluginEvent.get().getPluginName());
        assertEquals("{\"victim\":\"Boar\",\"killer\":\"Сэр Ланселап\",\"bodypart\":\"Body\",\"distance\":\"1.3 meters\",\"hp\":\"94.7\",\"weapon\":\"Waterpipe Shotgun\",\"attachments\":\"\",\"killerId\":\"76561198201020867\",\"victimId\":null,\"damageType\":\"Bullet\",\"killerEntityType\":\"Player\",\"victimEntityType\":\"Animal\"}", oxidePluginEvent.get().getMessage());
    }

    @Test
    void supports_oxidePluginEvent() {
        final var supports = oxidePluginEventParser.supports(new TestRustRconResponse("[SmoothRestarter] Fetching latest Oxide.Rust version..."));
        assertTrue(supports);
    }

    @Test
    void supports_nativeEvent() {
        final var supports = oxidePluginEventParser.supports(new TestRustRconResponse("[event] assets/prefabs/npc/patrol helicopter/patrolhelicopter.prefab"));
        assertFalse(supports);

    }
}