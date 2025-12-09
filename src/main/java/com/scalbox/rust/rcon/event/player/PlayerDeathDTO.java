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

package com.scalbox.rust.rcon.event.player;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class PlayerDeathDTO {
    @JsonProperty("victim")
    private final String victim;
    @JsonProperty("killer")
    private final String killer;
    @JsonProperty("bodypart")
    private final String bodyPart;
    @JsonProperty("distance")
    private final String distance;
    @JsonProperty("hp")
    private final String health;
    @JsonProperty("weapon")
    private final String weapon;
    @JsonProperty("attachments")
    private final String attachments;
    @JsonProperty("killerId")
    private final String killerId;
    @JsonProperty("victimId")
    private final String victimId;
    @JsonProperty("damageType")
    private final String damageType;
    @JsonProperty("killerEntityType")
    private final String killerEntityType;
    @JsonProperty("victimEntityType")
    private final String victimEntityType;
    @JsonProperty("owner")
    private final String owner;
}
