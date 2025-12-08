package com.scalbox.rust.rcon;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class RustRconResponseDTO {
    @JsonProperty("Identifier")
    private final Integer identifier;
    @JsonProperty("Message")
    private final String message;
    @JsonProperty("Type")
    private final String type;
    @JsonProperty("Stacktrace")
    private final String stackTrace;

    @JsonCreator
    public RustRconResponseDTO(
            @JsonProperty("Identifier") Integer identifier,
            @JsonProperty("Message") String message,
            @JsonProperty("Type") String type,
            @JsonProperty("Stacktrace") String stackTrace) {
        this.identifier = identifier;
        this.message = message;
        this.type = type;
        this.stackTrace = stackTrace;
    }
}
