# Folia Support

This fork adds Folia support to FastAsyncWorldEdit. It carries no third-party scheduler
library: everything goes through an in-plugin scheduler under
`com.github.ssquadteam.fawe.scheduler` in the `worldedit-bukkit` module.

## Why an in-plugin scheduler

The fork previously shaded [FoliaLib](https://github.com/TechnicallyCoded/FoliaLib). That
meant an extra maven repository, an extra shade/relocate rule, and a dependency whose bugs
were not fixable on this fork's timeline - all for a handful of methods. The in-plugin
scheduler exposes only what FAWE actually uses, and it is one place to fix when Folia's API
moves.

## Layout

| Type | Role |
| --- | --- |
| `FaweScheduler` | Static entry point. Detects Folia once, builds the right implementation on enable, cancels everything on disable. |
| `PlatformScheduler` | The scheduler contract, grouped by *what a task touches*. |
| `SchedulerTask` | Platform-independent cancellable task handle. |
| `BukkitPlatformScheduler` | Bukkit / Spigot / non-regionised Paper. Everything maps onto the main thread. |
| `FoliaScheduler` | Folia's region, entity, global, and async schedulers. |
| `FoliaSchedulerFactory` | The only reference to `FoliaScheduler`, so Spigot never loads a class that links against Folia-only API. |
| `RegionSync` | The blocking "give me this value from the right thread" helper, plus fire-and-forget `dispatch`. |

## Ownership rules

Folia has no single main thread. A task's owner is decided by what it *touches*, not by
where it was started.

- **Location owned** - blocks, chunks, world state at a point: `runAtLocation`,
  `runAtLocationTimer`, `supplyAtLocation`, or `RegionSync.supply` / `RegionSync.dispatch`.
- **Entity owned** - state belonging to one entity, following it across regions and worlds:
  `runAtEntity`, `supplyAtEntity`, `RegionSync.supplyAtEntity`.
- **Global** - server-wide state and plugin lifecycle: `runNextTick`, `runLater`, `runTimer`.
- **No server state** - blocking IO, compression, CPU work: `runAsync`, `runLaterAsync`,
  `runTimerAsync`. Hop back to the correct owner before touching anything.

`runAtLocation` and `runAtEntity` run the task inline when the calling thread already owns
the target. That is deliberate: without it, a caller that blocks on the result would be
waiting on its own region thread.

## What the fork changes

Detection is capability based - the presence of any of several regionised marker classes -
and cached once in `FaweScheduler`. More than one marker is checked so that Folia's forks,
such as Canvas, are recognised too: they inherit the regionised internals but are free to
move any single class, and a check hinging on one name would silently treat such a server as
ordinary, putting FAWE back on the wrong thread. `Fawe.setFoliaServer` mirrors the result
into `worldedit-core`, which has no Bukkit classes of its own.

**Core (`worldedit-core`)**

- `Fawe` - carries the `isFoliaServer` flag.
- `QueueHandler` - the "not main thread" assertion is skipped on Folia, and sync tasks are
  always queued for the tick loop rather than run inline, because the thread FAWE captured at
  startup owns no particular region.
- `TaskManager` - `sync` and `syncWhenFree` run on the calling thread on Folia. The tick loop
  draining the sync queue runs on the global region thread, so a caller already there would
  otherwise block waiting for itself. Call sites needing a specific owner schedule against
  that owner directly instead.
- `PlatformManager` - tool actions stay on the region thread that fired the interaction event
  rather than being pushed onto an async task.

**Platform (`worldedit-bukkit`)**

- `WorldEditPlugin` - initialises the scheduler before FAWE starts, cancels through it on
  disable.
- `BukkitTaskManager` - reimplemented over `PlatformScheduler`. It now keeps a real task-id
  registry, so `cancel(int)` works on both platforms.
- `AbstractBukkitGetBlocks` - the single place all adapters funnel their chunk sync work
  through. On Folia the whole chain runs on the region owning that chunk, which is why the
  version adapters need almost no Folia code of their own.
- `BukkitWorld`, `BukkitEntity`, `BukkitEntityProperties`, `BukkitPlayer`,
  `BukkitPlayerBlockBag`, `BukkitBlockCommandSender`, `BukkitServerInterface`, `FaweAdapter`,
  `IBukkitAdapter`, `Regenerator` - each routes its server-state access to the correct owner.
- `ChunkListener` and `RenderListener` - see the behaviour differences below.

**Version adapters**

- `PaperweightFaweWorldNativeAccess` - block writes go to the region owning each chunk. This
  one is easy to miss: the writes sit behind `TaskManager#sync`, which runs *inline* on
  Folia, so the unpatched path mutates the world straight from a FAWE worker thread.

- `PaperweightFaweAdapter` - feature, structure, and tree generation run on the target
  block's region.
- `PaperweightGetBlocks` - beacon block entity removal runs on the beacon's region; supplies
  the Bukkit world for the shared chunk routing above.
- `PaperweightPlatformAdapter` - chunk tickets, chunk packets, and the fallback chunk load in
  `ensureLoaded` run on the chunk's region instead of the server tick executor.

## Behaviour differences on Folia

- **Teleports are asynchronous.** Folia can only move an entity across regions
  asynchronously, so `BukkitPlayer` and `BukkitEntity` use `teleportAsync` and report success
  optimistically instead of blocking for a result.
- **Command block session state refreshes one call late.** `BukkitBlockCommandSender`
  schedules the refresh on the block's region and returns the previously known value, which
  matches what the Bukkit path already did off the main thread.
- **`Platform#schedule` returns a placeholder id** on Folia. Nothing in WorldEdit cancels
  through that id; callers only test it against `-1`.
- **The tick limiter is off.** `ChunkListener`'s counters are plain hash maps written
  straight from `BlockPhysicsEvent`, `EntityChangeBlockEvent` and `ItemSpawnEvent`, which
  fire on many region threads at once. It is deprecated and untouched since 1.12, so it is
  disabled with a warning rather than made thread-safe. Set `tick-limiter.enabled` to
  `false` to silence the warning.
- **Lighting uses FAWE's own relighter, not starlight.** `StarlightRelighter` batches up to
  32x32 chunks - more than one region - and drives the server light engine for the whole
  batch from the global region thread, which cannot be split by owner. Expect relighting to
  behave as it does on a server without the starlight relighter available.
- **Dynamic chunk rendering hops threads.** `RenderListener`'s timer walks every player from
  the global region thread, so each `setViewDistance` is dispatched to its own player and
  applies a tick or so later than it would elsewhere.

## Adding Folia-aware code

1. Decide what the work touches, then pick the matching method - do not reach for
   `runNextTick` because it looks like "the main thread".
2. Need a value back? Use `RegionSync`. Do not hand-roll a `CompletableFuture` plus scheduler
   call; that shape exists once on purpose.
3. Fire and forget at a block position? Use `RegionSync.dispatch`, which runs inline off
   Folia and so leaves non-Folia behaviour exactly as upstream wrote it.
4. Mark the change with `//FAWE-Folia start` / `//FAWE-Folia end`, matching the surrounding
   `//FAWE start` convention, so the fork's delta stays greppable at merge time.
5. Never reformat the upstream code around your change.

## What has and has not been checked

Every Bukkit-module source touching server state has been read for main-thread assumptions,
not just searched for one call shape - the dangerous ones turned out to mention no scheduler
at all, being plain Bukkit calls on a thread that used to be the main one.

None of it has run on a server. The build is green and the ownership reasoning is written
down above, but no block has been placed on a regionised server, and Folia's own thread
checks are a far better oracle than reading. Treat a first run on Canvas or Folia as the
real test, and read the log for the two startup lines this fork adds: the relighter factory
it chose, and the tick limiter warning if that setting is on.
