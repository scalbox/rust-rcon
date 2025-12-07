package com.scalbox.rust.rcon.tasks;

import com.scalbox.rust.rcon.RustServer;
import com.scalbox.rust.rcon.event.server.RustPlayersEvent;
import com.scalbox.rust.rcon.protocol.dto.RustPlayerDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class RustPlayersEmitTask extends RconTask {
    private final @NonNull RustServer server;
    private final @NonNull Supplier<CompletableFuture<List<RustPlayerDTO>>> rustPlayersGetter;
    private final @NonNull Consumer<RustPlayersEvent> rustPlayersEventEmitter;

    @Override
    public void execute() {
        rustPlayersGetter.get()
                .thenApply(rustPlayersEventMapper())
                .thenAccept(rustPlayersEventEmitter);
    }

    Function<List<RustPlayerDTO>, RustPlayersEvent> rustPlayersEventMapper() {
        return rustPlayers -> new RustPlayersEvent(server, rustPlayers);
    }
}
