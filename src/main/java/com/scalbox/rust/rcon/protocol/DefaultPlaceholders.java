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

package com.scalbox.rust.rcon.protocol;

import com.scalbox.rust.rcon.protocol.util.PlayerName;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DefaultPlaceholders {
    public static String DEFAULT_PLAYER_NAME_STRING = "Anonymous";
    public static PlayerName DEFAULT_PLAYER_NAME = new PlayerName(DEFAULT_PLAYER_NAME_STRING);
    public static String DEFAULT_REASON = "No Reason";
}
