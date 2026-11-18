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

package io.graversen.rust.rcon.protocol.util;

public enum WorldEvents {
    AIRDROP,
    BRADLEY_APC_DESTROYED,
    CARGO_SHIP_HARBOR_APPROACH,
    CARGO_SHIP_HARBOR_ARRIVED,
    CARGO_SHIP_HARBOR_LEFT,
    CARGO_PLANE,
    CH47_SCIENTISTS,
    LOCKED_CRATE_HACK_COMPLETED,
    LOCKED_CRATE_HACK_STARTED,
    LOCKED_CRATE_LANDED,
    MLRS_FIRED,
    PATROL_HELICOPTER,
    PATROL_HELICOPTER_KILLED,
    SUPPLY_DROP_DROPPED,
    SUPPLY_DROP_LANDED,
    UNKNOWN;
}
