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

package io.graversen.rust.rcon.protocol;

import io.graversen.rust.rcon.RustRconRouter;
import io.graversen.rust.rcon.protocol.util.PlayerName;
import io.graversen.rust.rcon.protocol.util.SteamId64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultAdminCodecTest {
    @Mock
    private RustRconRouter rustRconRouter;

    @InjectMocks
    private DefaultAdminCodec defaultAdminCodec;

    @Captor
    private ArgumentCaptor<RustRconMessage> rconMessageCaptor;

    @Test
    void banPlayer() {
        final var steamId = SteamId64.parseOrFail("76561197979952036");
        final var playerName = new PlayerName("Doctor Delete");
        final var reason = "You are banned m8";
        defaultAdminCodec.banPlayer(steamId, playerName, reason);

        verify(rustRconRouter).send(rconMessageCaptor.capture());
        assertEquals("global.banid \"76561197979952036\" \"Doctor Delete\" \"You are banned m8\"", rconMessageCaptor.getValue().get());
    }

    @Test
    void kickPlayer() {
        final var steamId = SteamId64.parseOrFail("76561197979952036");
        final var reason = "You are kicked m8";
        defaultAdminCodec.kickPlayer(steamId, reason);

        verify(rustRconRouter).send(rconMessageCaptor.capture());
        assertEquals("global.kick \"76561197979952036\" \"You are kicked m8\"", rconMessageCaptor.getValue().get());
    }

    @Test
    void banPlayerEscapesQuotesInNameAndReason() {
        final var steamId = SteamId64.parseOrFail("76561197979952036");
        final var playerName = new PlayerName("x\" \"evil");
        final var reason = "said \"hi\" C:\\temp";
        defaultAdminCodec.banPlayer(steamId, playerName, reason);

        verify(rustRconRouter).send(rconMessageCaptor.capture());
        assertEquals(
                "global.banid \"76561197979952036\" \"x\\\" \\\"evil\" \"said \\\"hi\\\" C:\\\\temp\"",
                rconMessageCaptor.getValue().get()
        );
    }

    @Test
    void banPlayerKeepsPlaceholdersInNamesAsText() {
        final var steamId = SteamId64.parseOrFail("76561197979952036");
        defaultAdminCodec.banPlayer(steamId, new PlayerName("${reason}"), "cheating");
        defaultAdminCodec.banPlayer(steamId, new PlayerName("${playerName}"), "cheating");

        verify(rustRconRouter, times(2)).send(rconMessageCaptor.capture());
        assertEquals("global.banid \"76561197979952036\" \"${reason}\" \"cheating\"", rconMessageCaptor.getAllValues().get(0).get());
        assertEquals("global.banid \"76561197979952036\" \"${playerName}\" \"cheating\"", rconMessageCaptor.getAllValues().get(1).get());
    }

    @Test
    void kickPlayerEscapesQuotesInReason() {
        final var steamId = SteamId64.parseOrFail("76561197979952036");
        defaultAdminCodec.kickPlayer(steamId, "bye\" \"76561197979952037");

        verify(rustRconRouter).send(rconMessageCaptor.capture());
        assertEquals("global.kick \"76561197979952036\" \"bye\\\" \\\"76561197979952037\"", rconMessageCaptor.getValue().get());
    }

    @Test
    void addOwnerEscapesQuotesInName() {
        final var steamId = SteamId64.parseOrFail("76561197979952036");
        defaultAdminCodec.addOwner(steamId, new PlayerName("Doc \"Delete\""));

        verify(rustRconRouter).send(rconMessageCaptor.capture());
        assertEquals("global.ownerid \"76561197979952036\" \"Doc \\\"Delete\\\"\"", rconMessageCaptor.getValue().get());
    }
}