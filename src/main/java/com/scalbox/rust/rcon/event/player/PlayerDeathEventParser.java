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

import com.scalbox.rust.rcon.RustRconResponse;
import com.scalbox.rust.rcon.configuration.Configuration;
import com.scalbox.rust.rcon.event.BaseRustEventParser;
import com.scalbox.rust.rcon.protocol.util.*;
import com.scalbox.rust.rcon.util.DefaultJsonMapper;
import com.scalbox.rust.rcon.util.JsonMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class PlayerDeathEventParser extends BaseRustEventParser<PlayerDeathEvent> {
    private static final String MESSAGE_PREFIX = String.format("[%s]", Configuration.UNDERTAKER_PLUGIN_NAME);

    private JsonMapper jsonMapper;

    @Override
    public boolean supports(@NonNull RustRconResponse payload) {
        return payload.getMessage().startsWith(MESSAGE_PREFIX);
    }

    @Override
    protected Function<RustRconResponse, Optional<PlayerDeathEvent>> eventParser() {
        return rconResponse -> {
            final var message = rconResponse.getMessage();
            final var trimmedMessage = message.substring(MESSAGE_PREFIX.length()).trim();
            return mapPlayerDeath().apply(trimmedMessage).map(mapPlayerDeathEvent());
        };
    }

    @Override
    public Class<PlayerDeathEvent> eventClass() {
        return PlayerDeathEvent.class;
    }

    protected JsonMapper jsonMapper() {
        if (jsonMapper == null) {
            jsonMapper = new DefaultJsonMapper();
        }

        return jsonMapper;
    }

    Function<String, Optional<PlayerDeathDTO>> mapPlayerDeath() {
        return message -> {
            try {
                final var playerDeath = jsonMapper().fromJson(message, PlayerDeathDTO.class);
                return Optional.ofNullable(playerDeath);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return Optional.empty();
            }
        };
    }

    Function<PlayerDeathDTO, PlayerDeathEvent> mapPlayerDeathEvent() {
        return playerDeath -> {
            final var attachments = playerDeath.getAttachments() == null
                    ? Set.<String>of()
                    : Arrays.stream(playerDeath.getAttachments().split(",")).map(String::trim).collect(Collectors.toUnmodifiableSet());

            final var victimEntity = EntityTypes.parse(playerDeath.getVictimEntityType());
            final var killerEntity = EntityTypes.parse(playerDeath.getKillerEntityType());
            final var bodyPart = BodyParts.parse(playerDeath.getBodyPart());
            final var combatType = resolveCombatType(playerDeath);
            final var damageType = DamageTypes.parse(playerDeath.getDamageType());
            final var distance = playerDeath.getDistance().replaceAll("[^\\d.]", "");

            final var playerEventSteamId = SteamId64.parse(playerDeath.getKillerId())
                    .or(() -> SteamId64.parse(playerDeath.getVictimId()))
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Could not resolve SteamID from: %s", playerDeath)));

            return new PlayerDeathEvent(
                    playerEventSteamId,
                    playerDeath.getVictim(),
                    victimEntity,
                    SteamId64.parse(playerDeath.getVictimId()).orElse(null),
                    Objects.isNull(playerDeath.getOwner()) ? playerDeath.getKiller() : playerDeath.getOwner(),
                    killerEntity,
                    bodyPart,
                    new BigDecimal(distance),
                    new BigDecimal(Objects.requireNonNullElse(playerDeath.getHealth(), "0")),
                    Objects.isNull(playerDeath.getOwner()) ? playerDeath.getWeapon() : playerDeath.getKiller(),
                    attachments,
                    combatType,
                    damageType
            );
        };
    }

    CombatTypes resolveCombatType(@NonNull PlayerDeathDTO playerDeath) {
        if (playerDeath.getKillerId() != null && playerDeath.getVictimId() != null) {
            if (Objects.equals(playerDeath.getKillerId(), playerDeath.getVictimId())) {
                return CombatTypes.SUICIDE;
            }
        }

        return CombatTypes.resolve(playerDeath.getKillerEntityType(), playerDeath.getVictimEntityType());
    }
}
