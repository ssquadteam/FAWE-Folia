package com.fastasyncworldedit.bukkit.adapter;

import com.fastasyncworldedit.core.extent.processor.lighting.Relighter;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Relighter for regionised servers whose adapter has no server-side (starlight) relighter. It does nothing.
 *
 * <p>The adapters without a starlight relighter fall back to {@code NMSRelighter}, which is not usable on Folia: it reads
 * and writes the light engine's data layers directly, which under starlight are copies, so its block light propagation
 * never converges. It floods outwards until the heap is full, and the edit never finishes.</p>
 *
 * <p>Nothing has to be sent from here either: on a regionised server each chunk is sent from its own region as soon as
 * it is written (see {@code AbstractBukkitGetBlocks#handleCallFinalizer}). Light of edited chunks is left as the server
 * had it and is corrected by the server's own engine on later block updates.</p>
 */
public class FoliaRelighter implements Relighter {

    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public boolean addChunk(int cx, int cz, byte[] skipReason, int bitmask) {
        return true;
    }

    @Override
    public void addLightUpdate(int x, int y, int z) {
    }

    @Override
    public void fixLightingSafe(boolean sky) {
    }

    @Override
    public void clear() {
    }

    @Override
    public void removeLighting() {
    }

    @Override
    public void fixBlockLighting() {
    }

    @Override
    public void fixSkyLighting() {
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public ReentrantLock getLock() {
        return lock;
    }

    @Override
    public boolean isFinished() {
        return true;
    }

    @Override
    public void close() {
    }

}
