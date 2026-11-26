# FAWE-Folia: upstream sync + Minecraft 26.3

Branch `feat/26.3-support`, built on the fork's `main` (6d64dfd95) with upstream
IntellectualSites/FastAsyncWorldEdit `main` (c1da1171d) merged in. Read `FOLIA.md` first: this
file only describes what changed on top of it.

Nothing here has run on a regionised server. The build is green and the jar contains the
26.3 adapter, but that says nothing about runtime behaviour on Canvas/Folia. Use the
checklist at the end before putting it in production.

## What was done

1. **Merged upstream `main`** (79 commits, merge-base 0a3382e30). Upstream brings 26.3 support
   (#3661), the removal of PaperLib (#3630, replaced by `PaperSupport`), block-entity removal on
   the main thread (#3714), the new fake-block handling (#3367/#3716), lazy-copy fixes (#3520),
   palette fixes (#3688) and dependency/Gradle bumps.
2. **Fixed the startup crash.** The crash reported on Canvas 26.3
   (`ExceptionInInitializerError` from `WorldEditPlugin.onEnable`, caused by
   `NullPointerException: Cannot read the array length because "<local0>" is null`) was **not** a
   missing adapter. `WorldEditPlugin.java:241` on the fork's `main` is `FaweScheduler.init(this)`.
   `FaweScheduler` declared `FOLIA = detectFolia()` *above* the `REGIONISED_MARKERS` array, and
   static fields initialise in source order, so `detectFolia()` looped over a `null` array. That
   happens on **every** server and Minecraft version: the fork's `main` cannot enable anywhere.
   It came in with eb40e4591. Fixed by declaring the array first (separate commit).
3. **Ported the Folia patches to `adapter-26.3`**, the same four files and the same changes as
   in 26.2.
4. **Audited the upstream code that merged without conflicts** and fixed the one new off-region
   world read found (`BukkitPlayer#sendFakeBlock`, see below).

## Build

```
JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot"
./gradlew -Dorg.gradle.jvmargs=-Xmx4g -Dorg.gradle.configureondemand=false --max-workers=4 \
    :worldedit-bukkit:build :worldedit-core:test --continue
```

- The toolchain is Java 25 (`buildlogic.common`). The reobf adapters (1.21.x) also ask for a
  Java 21 launcher, which Gradle found locally.
- `-Dorg.gradle.configureondemand=false` is needed when invoking `:worldedit-bukkit:build`
  directly. With the repository's `configureondemand=true`, `reobfShadowJar` fails with
  "Extension of type 'JavaPluginExtension' does not exist", because it reads
  `:worldedit-core`'s source sets before that project is configured. That is a
  build-script problem from before this branch. `gradlew build` from the root avoids it too.
- Result: BUILD SUCCESSFUL. All 9 adapters compile. Tests: worldedit-core 182/182 and
  worldedit-bukkit 10/10 passed. There are no Folia tests.
- Output: `worldedit-bukkit/build/libs/FastAsyncWorldEdit-Paper-<version>.jar`, the one to use on
  Canvas, and `FastAsyncWorldEdit-Bukkit-<version>.jar`, the Spigot-remapped one. Both contain
  `impl/v26_3` and `impl/fawe/v26_3` and `folia-supported: true`.

## How each group of conflicts was resolved

| Files | Resolution |
| --- | --- |
| `.github/workflows/*` (5) | Kept the fork's state: those workflows were deleted in the fork, so they stay deleted. `release.yml` is untouched. |
| `build-logic/.../buildlogic.adapter.gradle.kts` | Took upstream's version: PaperMC repo declared explicitly, a FabricMC repo for yarn, EngineHub excludes `net.fabricmc:yarn`, no more `killNonEngineHubRepositories()`, and the reobf/mojmap choice moved into `buildlogic.adapter-reobf` / `buildlogic.adapter-mojmap`. Kept the fork's `exclusiveContent` block that pins `net.fabricmc` to `maven.fabricmc.net`, which it added so cold builds of adapter-1_21 could resolve. The fork's `afterEvaluate { killNonEngineHubRepositories() }` was dropped: upstream removed it, and it would strip the PaperMC repository that 26.3 needs now that `injectPaperRepository = false` is gone. |
| `PaperweightGetBlocks.java` (8 adapters, same hunk in all) | Upstream #3714 wrapped the "remove existing block entities" loop in `TaskManager.taskManager().sync(...)`, to avoid a ConcurrentModificationException on the chunk's block-entity map. The fork had wrapped only `removeBeacon` in `RegionSync.dispatch`. `TaskManager#sync` runs inline on Folia (see FOLIA.md), so taking upstream as-is would have kept the loop off-region on Folia. Resolution: upstream's structure, with the sync call replaced by `RegionSync.supply(world, chunkX << 4, 0, chunkZ << 4, ...)`. That is the chunk's own region on Folia, and exactly `TaskManager#sync` everywhere else, so non-Folia behaviour is unchanged. The fork's `RegionSync.dispatch` around `removeBeacon` is kept inside it. The beacon is in the same chunk, so on Folia that dispatch now just runs inline. The unused `TaskManager` import upstream added is removed. |
| `BukkitServerInterface.java` | Import-only conflict: kept both `PaperSupport` (upstream) and `FaweScheduler` (fork). |
| `BukkitWorld.java` | `clearContainerBlockContents`: upstream's `PaperSupport` / pattern-variable form, still cleared inside the fork's `RegionSync.supply`. `checkLoadedChunk`: kept the fork's Folia branch (always load asynchronously on Folia), with Paper's `World#getChunkAtAsync` in place of the removed `PaperLib.getChunkAtAsync`. |

The adapter files that merged without a textual conflict (`PaperweightFaweAdapter`,
`PaperweightFaweWorldNativeAccess`, `PaperweightPlatformAdapter` in all eight adapters) were
checked: every `//FAWE-Folia start` marker the fork had is still there, the same count per file.

## adapter-26.3

The Folia delta was taken from the merged 26.2 adapter (merged 26.2 compared with upstream 26.2),
so the 26.3 port carries exactly what 26.2 carries:

- `PaperweightFaweAdapter`: `generateFeature`, `generateStructure` and `generateTree` run
  through `RegionSync.supply` at the target block instead of `TaskManager#sync`.
- `PaperweightFaweWorldNativeAccess`: on Folia, each block write is dispatched to the region
  owning its chunk, in both `flushAsync` and `flush`, and chunk packets are sent through the
  patched `sendChunk`.
- `PaperweightGetBlocks`: the block-entity removal described above, the beacon dispatch, and
  `getBukkitWorld()` for `AbstractBukkitGetBlocks`' region routing.
- `PaperweightPlatformAdapter`: fallback chunk load in `ensureLoaded`, `addTicket` and
  `sendChunk` go to the chunk's region (with the original executor as the non-Folia fallback).

NMS differences between 26.2 and 26.3 that touch the patched code:

- `generateFeature` / list of features: 26.3 looks up `Registries.FEATURE` and `Feature`
  instead of `Registries.CONFIGURED_FEATURE` / `ConfiguredFeature` (upstream change). The Folia
  wrapper is unaffected.
- `generateStructure`: in 26.3 `Structure#generate` takes a climate sampler
  (`randomState.createClimateSampler(SamplerContext.EMPTY_UNCACHED)`) and the
  `StructureTemplateManager`. Because of this the patch context did not match and the hunk was
  applied with reduced context. It only replaces the `TaskManager.taskManager().sync(` line, so
  the body is upstream's 26.3 code unchanged (checked by hand).
- `sendChunk`: the `ClientboundLevelChunkWithLightPacket` constructor lost its trailing boolean
  (anti-xray flag) in Paper 26.3. Upstream already handles that. The fork's change only wraps
  the dispatch, so nothing to adapt.
- `PaperweightPlatformAdapter` resolves several NMS members via `Refraction.pickName(...)` in
  26.3 (Spigot support). The Folia patch does not touch those.

None of the NMS calls the Folia patches add are new: `setBlockState`, `getChunk`,
`addTicketWithRadius`, `getWorld()` and `getBlockPos()` were already used by upstream's 26.3
code in the same methods, and the adapter compiles against the 26.3 dev bundle.

Adapter registration: there is no fork-specific version table. Adapters are found by
`BukkitImplLoader` from the classes shaded into the jar. `adapter-26.3` is in
`settings.gradle.kts` (from upstream) and goes into both shadow jars through
`adaptersGlobalMojmap` in `worldedit-bukkit/build.gradle.kts`.

## Post-merge audit (upstream code that merged without conflicts)

Searched the upstream diff (worldedit-core, worldedit-bukkit, adapters) for Bukkit
scheduler use, `isPrimaryThread`/`isMainThread`, `TaskManager` sync, `MAIN_EXECUTOR`,
`MinecraftServer#execute`, and direct chunk/entity/block-entity access. Also read the
non-trivial changes by hand.

- **Fixed: `BukkitPlayer#sendFakeBlock(pos, null)`** (upstream #3367/#3716). Restoring a fake
  block now reads the real block *with its block entity NBT* through
  `getExtent().getFullBlock(pos)`, which goes to the adapter's `getFullBlock`
  (`getChunk` + `getBlockEntity` + `saveWithId`). Server-side CUI (`LocalSession#updateServerCUI`)
  calls this from command threads. On Folia the read now goes through `RegionSync.supply` at the
  position. Elsewhere it is unchanged.
- **No change needed:**
  - `PaperSupport` replaces PaperLib one for one. On Folia, `isPaper()` is true, because
    Folia/Canvas ship `io.papermc.paper.configuration.Configuration`.
  - `WorldCopyClipboard` now calls `QueueHandler#unCache`, which only touches a thread-local cache.
  - `sendFakeNBT` / `sendFakeOP` only send packets.
  - `ExtentEntityCopy`, the clipboard size changes, `YAMLProcessor`/NIO, `EngineHubPaste`,
    the palette fixes and the registry updates do not touch server state.
  - The adapters' `ensureLoaded` now calls `World#getChunkAtAsync` directly instead of through
    PaperLib. Folia allows that from any thread.
- **Not introduced by this merge, still there (written down, not changed):**
  - `BukkitWorld#getFullBlock` / `getBlock` and the adapters' `getFullBlock` read the world
    directly. FAWE's own edit path reads through the chunk queue instead (region-routed by
    `AbstractBukkitGetBlocks`), but plugins calling these on `BukkitWorld` from an async thread
    on Folia can still hit Folia's thread checks.
  - `BukkitWorld#clearContainerBlockContents` (fallback path, only when the adapter call fails)
    takes `block.getState(...)` *before* switching to the block's region, as the fork did before
    the merge.
  - `getEntities()` / `getEntities(region)` run `World#getEntities` on a single region: spawn,
    or the selection's minimum corner. On Folia that only sees what that region can see, so
    entity copy, `//copy -e` and `//remove` over a selection that crosses region boundaries can
    miss entities. This is the fork's existing design.

## Limited or unsupported on Folia

From FOLIA.md and the code, not from testing:

- **Teleports are asynchronous** and report success optimistically (`//up`, `//jumpto`,
  `//thru`, `//unstuck`, and anything else that calls `setPosition`).
- **Lighting:** edited chunks are sent as soon as they are written and relit afterwards through
  the server's own relight. Both were checked by hand on Canvas 26.3 with `//set`. See FOLIA.md.
- **Tick limiter is disabled** (warning in the log if `tick-limiter.enabled` is true).
- **`//regen`:** the drain timer runs on the region owning the selection's minimum corner. A
  regen over several regions still drives everything from that one region. It is the fork's
  design and has never been run.
- **Entities** (copy/paste with `-e`, `//remove*`, `//butcher`): see the single-region
  `getEntities` limitation above.
- **Command block session state** refreshes one call late. **`Platform#schedule`** returns a
  placeholder id.
- **Dynamic chunk rendering** applies `setViewDistance` a tick or so late.
- Edits that block on another region (`RegionSync.supply` from a region thread for a position
  owned by a different region) can stall the calling region for as long as the other one takes.
  `RegionSync` warns about this. Large tool/brush edits started from a region thread are where it
  would show.

## Risks spotted

1. **The block-entity removal now blocks a FAWE worker on the chunk's region** for every chunk
   that has block entities in the edited sections. Upstream does the same against the main
   thread. On Folia it is spread over region threads, but a region that is busy or stuck will
   hold back the FAWE workers waiting on it. If the server hangs during a big `//set` or paste,
   take a thread dump and look for FAWE threads parked in `RegionSync.join` inside
   `PaperweightGetBlocks.internalCall`.
2. **adapter-26.3 is new upstream code** ("TODO Check when Paper 26.3 approaches beta" in
   `sendChunk`). Bugs there would show on Paper too, not only on Folia.
3. **Canvas 26.3 b1047 versus the dev bundle.** The adapter compiles against Paper's 26.3 dev
   bundle (the build resolves `26.3.build.+`). If Canvas changed NMS internals that the
   adapter reaches by reflection (`PaperweightPlatformAdapter`'s static initialiser,
   `Refraction.pickName`), it will fail at startup with a reflection error in the log. That is
   not Folia-specific, and nothing here can check it.
4. **`folia-supported: true`** is in `plugin.yml`. There is no `paper-plugin.yml`.

## Test checklist for a real server

Use a staging copy of the network, not production. Keep the world backed up.

**Startup**

- [ ] Server starts with only this jar in `plugins/`, no `ExceptionInInitializerError`, and
      `/version FastAsyncWorldEdit` shows it enabled.
- [ ] The log shows the 26.3 adapter being picked (no "no adapter found" / "unsupported version"
      warnings), the relighter factory line from FOLIA.md, and the tick-limiter warning if that
      setting is on.
- [ ] No `ClassNotFoundException`, `NoSuchFieldException` or `NoSuchMethodException` during
      startup (reflection in `PaperweightPlatformAdapter`).

**Small selection (inside one region, for example 10x10x10 near spawn)**

- [ ] `//wand`: the wand is given and pos1/pos2 clicks register.
- [ ] `//set stone`, then `//set air`. Blocks change, clients see them without relogging, and
      nobody gets kicked.
- [ ] `//copy`, move, `//paste`, `//undo` (twice), `//redo`.
- [ ] Same with chests/signs/beacons in the selection: block entities removed and replaced,
      beacon beam updated, no duplicated items.
- [ ] With server-side CUI on (`//drawsel`), select and deselect. The structure block preview
      appears and is removed without errors (this is the `sendFakeBlock` path fixed in this
      branch).

**Selection that crosses several Folia regions**

Region size depends on the Folia/Canvas config (`grid-exponent`). Pick something like 600x50x600,
or larger than two region sections, and have players standing in different parts of it.

- [ ] `//set`, `//replace`, `//copy` + `//paste` somewhere far away, `//undo`.
- [ ] No hung regions: `/tps` (or Canvas's region report) keeps updating during and after the edit.
- [ ] Chunks at region borders are complete (no half-written chunks after relogging).
- [ ] `//copy -e` / `//paste -e` with a few armour stands or item frames spread across the
      selection. Write down which ones are missed: expected per the entity limitation above.

**Schematics through the API (arena resets every 5 minutes)**

- [ ] With the arena plugin installed, let at least 3–4 reset cycles run with players inside the
      arena and with no players nearby (unloaded chunks).
- [ ] Each reset completes, blocks and block entities match the `.schem`, and no reset overlaps
      the next one (watch for growing queue sizes or memory).
- [ ] Find out which thread the plugin calls FAWE from (async task, global region, region
      thread). Calls from a region thread that paste into a different region are the case most
      likely to stall.
- [ ] Two arenas resetting at the same moment in different regions.

**What to look for in the log**

- `Thread failed main thread check`, `thread check`, `Accessing ... off-region`,
  `IllegalStateException` mentioning region/ownership or `TickThread`: something touched the world
  from the wrong thread. Copy the full stack trace. The FAWE frame tells you which call site needs
  routing.
- `ConcurrentModificationException` in `PaperweightGetBlocks` or in a block-entity map.
- `Error performing chunk call at chunk` / `Error performing final chunk calling at` (FAWE's
  own wrapping of the above).
- Watchdog / "region ... not responding" dumps that mention FAWE threads or `RegionSync`.
- `Unable to retrieve block via impl adapter` (adapter failures falling back to Bukkit).
