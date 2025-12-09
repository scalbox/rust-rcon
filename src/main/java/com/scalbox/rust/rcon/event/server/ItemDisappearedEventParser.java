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

import com.scalbox.rust.rcon.RustRconResponse;
import com.scalbox.rust.rcon.event.BaseRustEventParser;
import lombok.NonNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

public class ItemDisappearedEventParser extends BaseRustEventParser<ItemDisappearedEvent> {
    private static final Pattern ITEM_PATTERN = Pattern.compile("Invalid Position: .*?\\[(\\d+)]\\s+(\\S+) \\(.*?\\) (.*)");
    private static final Pattern CORPSE_PATTERN = Pattern.compile("Invalid Position: (\\S+)\\[(\\d+)] .*?");
    private static final Pattern BACKPACK_PATTERN = Pattern.compile("Invalid Position: (item_drop_backpack)\\[(\\d+)] .*?");

    @Override
    public boolean supports(@NonNull RustRconResponse payload) {
        return payload.getMessage().startsWith("Invalid Position: ");
    }

    @Override
    public Class<ItemDisappearedEvent> eventClass() {
        return ItemDisappearedEvent.class;
    }

    @Override
    protected Function<RustRconResponse, Optional<ItemDisappearedEvent>> eventParser() {
        return rconResponse -> {
            final var message = rconResponse.getMessage();

            final var itemMatcher = ITEM_PATTERN.matcher(message);
            final var corpseMatcher = CORPSE_PATTERN.matcher(message);
            final var backpackMatcher = BACKPACK_PATTERN.matcher(message);

            if (itemMatcher.find()) {
                final var item = itemMatcher.group(2);
                final var itemDisappearEvent = new ItemDisappearedEvent(
                        rconResponse.getServer(),
                        ItemDisappearTypes.ITEM_DISAPPEARED,
                        item
                );
                return Optional.of(itemDisappearEvent);
            } else if (backpackMatcher.find()) {
                final var itemDisappearEvent = new ItemDisappearedEvent(
                        rconResponse.getServer(),
                        ItemDisappearTypes.PLAYER_OR_SCIENTIST_BACKPACK_DISAPPEARED,
                        null
                );
                return Optional.of(itemDisappearEvent);
            } else if (corpseMatcher.find()) {
                final var corpse = corpseMatcher.group(1);
                final var itemDisappearEvent = new ItemDisappearedEvent(
                        rconResponse.getServer(),
                        ItemDisappearTypes.ANIMAL_CORPSE_DISAPPEARED,
                        corpse
                );
                return Optional.of(itemDisappearEvent);
            } else {
                return Optional.empty();
            }
        };
    }
}
