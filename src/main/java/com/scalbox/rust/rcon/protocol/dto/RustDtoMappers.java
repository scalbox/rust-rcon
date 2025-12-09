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

import com.scalbox.rust.rcon.RustRconResponse;
import com.scalbox.rust.rcon.util.JsonMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor
public class RustDtoMappers {
    private final @NonNull JsonMapper jsonMapper;

    public Function<RustRconResponse, List<BanDTO>> mapBans() {
        return rconResponse -> {
            final var bans = jsonMapper.fromJson(rconResponse.getMessage(), BanDTO[].class);
            return Arrays.stream(bans).toList();
        };
    }

    public Function<RustRconResponse, BuildInfoDTO> mapBuildInfo() {
        return rconResponse -> jsonMapper.fromJson(rconResponse.getMessage(), BuildInfoDTO.class);
    }

    public Function<RustRconResponse, ServerInfoDTO> mapServerInfo() {
        return rconResponse -> jsonMapper.fromJson(rconResponse.getMessage(), ServerInfoDTO.class);
    }

    public Function<RustRconResponse, List<RustPlayerDTO>> mapRustPlayers() {
        return rconResponse -> jsonMapper.fromJsonArray(rconResponse.getMessage(), RustPlayerDTO.class);
    }
}
