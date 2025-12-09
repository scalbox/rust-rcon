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

package com.scalbox.rust.rcon.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Synchronized;

import java.util.List;

public class DefaultJsonMapper implements JsonMapper {
    private ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public String toJson(@NonNull Object object) {
        return getObjectMapper().writeValueAsString(object);
    }

    @Override
    @SneakyThrows
    public <T> T fromJson(@NonNull String json, @NonNull Class<T> toClass) {
        return getObjectMapper().readValue(json, toClass);
    }

    @Override
    @SneakyThrows
    public <T> List<T> fromJsonArray(@NonNull String json, @NonNull Class<T> toClass) {
        CollectionType listType = getObjectMapper().getTypeFactory().constructCollectionType(List.class, toClass);
        return getObjectMapper().readValue(json, listType);
    }

    @Synchronized
    private ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }

        return objectMapper;
    }
}
