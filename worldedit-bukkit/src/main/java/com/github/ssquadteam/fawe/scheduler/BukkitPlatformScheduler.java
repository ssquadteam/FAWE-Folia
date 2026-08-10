package com.github.ssquadteam.fawe.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * {@link PlatformScheduler} backed by the Bukkit scheduler.
 *
 * <p>
 * Used on Bukkit, Spigot, and non-regionised Paper, where all server state is
 * owned by the single main thread. Every ownership variant therefore maps onto
 * the main thread, and the async variants map onto Bukkit's async task pool.
 * </p>
 */
final class BukkitPlatformScheduler implements PlatformScheduler {

    private final Plugin plugin;

    BukkitPlatformScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isFolia() {
        return false;
    }

    @Override
    public boolean isOwnedByCurrentRegion(World world, int chunkX, int chunkZ) {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public boolean isOwnedByCurrentRegion(Location location) {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public boolean isOwnedByCurrentRegion(Entity entity) {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public SchedulerTask runNextTick(Runnable task) {
        return wrap(Bukkit.getScheduler().runTask(plugin, task));
    }

    @Override
    public SchedulerTask runLater(Runnable task, long delay) {
        if (delay <= 0) {
            return runNextTick(task);
        }
        return wrap(Bukkit.getScheduler().runTaskLater(plugin, task, delay));
    }

    @Override
    public SchedulerTask runTimer(Runnable task, long delay, long period) {
        return wrap(Bukkit.getScheduler().runTaskTimer(plugin, task, Math.max(0, delay), Math.max(1, period)));
    }

    @Override
    public SchedulerTask runAsync(Runnable task) {
        return wrap(Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
    }

    @Override
    public SchedulerTask runLaterAsync(Runnable task, long delay) {
        if (delay <= 0) {
            return runAsync(task);
        }
        return wrap(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay));
    }

    @Override
    public SchedulerTask runTimerAsync(Runnable task, long delay, long period) {
        return wrap(Bukkit.getScheduler().runTaskTimerAsynchronously(
                plugin,
                task,
                Math.max(0, delay),
                Math.max(1, period)
        ));
    }

    @Override
    public SchedulerTask runAtLocation(Location location, Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            task.run();
            return CompletedSchedulerTask.INSTANCE;
        }
        return runNextTick(task);
    }

    @Override
    public SchedulerTask runAtLocationTimer(Location location, Runnable task, long delay, long period) {
        return runTimer(task, delay, period);
    }

    @Override
    public SchedulerTask runAtEntity(Entity entity, Runnable task) {
        return runAtLocation(entity.getLocation(), task);
    }

    @Override
    public <T> CompletableFuture<T> supplyAtLocation(Location location, Supplier<T> supplier) {
        return SchedulerFutures.supplyOn(this::runAtLocation, location, supplier);
    }

    @Override
    public <T> CompletableFuture<T> supplyAtEntity(Entity entity, Supplier<T> supplier) {
        return SchedulerFutures.supplyOn(this::runAtEntity, entity, supplier);
    }

    @Override
    public void cancelAllTasks() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }

    private static SchedulerTask wrap(BukkitTask task) {
        return new BukkitSchedulerTask(task);
    }

    private record BukkitSchedulerTask(BukkitTask task) implements SchedulerTask {

        @Override
        public void cancel() {
            task.cancel();
        }

        @Override
        public boolean isCancelled() {
            return task.isCancelled();
        }

    }

}
