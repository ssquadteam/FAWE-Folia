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

Detection is capability based - the presence of Folia's `RegionizedServer` class - and cached
once in `FaweScheduler`. `Fawe.setFoliaServer` mirrors it into `worldedit-core`, which has no
Bukkit classes of its own.

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
  `BukkitBlockCommandSender`, `BukkitServerInterface`, `FaweAdapter`, `IBukkitAdapter`,
  `Regenerator` - each routes its server-state access to the correct owner.

**Version adapters**

- `PaperweightFaweAdapter` - feature, structure, and tree generation run on the target
  block's region.
- `PaperweightGetBlocks` - beacon block entity removal runs on the beacon's region; supplies
  the Bukkit world for the shared chunk routing above.
- `PaperweightPlatformAdapter` - chunk tickets and chunk packets run on the chunk's region
  instead of the server tick executor.

## Behaviour differences on Folia

- **Teleports are asynchronous.** Folia can only move an entity across regions
  asynchronously, so `BukkitPlayer` and `BukkitEntity` use `teleportAsync` and report success
  optimistically instead of blocking for a result.
- **Command block session state refreshes one call late.** `BukkitBlockCommandSender`
  schedules the refresh on the block's region and returns the previously known value, which
  matches what the Bukkit path already did off the main thread.
- **`Platform#schedule` returns a placeholder id** on Folia. Nothing in WorldEdit cancels
  through that id; callers only test it against `-1`.

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
