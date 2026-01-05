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
in-game occurrences — such as player connections, chats, combat, and more — into events.  
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

<hr>

## Examples :alembic:

**Difference between classes:** 

```java
public class DefaultRustRconService {}
public class DefaultRustRconRouter {}
public class DefaultRustRconClient {}
```

<hr>

<h3> DefaultRustRconService </h3>

```java
public class DefaultRustRconService {}
```

The `DefaultRustRconService` class is the **operational core** and the main implementation of the connection and management logic for a Rust game server using the RCON protocol.

Here are its main functions:

1. **Component Orchestration**: Acts as a central "hub" that initializes and connects the WebSocket client (`RustWebSocketClient`), the event system (`EventBus`), the communication protocols (`Codec`), and the data mappers (`RustDtoMappers`). It uses the `Lazy` pattern to initialize these components only when necessary.
2. **Lifecycle Management**: Manages the starting (`start()`) and stopping (`stop()`) of the server connection and the message router.
3. **Automation (Polling)**: In the `configure()` method, set up scheduled tasks (`ScheduledExecutorService`) that automatically poll the server at regular intervals to keep local data up to date. Specifically:
   * Server info (every 5 seconds).
   * Player list (every 3 seconds).
   * Team list (every 5 minutes).
4. **State Management**: Maintains the current server state, such as the player list (`rustPlayers`), teams (`rustTeams`), and diagnostics, in memory (via `AtomicReference`), making them accessible to the application without having to make a network request each time.
5. **API Facade**: Provides high-level methods to access specific functionality, such as player management (`playerManagement()`) or Oxide framework management (`oxideManagement()`).

To use the `DefaultRustRconService` class, you must follow a standard lifecycle: configuration, startup, interaction, and shutdown.

Here are the main steps:

1. Instantiation: Create an instance by passing a `RustRconConfiguration` object with connection details (IP, port, password).
2. Event Registration (Optional): If you want to react to events (e.g., chat messages, server logs), register your listeners before or after startup.
3. Startup: Call the `start()` method. This opens the WebSocket connection and starts automatic tasks (polling players, server info, etc.).
4. Interaction: Use the exposed methods to send commands or read state (e.g., `playerManagement()`, `codec()`, `players()`).
5. Stoppage: Call `stop()` when the application terminates to close connections and threads.

Here is a practical example of use:

```java
import com.scalbox.rust.rcon.DefaultRustRconService;
import com.scalbox.rust.rcon.RustRconConfiguration;
import com.scalbox.rust.rcon.protocol.dto.RustPlayerDTO;

public class ExampleUsage { 

   public static void main(String[] args) { 
      // 1. Configuration 
      RustRconConfiguration config = new RustRconConfiguration( 
         "127.0.0.1", 
         28016, 
         "your_password_rcon"
      ); 
      
      // 2. Instantiation of the service 
      DefaultRustRconService rconService = new DefaultRustRconService(config); 
      
      // (Optional) Enable logging of all RCON traffic to the console
      rconService.enableRconLogger();
      
      // 3. Start connection and background tasks
      rconService.start();
      
      // 4. Interaction: Example of sending a command (banning a player)
      // Note: playerManagement() offers high-level methods
      // rconService.playerManagement().ban("steamId", "reason");
      
      // 4. Interaction: Read cached data (automatically updated every 3 seconds)
      System.out.println("Online players: " + rconService.players().size());
      
      // 4. Interaction: Send raw command via Codec
      rconService.codec().send("say Hello Server!");
      
      // 5. Stop (usually when the application shuts down)
      rconService.stop();
   }
}
```

<hr>

<h3> DefaultRustRconRouter </h3>

```java
public class DefaultRustRconRouter {}
```

The `DefaultRustRconRouter` class acts as a **transport and correlation layer** between your application and the raw RCON client. While the `Service` (seen previously) handles the high-level logic (players, teams), this `Router` handles the sending and receiving mechanics.

Here's what it does specifically:

1. **Identifier (ID) Management**: The RCON protocol requires each packet sent to have a unique ID to associate the response with the correct request. This class generates these IDs automatically (`identifierGenerator`) using an `AtomicInteger`.
2. **Request/Response Correlation**: When you call `send()`, the class packages the message in a `RustRconRequest`, sends it, and returns a `CompletableFuture`. This allows the response to be handled asynchronously as soon as it arrives.
3. **Audit and Logging (Event Bus)**: Using the `handleRconProtocolExchange` method, every time a request is completed (successfully or unsuccessfully), an `RconProtocolExchangeEvent` is published to the `EventBus`. This is essential for logging traffic or for plugins that need to analyze everything that passes "over the wire".
4. **Client Abstraction**: Hides the connection details of the underlying `RustRconClient`, exposing only methods for sending messages and managing the lifecycle (`start`/`stop`).

### How to use it

Typically, this class is instantiated and managed internally by the `DefaultRustRconService`. However, if you're building a custom implementation or need to send raw commands bypassing the high-level logic, you can use it like this:

1. Dependency Injection: Requires a RustRconClient (the physical connection) and an EventBus (for events).
2. Lifecycle: You must call start() to connect and stop() to close.
3. Command Sending: You use the send() method, which accepts a message and (optionally) a mapping function to transform the response.

Here's an example of direct usage:

```java
import com.google.common.eventbus.EventBus;
import com.scalbox.rust.rcon.DefaultRustRconRouter;
import com.scalbox.rust.rcon.RustRconClient; // Hypothetical implementation
import com.scalbox.rust.rcon.protocol.RustRconMessage;

public class RouterUsageExample {

   public void example(RustRconClient rawClient, EventBus eventBus) {
      // 1. Instantiation
      DefaultRustRconRouter router = new DefaultRustRconRouter(rawClient, eventBus);
      
      // 2. Starting the underlying connection
      router.start();
      
      // 3. Creating a message (functional wrapper or object)
      RustRconMessage message = () -> "status";
      
      // 4. Asynchronous sending and response handling
      router.send(message).thenAccept(response -> {
        System.out.println("Response from server: " + response.getMessage());
      }).exceptional(ex -> {
        System.err.println("Error sending: " + ex.getMessage());
        return null;
      });
      
      // 5. Stop
      router.stop();
   }
}
```

<hr>

<h3> DefaultRustRconClient </h3>

```java
public class DefaultRustRconClient {}
```

The `DefaultRustRconClient` class is the concrete, low-level implementation of the RCON client. It acts as a protocol adapter between the raw WebSocket connection and the Java application logic.

Here are its main functions:

1. **Asynchronous Request/Response Handling**: This component solves the complexity of asynchrony. When you send a command, the server responds at a future time. This class uses a `CompletableFuture` cache to "remember" which request is pending and completes the correct future when the response with the same ID arrives (`onWsMessage`).
2. **Serialization/Deserialization**: Converts Java objects (`RustRconRequest`) to JSON for sending and converts the JSON received from the WebSocket into Java objects (`RustRconResponse`), using a `JsonMapper`.
3. **WebSocket Event Handling**: Listens for low-level WebSocket events (`WsOpenedEvent`, `WsMessageEvent`, `WsClosedEvent`) via the `EventBus` and reacts accordingly (e.g., by setting the `isInitialized` flag or publishing an `RconReceivedEvent`).
4. **Connection Management**: Controls the opening (`connect()`) and controlled closing (`close()`) of the underlying WebSocket connection.

### How to use it

Normally, this class is "hidden" within `DefaultRustRconRouter` or `DefaultRustRconService`. However, if you need to use it directly to manually manage the RCON protocol without the higher-level abstractions, here's how:

1. **Dependencies**: Requires a `RustWebSocketClient` (the physical connection) and an `EventBus`.
2. **Setup**: Instantiate the class and call `connect()`.
3. **Send**: Use `send()`, which returns a `CompletableFuture`.
4. **Closing**: Call `close()` to terminate.

Example of direct usage:

```java
import com.google.common.eventbus.EventBus;
import com.scalbox.rust.rcon.DefaultRustRconClient;
import com.scalbox.rust.rcon.protocol.RustRconRequest;
import com.scalbox.rust.rcon.ws.RustWebSocketClient; // Hypothetical implementation
import java.net.URI;

public class ClientUsage {

   public void manualClientUsage() {
      // 1. Prepare dependencies
      EventBus eventBus = new EventBus();
      RustWebSocketClient wsClient = new RustWebSocketClient(URI.create("ws://127.0.0.1:28016/password"), eventBus);
      
      // 2. Instantiation
      DefaultRustRconClient client = new DefaultRustRconClient(wsClient, eventBus);
      
      // 3. Connection (register listeners on the EventBus internally)
      client.connect();
      
      // 4. Manual request creation (note: you must manage the ID here if you don't use the Router)
      RustRconRequest request = new RustRconRequest() {
        @Override public int getIdentifier() { return 12345; }
        @Override public String getMessage() { return "status"; }
      };
      
      // 5. Sending and handling the response
      client.send(request).thenAccept(response -> {
        System.out.println("Raw Response: " + response.getMessage());
      });
      
      // 6. Closing
      client.close();
   }
}
```

<hr>

<h3> What should be used between DefaultRustRconService, DefaultRustRconRouter, and DefaultRustRconClient? </h3>

The choice depends on the level of abstraction and control you need for your application. Here's a quick guide to help you decide:

1. **`DefaultRustRconClient`**: This is the lowest level. It only handles the raw socket connection. It's too primitive to handle complex business logic (such as request/response correlation).
2. **`DefaultRustRconRouter`** (the file you're viewing): This is the communication "engine." As you can see from the code, it manages the `EventBus`, the correlation between requests and responses via IDs (`identifierGenerator`), and returns `CompletableFuture`. It's essential for asynchronous operation, but is intended to be used by a higher level.
3. **`DefaultRustRconService`**: This is the highest-level abstraction. It typically encapsulates the `Router` and provides ready-made utility methods (e.g., getting the player list, server info, etc.) that you need for the dashboard.

### 1. DefaultRustRconService (Recommended in 95% of cases)
This is the "turnkey" solution. You should use this class if you're building a bot, a dashboard, or a standard management tool.

* **Why use it:**
* Automatically manages the connection lifecycle.
* Performs **automatic polling** (updates player list, server info, team in the background).
* Maintains a **local cache** of data for quick access without network calls.
* Offers high-level APIs (e.g., `playerManagement().kick(...)` instead of writing raw commands).
* **Hierarchy:** Internally, it instantiates and manages both the *Router* and the *Client*.

### 2. DefaultRustRconRouter (Intermediate Level)
You should use this class if you want lightweight control but aren't interested in the Service's automatic features.

* **Why use it:**
* You only need to send specific RCON commands and receive the asynchronous response (`CompletableFuture`).
* You want to avoid the overhead of continuous polling (you don't want the app to ask for the player list every 3 seconds).
* You want to manually manage when to send the commands.
* **What it does:** Handles Request-Response correlation (packet ID) and serialization, but doesn't maintain state (no player cache).

### 3. DefaultRustRconClient (Low Level)
You should use this class **very rarely**, usually only if you're extending the library itself.

* **Why use it:**
* You need to implement completely custom routing logic.
* Need direct access to WebSocket or raw connection events?
* **What it does:** It's just an adapter that transforms Java objects to JSON and vice versa over a WebSocket. It doesn't know how to associate a response with the request that generated it.

### Summary
* Want a complete and easy app? Use **`DefaultRustRconService`**.
* Want to just send raw commands without automation? Use **`DefaultRustRconRouter`**.

<hr>

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