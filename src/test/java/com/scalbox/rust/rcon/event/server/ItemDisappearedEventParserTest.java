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

package com.scalbox.rust.rcon.event.server;

import com.scalbox.rust.rcon.TestRustRconResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemDisappearedEventParserTest {
    private static final String ITEM_PATTERN_CASE = "Invalid Position: generic_world[43880625] ammo.nailgun.nails (world) (-0.3, -500.3, 0.0) (destroying)";
    private static final String CORPSE_PATTERN_CASE = "Invalid Position: chicken.corpse[43877258] (-429.9, -500.9, 443.4) (destroying)";
    private static final String BACKPACK_PATTERN_CASE = "Invalid Position: item_drop_backpack[43830336] (-4205.2, 31.2, -605.5) (destroying)";

    private final ItemDisappearedEventParser itemDisappearedEventParser = new ItemDisappearedEventParser();

    @Test
    void supports() {
        assertTrue(itemDisappearedEventParser.supports(new TestRustRconResponse(ITEM_PATTERN_CASE)));
        assertTrue(itemDisappearedEventParser.supports(new TestRustRconResponse(CORPSE_PATTERN_CASE)));
        assertTrue(itemDisappearedEventParser.supports(new TestRustRconResponse(BACKPACK_PATTERN_CASE)));
    }

    @Test
    void parseEvent_itemPattern() {
        final var event = itemDisappearedEventParser.eventParser().apply(new TestRustRconResponse(ITEM_PATTERN_CASE));
        assertTrue(event.isPresent());
        assertEquals(ItemDisappearTypes.ITEM_DISAPPEARED, event.get().getItemDisappearType());
        assertEquals("ammo.nailgun.nails", event.get().getDescription());
    }

    @Test
    void parseEvent_corpsePattern() {
        final var event = itemDisappearedEventParser.eventParser().apply(new TestRustRconResponse(CORPSE_PATTERN_CASE));
        assertTrue(event.isPresent());
        assertEquals(ItemDisappearTypes.ANIMAL_CORPSE_DISAPPEARED, event.get().getItemDisappearType());
        assertEquals("chicken.corpse", event.get().getDescription());
    }

    @Test
    void parseEvent_backpackPattern() {
        final var event = itemDisappearedEventParser.eventParser().apply(new TestRustRconResponse(BACKPACK_PATTERN_CASE));
        assertTrue(event.isPresent());
        assertEquals(ItemDisappearTypes.PLAYER_OR_SCIENTIST_BACKPACK_DISAPPEARED, event.get().getItemDisappearType());
        assertNull(event.get().getDescription());
    }
}