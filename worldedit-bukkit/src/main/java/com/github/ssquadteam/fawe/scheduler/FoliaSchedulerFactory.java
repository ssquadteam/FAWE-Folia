package com.github.ssquadteam.fawe.scheduler;

import org.bukkit.plugin.Plugin;

/**
 * Isolates the only reference to {@link FoliaScheduler}.
 *
 * <p>
 * {@link FoliaScheduler} links against Folia-only API. Plain Spigot servers do
 * not ship those classes, so loading it there would throw
 * {@link NoClassDefFoundError}. Keeping the reference in this class means
 * verification of {@link FaweScheduler} never pulls it in - it is loaded only
 * when {@link #create(Plugin)} is actually invoked, which happens on Folia
 * alone.
 * </p>
 */
final class FoliaSchedulerFactory {

    private FoliaSchedulerFactory() {
    }

    /**
     * Create the Folia-backed scheduler.
     *
     * @param plugin the plugin owning every scheduled task
     * @return a scheduler backed by Folia's regionised schedulers
     */
    static PlatformScheduler create(Plugin plugin) {
        return new FoliaScheduler(plugin);
    }

}
