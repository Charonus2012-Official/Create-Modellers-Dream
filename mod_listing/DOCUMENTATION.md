# Create: Modellers Dream Documentation

## 1. Introduction

**Create: Modellers Dream** is a NeoForge addon for the Create mod that adds smart train dispatch and control. Instead of manually programming complex schedules, players can use the **Dispatcher Table** and **Track Connectors** to wirelessly select trains and route them between named stations.

### Core Design Philosophy

- **No custom pathfinding** — reuses Create's native signal system for collision avoidance
- **No mixins required** — pure event-based integration with Create's schedule API
- **Wireless routing** — frequency-based network linking (Train Connector network IDs)
- **Live schedule mutation** — append destinations to active trains without resetting progress

### Requirements

- Minecraft 1.21.1
- NeoForge 54.2.0+
- Create mod 6 or later

---

## 2. For Players

## Blocks

#### Track Connector
Marks a station or connection point on your rail network. Place it on or adjacent to a rail block.

**Placing (Two-Click System):**
1. **First click** — click on a rail block (the connection point)
2. **Second click** — click on an adjacent block (any block, defines connection orientation)
3. A connection icon appears on the rail to show the link is active

**How it Works:**
- All Track Connectors sharing the same frequency form a **network**
- The network's frequency defines which Dispatcher Table can control it
- When you dispatch from a Dispatcher Table, it queries all stations reachable from that network's graph
- Create's signal system automatically handles collision avoidance

#### Dispatcher Table
The control center for train routing. Place it anywhere (not on track). Right-click to open a GUI where you can:
- **Select a train** from a dropdown of all loaded trains in the world
- **Direct Route** — wipe the train's schedule and send it to a single station
- **Append Route** — add a station to the end of the train's current schedule (if running)

**Placing & Linking**
- Craft with crafting recipe or find in creative tab
- Right-click with it the Track Connector to link to a network and place
- Wrench to rotate facing direction (cosmetic)

## Workflow

1. **Build track** — standard Create railway with signals (optional but recommended for safety)
2. **Place stations** — use Create's Station blocks at key locations
3. **Place Track Connectors** — click on a rail at each station
4. **Place Dispatcher Table** — anywhere in your base
5. **Link Dispatcher to network** — right-click with Track Communicator
6. **Dispatch trains** — select train, choose destination, press "Direct Route" or "Append Route"

## Tips

- **Trains need conductors** — seat a conductor with an empty schedule in forward or backward position before dispatching
- **Multiple trains at once** — each Dispatcher Table can send different trains to different places simultaneously
- **Append vs Direct** — use Append to chain destinations; Direct Route resets and starts fresh
- **Signal blocks are your friend** — use them to prevent collisions on shared single-track sections
- **Frequency matching** — all Track Connectors on the same frequency form one network; use different frequencies to split networks

---

## 3. For Developers

### Architecture Overview

The mod is split into several key layers:

```
DispatcherScreen / DispatcherMenu (Client/Server GUI)
    ↓
DispatchCommand (Command handler)
    ↓
DispatchManager (Schedule mutation logic)
    ↓
Create's ScheduleRuntime & Train API
```

### Core Classes

#### DispatchManager

**Location:** `dispatch/DispatchManager.java`

Central authority for all train schedule manipulation. This is the **only place** that touches `Train.runtime` directly.

**Key Methods:**

```java
public static void setDestination(Train train, String stationName)
```
Wipes any existing schedule and dispatches the train to a single station. Calls `train.runtime.setSchedule()` (which zeroes `currentEntry`), so only use this for fresh starts or when you know the previous schedule completed.

```java
public static void appendDestination(Train train, String stationName)
```
Adds a station to the end of the train's run. If no active schedule exists, falls back to `setDestination()`. **Important:** always appends `-1` to `train.runtime.predictionTicks` in lockstep with the new entry, or the next tick crashes with `IndexOutOfBoundsException`.

```java
public static List<String> getPendingStations(Train train)
```
Returns the list of queued stations for this train. Syncs automatically against `runtime.currentEntry` to drop completed stations.

```java
public static boolean hasActiveSchedule(Train train)
public static boolean hasSchedule(Train train)
public static boolean hasConductor(Train train)
public static Train findTrainByName(String name)
```
Query methods for schedule state and train lookups.

**Critical Quirks (from Create source):**

1. **`ScheduleRuntime.setSchedule()` calls `reset()`** — This zeroes `currentEntry`, so calling it on an already-running train replays its entire schedule. Only call `setSchedule()` for fresh starts; use in-place mutation of `schedule.entries` for mid-run appends.

2. **Empty schedules crash** — Create's `getWaitingStatus()` does `schedule.entries.get(currentEntry)` without bounds checking. This is handled by `ScheduleRuntimeMixin` (see Mixins below).

3. **Entries need condition columns** — A `ScheduleEntry` with zero condition columns never advances `currentEntry`. Always include at least one (even an empty `ScheduledDelay` column), which passes instantly.

4. **`predictionTicks` parallel list** — `ScheduleRuntime.predictForEntry()` indexes this list. If `schedule.entries.size() != predictionTicks.size()`, an `IndexOutOfBoundsException` fires on the next tick. Always append `-1` (a TBD sentinel) when adding entries in-place.

#### DispatcherTableBlockEntity

**Location:** `block/entity/DispatcherTable/DispatcherTableBlockEntity.java`

GUI control block. Stores:
- `networkId` (UUID) — unique ID of the linked Track Connector network
- `linkedConnectorPos` (BlockPos) — physical location of the Track Connector
- `linkedConnectorDimension` (ResourceKey) — dimension of the Track Connector
- `availableTrains`, `availableStations` — cached lists for the GUI

**Key Methods:**

```java
public TrackConnectorQuery.QueryResult queryLinkedNetwork()
```
Resolves the linked Track Connector, validates the UUID match, and queries its graph via `TrackConnectorQuery`. Returns an immutable result with train/station lists. Called server-side when the Dispatcher screen opens.

```java
public void refreshAvailableNetworkData()
```
Refreshes the cached train/station lists and syncs to the client. Call this whenever the network topology changes or the screen re-opens.

```java
public void logLinkedNetwork()
```
Diagnostic method that logs every step of resolution (connector found, graph resolved, stations enumerated, etc.). Useful for debugging link failures.

#### TrackConnectorBlockEntity

**Location:** `block/entity/TrackConnector/TrackConnectorBlockEntity.java`

Placed on/near rails. Stores:
- `networkId` (UUID) — shared ID for all connectors in the same network
- `targetTrack` (TargetTrack) — relative offset to the rail this connector points to

**Purpose:**
Acts as a query anchor. When the Dispatcher Table calls `queryLinkedNetwork()`, it resolves this connector's `targetTrack` to a physical rail position, then calls `Create.RAILWAYS.sided(level).getGraph(level, pos)` to fetch the track graph.

**Graph Resolution Pattern (from TrackConnectorQuery):**

```java
public static TrackGraph resolveGraph(ServerLevel level, TargetTrack target) {
    TrackNodeLocation trackLoc = target.resolve(level);  // Relative → absolute
    if (trackLoc == null) return null;
    
    return Create.RAILWAYS.sided(level).getGraph(level, trackLoc.blockPos());
}
```

**Critical:** Never cache graph IDs. Graphs merge and split live as players build/break track. Always re-resolve on every query.

#### TrackConnectorQuery

**Location:** `block/entity/TrackConnector/TrackConnectorQuery.java`

Static utility class that handles graph resolution and enumeration.

```java
public static QueryResult query(TrackConnectorBlockEntity connector, ServerLevel level)
```

Returns an immutable `QueryResult`:
```java
public record QueryResult(
    UUID graphId,
    List<Train> trains,
    List<GlobalStation> stations
) { ... }
```

This is what the Dispatcher Table uses to populate its dropdowns. `trains` and `stations` are **immutable snapshots**—safe to cache briefly for GUI purposes, but always re-query before dispatching.

#### DispatchCommand

**Location:** `command/DispatchCommand.java`

Registers the `/dispatch` command tree. Currently supports:
- `/dispatch select <train>` — selects a train (primarily for debugging)
- `/dispatch goto <station>` — direct route to a station
- `/dispatch append <station>` — append a station to the active schedule

Parses arguments and delegates to `DispatchManager` methods. Does **not** touch `Train.runtime` directly.

### Mixins

**Location:** `mixin/`

#### ScheduleRuntimeMixin

Patches Create's `ScheduleRuntime` to handle empty schedules gracefully.

**Patches:**

1. **`setSchedule()` tail** — If `schedule.entries` is empty after `setSchedule()` completes, set:
   - `currentEntry = 0`
   - `paused = true`
   - `completed = true`
   
   This prevents `getWaitingStatus()` from crashing on the next tick.

2. **`getWaitingStatus()` head** — If `schedule.entries` is empty, return `Component.empty()` and cancel the original method.

3. **Prediction code** — Guard against empty entries lists in prediction calculations.

#### ScheduleItemMixin

Patches Schedule deserialization (NBT handling). Ensures backwards compatibility with old schedule formats.

### Screens

#### DispatcherScreen & DispatcherMenu

**Location:** `screen/custom/DispatcherTable/`

Client-side GUI and server-side menu container.

**DispatcherScreen features:**
- Train dropdown (scrollable list of all trains)
- Station dropdown (scrollable list of reachable stations from the network)
- "Append Route" button (IconButton with ADD icon) — appends the selected station
- "Direct Route" button (IconButton with CONFIRM icon) — replaces the schedule with a single destination

**Syncing:**
Currently uses the `DispatcherTableBlockEntity`'s cached `availableTrains` and `availableStations`. For larger networks, consider implementing a custom payload packet to avoid re-querying on every frame.

### Network Architecture (Future)

The mod currently syncs train/station data via block entity NBT. For real-time multi-user scenarios, a custom packet system is planned:

```
Payload: QueryNetworkRequest { dispatcherPos, dimension }
  ↓ (server receives)
DispatchManager.queryNetwork(dispatcher)
  ↓ (server sends back)
Payload: QueryNetworkResponse { trains[], stations[] }
  ↓ (client receives)
DispatcherScreen.updateLists()
```

This allows the Dispatcher Table to reflect live changes without block updates.

### Extending the Mod

#### Adding a new dispatch strategy

1. Add a method to `DispatchManager`:
   ```java
   public static void customStrategy(Train train, List<String> stations) {
       // Build entries
       // Call setDestination() or appendDestination() for each
   }
   ```

2. Register a command or listen to an event that calls your method.

#### Querying a network without dispatching

Use `TrackConnectorQuery.query()` directly:
```java
ServerLevel level = /* ... */;
TrackConnectorBlockEntity connector = /* ... */;
TrackConnectorQuery.QueryResult result = TrackConnectorQuery.query(connector, level);

result.trains.forEach(train -> { /* ... */ });
result.stations.forEach(station -> { /* ... */ });
```

#### Integrating with other mods

If you want to dispatch trains from another mod:
1. Call `DispatchManager.findTrainByName(name)` to get the Train
2. Call `DispatchManager.setDestination(train, stationName)` or `appendDestination(train, stationName)`

No other integration is needed—DispatchManager handles all Create schedule quirks.

### Testing

**Manual test scenario:**
1. Place 2 stations, 2 Track Connectors, 1 Dispatcher Table
2. Add a signal block between stations on a single-track section
3. Dispatch two trains simultaneously to opposite stations
4. Observe both trains running without collision (signal system handles it)

**Edge cases to verify:**
- Appending to a completed schedule (should create a fresh one)
- Appending while train is mid-schedule (should queue next destination)
- Empty schedules (Mixin should prevent crashes)
- Dispatcher in different dimension from connector (should handle gracefully)

---

## 4. Troubleshooting

### Train doesn't move
- **Check:** Does the train have a conductor seated? (`DispatchManager.hasConductor()` must return true)
- **Check:** Is the Dispatcher Table linked to the correct Track Connector? (use `logLinkedNetwork()`)
- **Check:** Does the destination station exist in the graph? (visible in the Dispatcher screen dropdown)

### Server crashes with IndexOutOfBoundsException
- **Cause:** Usually `predictionTicks` list out of sync with `schedule.entries`
- **Fix:** Ensure `DispatchManager.appendDestination()` is called (not manual entry addition)

### "Cannot find connector" error
- **Cause:** Dispatcher Table and Track Connector are in different dimensions
- **Fix:** Use the Track Communicator in the same dimension

### Graph not resolving
- **Cause:** Track Connector's rail position is unloaded or the rail was deleted
- **Fix:** Re-place the Track Connector on an active rail

---

## 5. API Reference

### DispatchManager (Static Utility)

| Method | Signature | Notes |
|--------|-----------|-------|
| `setDestination` | `void setDestination(Train, String)` | Wipe & dispatch to single station |
| `appendDestination` | `void appendDestination(Train, String)` | Queue next destination (or start fresh if no schedule) |
| `getPendingStations` | `List<String> getPendingStations(Train)` | Queued stations (auto-syncs with runtime) |
| `findTrainByName` | `Train findTrainByName(String)` | Case-insensitive lookup |
| `hasConductor` | `boolean hasConductor(Train)` | True if train is controllable |
| `hasActiveSchedule` | `boolean hasActiveSchedule(Train)` | True if schedule exists & not completed |

### TrackConnectorQuery (Static Utility)

| Method | Signature | Notes |
|--------|-----------|-------|
| `query` | `QueryResult query(TrackConnectorBlockEntity, ServerLevel)` | Enumerate trains & stations in the network |
| `resolveGraph` | `TrackGraph resolveGraph(ServerLevel, TargetTrack)` | Fetch Create's graph for a rail position |

### QueryResult (Immutable Record)

```java
public record QueryResult(
    UUID graphId,
    List<Train> trains,
    List<GlobalStation> stations
) { }
```

---

## 6. License & Credits

- **Built for:** Create mod by Creators-of-Create
- **Platform:** NeoForge 1.21.1
- **License:** MIT License (see LICENSE file)
