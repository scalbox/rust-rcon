# Rust RCON V2 :video_game:

An asynchronous, fault-tolerant Rust RCON client built in Java.  
Seamlessly integrates with Rust's RCON request-response semantics using websocket connections.  
Ideal for those looking to harness the power of Rust's RCON with Java's portability and ease.

## About :pencil:

This library serves as a Rust (the video game) counterpart to the  
[**`minecraft-rcon`**](https://github.com/MrGraversen/minecraft-rcon) library.

It empowers applications with easy integration capabilities with a Rust game server via RCON,  
translating internal server details into actionable events.

By interfacing with Rust's native RCON payloads, the library efficiently captures and translates  
in-game occurrences — such as player connections, chats, combat and more — into events.  
This reactive design allows user code to be promptly informed about player-driven and server-driven actions.

Additionally, Rust RCON manages connection states intelligently, eliminating concerns about  
game server restarts. The connection is resilient: if the game server goes offline, the client  
automatically attempts to reconnect and seamlessly recovers once the server becomes available again.

### About RCON

RCON (Remote Console) is a protocol that allows for remote game server management.  
In the context of Rust, RCON is a powerful tool for administrators and developers who need to  
monitor, control, and integrate with game servers.

Whether it is issuing commands, retrieving information, or automating operational tasks,  
Rust’s RCON implementation provides a flexible interface for these operations — all without  
requiring direct access to the game server console.

This library, Rust RCON V2, simplifies and enhances interaction with Rust’s RCON system by  
providing a strongly-typed, event-driven Java client built around websocket communication.

## Installation :floppy_disk:

Rust RCON is available through **Maven Central Repository**.

Repository:

> https://central.sonatype.com/artifact/com.scalbox/rust-rcon

> https://repo1.maven.org/maven2/com/scalbox/rust-rcon/

LATEST Version

1. Maven:

```xml

<dependency>
    <groupId>com.scalbox</groupId>
    <artifactId>rust-rcon</artifactId>
    <version>2.0.0-alpha.5</version>
</dependency>
```

2. Gradle:

```groovy
implementation("com.scalbox:rust-rcon:2.0.0-alpha.5")
```

3. Now you can use Rust RCON in your Java project!

## Events :rocket:

Rust RCON offers a plethora of events that give developers a detailed insight into various facets of the Rust game server. Below is a categorized breakdown of these events:

### Oxide

- **OxidePluginEvent**: Pertains to activities and notifications related to Oxide plugins.

### Player

- **PlayerChatEvent**: Triggered when a player sends a chat message in the game.
- **PlayerConnectedEvent**: Fired when a player successfully connects to the server.
- **PlayerDeathEvent**: Captured when a player meets their demise in the game.
- **PlayerDisconnectedEvent**: Emitted when a player disconnects from the server.
- **PlayerMiniCopterCrashedEvent**: Indicates when a player's mini-copter crashes.
- **PlayerSuicideEvent**: Signaled when a player deliberately ends their in-game life.

### RCON

- **RconProtocolExchangeEvent**: Deals with the exchange of protocol-specific data via RCON.
- **RconReceivedEvent**: Emitted when the server sends a response or data through RCON.

### Server

- **EasyAntiCheatEvent**: Relates to notifications and details stemming from the EasyAntiCheat system.
- **ItemDisappearedEvent**: Triggered when an in-game item disappears due to various reasons.
- **SaveEvent**: Captured whenever there's a save action on the server, be it automatic or manual.
- **WorldEvent**: Encompasses events related to the overall game world and its elements.
- **ServerEvent**: General server-related events not categorized under other specific events.

### Websocket

- **WsOpenedEvent**: Signaled when a new websocket connection is successfully established.
- **WsMessageEvent**: Fired when a message is received over the websocket connection.
- **WsErrorEvent**: Triggered when there's an error in the websocket communication.
- **WsClosedEvent**: Emitted when the websocket connection is closed, either due to errors or deliberate actions.

## Examples :alembic:

*Coming soon*

## Useful Resources

* https://steamid.io/ - translate between Steam IDs
* https://www.corrosionhour.com/rust-item-list/ - all Rust items, including shortcodes required by inventory rcon

---

## About Original Author :pencil:

This repository (`Rust RCON V2`) started as an independent snapshot of the original  
[MrGraversen/rust-rcon](https://github.com/MrGraversen/rust-rcon) project and has since evolved  
as a separate codebase under the fork:

> https://github.com/scalbox/rust-rcon

For full attribution and licensing details (original MIT License and this fork’s Apache License 2.0),  
see [CREDITS.md](CREDITS.md).