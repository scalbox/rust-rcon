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
