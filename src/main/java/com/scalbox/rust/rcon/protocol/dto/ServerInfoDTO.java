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

package com.scalbox.rust.rcon.protocol.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class ServerInfoDTO {
    @JsonProperty("Hostname")
    private final String hostName;

    @JsonProperty("MaxPlayers")
    private final Integer maxPlayers;

    @JsonProperty("Players")
    private final Integer currentPlayers;

    @JsonProperty("Queued")
    private final Integer queuedPlayers;

    @JsonProperty("Joining")
    private final Integer joiningPlayers;

    @JsonProperty("EntityCount")
    private final Integer entityCount;

    @JsonProperty("GameTime")
    private final String gameDateTime;

    @JsonProperty("Uptime")
    private final Long uptimeSeconds;

    @JsonProperty("Map")
    private final String map;

    @JsonProperty("Framerate")
    private final Double frameRate;

    @JsonProperty("Memory")
    private final Integer memoryUsageMb;

    @JsonProperty("Collections")
    private final Integer collections;

    @JsonProperty("NetworkIn")
    private final Integer networkInBytes;

    @JsonProperty("NetworkOut")
    private final Integer networkOutBytes;

    @JsonProperty("Restarting")
    private final Boolean restarting;

    @JsonProperty("SaveCreatedTime")
    private final String saveCreatedTime;

    @JsonProperty("Version")
    private final Integer version;

    @JsonProperty("Protocol")
    private final String protocol;
}
