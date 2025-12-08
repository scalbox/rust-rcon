package com.scalbox.rust.rcon;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class RustRconRequestDTO {
    @JsonProperty("Identifier")
    private final @NonNull Integer identifier;
    @JsonProperty("Message")
    private final @NonNull String message;
    @JsonProperty("Name")
    private final @NonNull String name;

    @JsonCreator
    public RustRconRequestDTO(
            @JsonProperty("Identifier") @NonNull Integer identifier,
            @JsonProperty("Message") @NonNull String message,
            @JsonProperty("Name") @NonNull String name) {
        this.identifier = identifier;
        this.message = message;
        this.name = name;
    }
}
