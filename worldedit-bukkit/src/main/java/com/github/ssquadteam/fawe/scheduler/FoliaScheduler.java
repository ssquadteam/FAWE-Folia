package com.github.ssquadteam.fawe.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * {@link PlatformScheduler} backed by Folia's regionised schedulers.
 *
 * <p>
 * This class references Folia-only API and is therefore only loaded when
 * {@link FaweScheduler} has detected a Folia server. Referencing it on a
 * non-Folia server would fail to link.
 * </p>
 */
final class FoliaScheduler implements PlatformScheduler {

    /**
     * Milliseconds in one tick, used to translate tick-based delays into the
     * wall-clock delays Folia's async scheduler expects.
     */
    private static final long MILLIS_PER_TICK = 50L;

    private final Plugin plugin;

    /**
     * Repeating region tasks, which no {@code cancelTasks(plugin)} call covers.
     * One-shot tasks are not tracked: they retire themselves within a tick or two,
     * and tracking them would grow this set without bound.
     */
    private final Set<SchedulerTask> trackedTasks = ConcurrentHashMap.newKeySet();

    FoliaScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isFolia() {
        return true;
    }

    @Override
    public boolean isOwnedByCurrentRegion(World world, int chunkX, int chunkZ) {
        return Bukkit.isOwnedByCurrentRegion(world, chunkX, chunkZ);
    }

    @Override
    public boolean isOwnedByCurrentRegion(Location location) {
        return Bukkit.isOwnedByCurrentRegion(location);
    }

    @Override
    public boolean isOwnedByCurrentRegion(Entity entity) {
        return Bukkit.isOwnedByCurrentRegion(entity);
    }

    @Override
    public SchedulerTask runNextTick(Runnable task) {
        return wrap(Bukkit.getGlobalRegionScheduler().run(plugin, scheduled -> task.run()));
    }

    @Override
    public SchedulerTask runLater(Runnable task, long delay) {
        return wrap(Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduled -> task.run(), atLeastOneTick(delay)));
    }

    @Override
    public SchedulerTask runTimer(Runnable task, long delay, long period) {
        return wrap(Bukkit.getGlobalRegionScheduler().runAtFixedRate(
                plugin,
                scheduled -> task.run(),
                atLeastOneTick(delay),
                atLeastOneTick(period)
        ));
    }

    @Override
    public SchedulerTask runAsync(Runnable task) {
        return wrap(Bukkit.getAsyncScheduler().runNow(plugin, scheduled -> task.run()));
    }

    @Override
    public SchedulerTask runLaterAsync(Runnable task, long delay) {
        return wrap(Bukkit.getAsyncScheduler().runDelayed(
                plugin,
                scheduled -> task.run(),
                atLeastOneTick(delay) * MILLIS_PER_TICK,
                TimeUnit.MILLISECONDS
        ));
    }

    @Override
    public SchedulerTask runTimerAsync(Runnable task, long delay, long period) {
        return wrap(Bukkit.getAsyncScheduler().runAtFixedRate(
                plugin,
                scheduled -> task.run(),
                atLeastOneTick(delay) * MILLIS_PER_TICK,
                atLeastOneTick(period) * MILLIS_PER_TICK,
                TimeUnit.MILLISECONDS
        ));
    }

    @Override
    public SchedulerTask runAtLocation(Location location, Runnable task) {
        // Running inline when the region is already ours keeps callers that block on
        // the result from deadlocking against their own region thread.
        if (Bukkit.isOwnedByCurrentRegion(location)) {
            task.run();
            return CompletedSchedulerTask.INSTANCE;
        }
        return wrap(Bukkit.getRegionScheduler().run(plugin, location, scheduled -> task.run()));
    }

    @Override
    public SchedulerTask runAtLocationTimer(Location location, Runnable task, long delay, long period) {
        SchedulerTask tracked = wrap(Bukkit.getRegionScheduler().runAtFixedRate(
                plugin,
                location,
                scheduled -> task.run(),
                atLeastOneTick(delay),
                atLeastOneTick(period)
        ));
        trackedTasks.add(tracked);
        return tracked;
    }

    @Override
    public SchedulerTask runAtEntity(Entity entity, Runnable task) {
        if (Bukkit.isOwnedByCurrentRegion(entity)) {
            task.run();
            return CompletedSchedulerTask.INSTANCE;
        }
        ScheduledTask scheduled = entity.getScheduler().run(plugin, ignored -> task.run(), null);
        // A null result means the entity retired before the task could be scheduled,
        // so the task will never run and there is nothing to cancel.
        return scheduled == null ? CompletedSchedulerTask.INSTANCE : wrap(scheduled);
    }

    @Override
    public <T> CompletableFuture<T> supplyAtLocation(Location location, Supplier<T> supplier) {
        return SchedulerFutures.supplyOn(this::runAtLocation, location, supplier);
    }

    @Override
    public <T> CompletableFuture<T> supplyAtEntity(Entity entity, Supplier<T> supplier) {
        if (Bukkit.isOwnedByCurrentRegion(entity)) {
            CompletableFuture<T> future = new CompletableFuture<>();
            SchedulerFutures.complete(future, supplier);
            return future;
        }
        CompletableFuture<T> future = new CompletableFuture<>();
        ScheduledTask scheduled = entity.getScheduler().run(
                plugin,
                ignored -> SchedulerFutures.complete(future, supplier),
                () -> future.completeExceptionally(new IllegalStateException("Entity retired before the task ran"))
        );
        if (scheduled == null) {
            future.completeExceptionally(new IllegalStateException("Entity retired before the task was scheduled"));
        }
        return future;
    }

    @Override
    public void cancelAllTasks() {
        Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
        Bukkit.getAsyncScheduler().cancelTasks(plugin);
        for (SchedulerTask task : trackedTasks) {
            task.cancel();
        }
        trackedTasks.clear();
    }

    /**
     * Folia rejects delays and periods below one tick, so clamp them.
     *
     * @param ticks the requested tick count
     * @return the same count, or 1 if it was lower
     */
    private static long atLeastOneTick(long ticks) {
        return Math.max(1L, ticks);
    }

    private SchedulerTask wrap(ScheduledTask task) {
        return new FoliaSchedulerTask(task, trackedTasks);
    }

    private record FoliaSchedulerTask(ScheduledTask task, Set<SchedulerTask> trackedTasks) implements SchedulerTask {

        @Override
        public void cancel() {
            task.cancel();
            trackedTasks.remove(this);
        }

        @Override
        public boolean isCancelled() {
            return task.isCancelled();
        }

    }

}
