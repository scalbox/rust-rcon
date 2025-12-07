package com.scalbox.rust.rcon.event.oxide;

import com.scalbox.rust.rcon.event.RustEvent;
import lombok.*;

@Getter
@ToString
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class OxidePluginEvent extends RustEvent {
    private final @NonNull String pluginName;
    private final @NonNull String message;
}
