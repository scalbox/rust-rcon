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

import com.scalbox.rust.rcon.RustServer;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

@Getter
@ToString(callSuper = true)
public class ItemDisappearedEvent extends ServerEvent {
    private final @NonNull ItemDisappearTypes itemDisappearType;
    private final @Nullable String description;

    public ItemDisappearedEvent(
            @NonNull RustServer server,
            @NonNull ItemDisappearTypes itemDisappearType,
            @Nullable String description
    ) {
        super(server);
        this.itemDisappearType = itemDisappearType;
        this.description = description;
    }
}
