package com.fastasyncworldedit.bukkit.adapter;

import com.fastasyncworldedit.core.extent.processor.lighting.NMSRelighter;
import com.fastasyncworldedit.core.extent.processor.lighting.Relighter;
import com.fastasyncworldedit.core.queue.IQueueExtent;
import com.fastasyncworldedit.core.util.MathMan;
import com.fastasyncworldedit.core.util.TaskManager;
import com.github.ssquadteam.fawe.scheduler.FaweScheduler;
import com.github.ssquadteam.fawe.scheduler.RegionSync;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * A base class for version-specific implementations of the starlight relighting mechanism
 *
 * @param <SERVER_LEVEL> the version-specific ServerLevel type
 * @param <CHUNK_POS>    the version-specific ChunkPos type
 * @since 2.8.2
 */
public abstract class StarlightRelighter<SERVER_LEVEL, CHUNK_POS> implements Relighter {

    protected static final Logger LOGGER = LogManagerCompat.getLogger();

    private static final int CHUNKS_PER_BATCH = 1024; // 32 * 32
    private static final int CHUNKS_PER_BATCH_SQRT_LOG2 = 5; // for shifting

    private final ReentrantLock lock = new ReentrantLock();
    private final Long2ObjectLinkedOpenHashMap<LongSet> regions = new Long2ObjectLinkedOpenHashMap<>();
    private final ReentrantLock areaLock = new ReentrantLock();
    private final NMSRelighter delegate;
    protected final SERVER_LEVEL serverLevel;
    //FAWE-Folia start - the world is needed to reach each chunk's region
    private volatile org.bukkit.World foliaWorld;

    /**
     * Set the Bukkit world of this relighter. Required on a regionised server, where every chunk is relit from its region.
     */
    public void setFoliaWorld(org.bukkit.World world) {
        this.foliaWorld = world;
    }
    //FAWE-Folia end

    protected StarlightRelighter(SERVER_LEVEL serverLevel, IQueueExtent<?> queue) {
        this.serverLevel = serverLevel;
        this.delegate = new NMSRelighter(queue);
    }

    protected Set<CHUNK_POS> convertChunkKeysToChunkPos(LongSet chunks) {
        // convert from long keys to ChunkPos
        Set<CHUNK_POS> coords = new HashSet<>();
        LongIterator iterator = chunks.iterator();
        while (iterator.hasNext()) {
            coords.add(createChunkPos(iterator.nextLong()));
        }
        return coords;
    }

    protected abstract CHUNK_POS createChunkPos(long chunkKey);

    protected abstract long asLong(int chunkX, int chunkZ);

    protected abstract CompletableFuture<?> chunkLoadFuture(CHUNK_POS pos);

    protected List<CompletableFuture<?>> chunkLoadFutures(Set<CHUNK_POS> coords) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (final CHUNK_POS coord : coords) {
            futures.add(chunkLoadFuture(coord));
        }
        return futures;
    }

    @NotNull
    protected IntConsumer postProcessCallback(Runnable andThen, Set<CHUNK_POS> coords) {
        return i -> {
            if (i != coords.size()) {
                LOGGER.warn("Processed {} chunks instead of {}", i, coords.size());
            }
            // post process chunks on main thread
            TaskManager.taskManager().task(() -> postProcessChunks(coords));
            // call callback on our own threads
            TaskManager.taskManager().async(andThen);
        };
    }

    protected abstract void invokeRelight(
            Set<CHUNK_POS> coords,
            Consumer<CHUNK_POS> chunkCallback,
            IntConsumer processCallback
    );

    protected abstract void postProcessChunks(Set<CHUNK_POS> coords);

    /*
     * Processes a set of chunks and runs an action afterwards.
     * The action is run async, the chunks are partly processed on the main thread
     * (as required by the server).
     */
    protected void fixLighting(LongSet chunks, Runnable andThen) {
        //FAWE-Folia start - relight every chunk from its own region
        if (FaweScheduler.isFolia()) {
            fixLightingFolia(chunks, andThen);
            return;
        }
        //FAWE-Folia end
        Set<CHUNK_POS> coords = convertChunkKeysToChunkPos(chunks);
        TaskManager.taskManager().task(() -> {
            // trigger chunk load and apply ticket on main thread
            List<CompletableFuture<?>> futures = chunkLoadFutures(coords);
            // collect futures and trigger relight once all chunks are loaded
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept(v ->
                    invokeRelight(
                            coords,
                            c -> {
                            }, // no callback for single chunks required
                            postProcessCallback(andThen, coords)
                    )
            );
        });
    }

    //FAWE-Folia start - relight every chunk from its own region
    /*
     * There is no thread that may touch a whole batch on a regionised server, so each loaded chunk is handed to the
     * server's relight on its own, from the region owning it. The server tickets the chunk, relights it on its light
     * executor and sends the light packets by itself. Chunks that are not loaded are skipped: nobody sees them, and
     * scheduling on them would load them.
     */
    private void fixLightingFolia(LongSet chunks, Runnable andThen) {
        org.bukkit.World world = this.foliaWorld;
        if (world == null) {
            LOGGER.warn("Cannot relight {} chunk(s): the relighter was not given its world", chunks.size());
            TaskManager.taskManager().async(andThen);
            return;
        }
        final int total = chunks.size();
        final AtomicInteger pending = new AtomicInteger(total);
        final AtomicInteger relit = new AtomicInteger();
        final AtomicInteger skipped = new AtomicInteger();
        final Runnable chunkDone = () -> {
            if (pending.decrementAndGet() == 0) {
                LOGGER.debug("Relit {} of {} chunk(s) of world {} ({} not loaded)", relit.get(), total, world.getName(),
                        skipped.get());
                TaskManager.taskManager().async(andThen);
            }
        };
        LOGGER.debug("Relighting {} chunk(s) of world {}", total, world.getName());
        LongIterator iterator = chunks.iterator();
        while (iterator.hasNext()) {
            final long key = iterator.nextLong();
            // The keys come from asLong, which packs like ChunkPos in every version: x in the low half, z in the high one
            final int x = (int) key;
            final int z = (int) (key >>> 32);
            try {
                if (!world.isChunkLoaded(x, z)) {
                    skipped.incrementAndGet();
                    chunkDone.run();
                    continue;
                }
                RegionSync.dispatch(world, x << 4, 0, z << 4, () -> {
                    try {
                        Set<CHUNK_POS> single = new HashSet<>();
                        single.add(createChunkPos(key));
                        invokeRelight(single, c -> {
                        }, i -> {
                            relit.addAndGet(i);
                            chunkDone.run();
                        });
                    } catch (Throwable t) {
                        LOGGER.error("Could not relight chunk {},{} of world {}", x, z, world.getName(), t);
                        chunkDone.run();
                    }
                });
            } catch (Throwable t) {
                LOGGER.error("Could not schedule relighting chunk {},{} of world {}", x, z, world.getName(), t);
                chunkDone.run();
            }
        }
    }
    //FAWE-Folia end

    @Override
    public boolean addChunk(int cx, int cz, byte[] skipReason, int bitmask) {
        areaLock.lock();
        try {
            // light can go into neighboring chunks, make sure they are relighted too.
            for (int x = cx - 1; x <= cx + 1; x++) {
                for (int z = cz - 1; z <= cz + 1; z++) {
                    long key = MathMan.pairInt(x >> CHUNKS_PER_BATCH_SQRT_LOG2, z >> CHUNKS_PER_BATCH_SQRT_LOG2);
                    LongSet chunks = this.regions.computeIfAbsent(key, k -> new LongArraySet(CHUNKS_PER_BATCH >> 2));
                    chunks.add(asLong(x, z));
                }
            }
        } finally {
            areaLock.unlock();
        }
        return true;
    }

    /*
     * This method is called "recursively", iterating and removing elements
     * from the regions linked map. This way, chunks are loaded in batches to avoid
     * OOMEs.
     */

    @Override
    public void fixLightingSafe(boolean sky) {
        this.areaLock.lock();
        try {
            if (regions.isEmpty()) {
                return;
            }
            LongSet first = regions.removeFirst();
            fixLighting(first, () -> fixLightingSafe(true));
        } finally {
            this.areaLock.unlock();
        }
    }

    @Override
    public void addLightUpdate(int x, int y, int z) {
        this.delegate.addLightUpdate(x, y, z);
    }

    @Override
    public void clear() {

    }

    @Override
    public void removeLighting() {
        this.delegate.removeLighting();
    }

    @Override
    public void fixBlockLighting() {
        fixLightingSafe(true);
    }

    @Override
    public void fixSkyLighting() {
        fixLightingSafe(true);
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public ReentrantLock getLock() {
        return this.lock;
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public void close() throws Exception {
        fixLightingSafe(true);
    }

}
