package com.github.ssquadteam.fawe.scheduler;

import com.fastasyncworldedit.core.Fawe;
import org.bukkit.plugin.Plugin;

/**
 * Entry point to the scheduler FAWE runs its server-thread work through.
 *
 * <p>
 * Holds the single {@link PlatformScheduler} for the plugin and picks the
 * implementation at startup based on whether the server is regionised.
 * </p>
 *
 * <p>
 * Callers should reach for {@link #scheduler()} and then pick the method that
 * matches what their task touches; see {@link PlatformScheduler} for the
 * ownership rules.
 * </p>
 */
public final class FaweScheduler {

    /**
     * Whether this server regionises ticking.
     *
     * <p>
     * Resolved once from the presence of Folia's regionised server class rather
     * than from a version string, so it stays correct across forks and rebrands.
     * </p>
     */
    private static final boolean FOLIA = detectFolia();

    private static volatile PlatformScheduler scheduler;

    private FaweScheduler() {
    }

    /**
     * Create the platform scheduler for the plugin. Called once, on enable.
     *
     * @param plugin the plugin owning every scheduled task
     */
    public static void init(Plugin plugin) {
        if (scheduler != null) {
            return;
        }
        // The Folia implementation links against Folia-only API, so it is created
        // through a separate class that is never loaded on a non-Folia server.
        scheduler = FOLIA ? FoliaSchedulerFactory.create(plugin) : new BukkitPlatformScheduler(plugin);
        Fawe.setFoliaServer(FOLIA);
    }

    /**
     * Get the platform scheduler.
     *
     * @return the scheduler for this server
     * @throws IllegalStateException if called before {@link #init(Plugin)}
     */
    public static PlatformScheduler scheduler() {
        PlatformScheduler current = scheduler;
        if (current == null) {
            throw new IllegalStateException("FaweScheduler has not been initialized yet.");
        }
        return current;
    }

    /**
     * Get whether this server regionises ticking.
     *
     * <p>
     * Safe to call before {@link #init(Plugin)}, unlike {@link #scheduler()}.
     * </p>
     *
     * @return true on Folia, false otherwise
     */
    public static boolean isFolia() {
        return FOLIA;
    }

    /**
     * Cancel every task still tracked and drop the scheduler. Called on disable.
     */
    public static void shutdown() {
        PlatformScheduler current = scheduler;
        if (current != null) {
            current.cancelAllTasks();
            scheduler = null;
        }
    }

    private static boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

}
