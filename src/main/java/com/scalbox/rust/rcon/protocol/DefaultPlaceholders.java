package com.scalbox.rust.rcon.protocol;

import com.scalbox.rust.rcon.protocol.util.PlayerName;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DefaultPlaceholders {
    public static String DEFAULT_PLAYER_NAME_STRING = "Anonymous";
    public static PlayerName DEFAULT_PLAYER_NAME = new PlayerName(DEFAULT_PLAYER_NAME_STRING);
    public static String DEFAULT_REASON = "No Reason";
}
