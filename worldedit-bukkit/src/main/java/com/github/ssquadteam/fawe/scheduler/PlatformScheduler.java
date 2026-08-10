package com.github.ssquadteam.fawe.scheduler;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * A scheduler abstraction that hides the difference between a single-threaded
 * Bukkit server and a regionised Folia server.
 *
 * <p>
 * On Folia there is no single main thread. Work is owned by whatever holds the
 * region lock for the chunk, entity, or global server state it touches, so the
 * caller must say <em>what</em> a task touches rather than merely "run this on
 * the server thread". The methods below are grouped by ownership:
 * </p>
 *
 * <ul>
 *     <li>{@code runAtLocation*} - block, chunk, and world state at a location</li>
 *     <li>{@code runAtEntity} - state owned by a single entity, following it
 *     across regions and worlds</li>
 *     <li>{@code runNextTick}, {@code runLater}, {@code runTimer} - global server
 *     state that belongs to no particular region</li>
 *     <li>{@code runAsync}, {@code runLaterAsync}, {@code runTimerAsync} -
 *     blocking or CPU-heavy work that touches no server state at all</li>
 * </ul>
 *
 * <p>
 * On a non-Folia server every ownership variant collapses onto the Bukkit main
 * thread, so callers can use the ownership-correct method unconditionally.
 * </p>
 *
 * <p>
 * Delays and periods are expressed in ticks. A delay of {@code 0} or less is
 * treated as "as soon as possible".
 * </p>
 */
public interface PlatformScheduler {

    /**
     * Get whether this scheduler is backed by Folia's regionised schedulers.
     *
     * @return true on Folia, false on Bukkit, Spigot, and Paper
     */
    boolean isFolia();

    /**
     * Get whether the calling thread currently owns the region containing the
     * given chunk, and may therefore touch it directly.
     *
     * <p>
     * Always true on a non-Folia server when called from the main thread.
     * </p>
     *
     * @param world  the world containing the chunk
     * @param chunkX the chunk X coordinate
     * @param chunkZ the chunk Z coordinate
     * @return true if the calling thread may touch that chunk right now
     */
    boolean isOwnedByCurrentRegion(World world, int chunkX, int chunkZ);

    /**
     * Get whether the calling thread currently owns the region containing the
     * given location.
     *
     * @param location the location to test
     * @return true if the calling thread may touch that location right now
     */
    boolean isOwnedByCurrentRegion(Location location);

    /**
     * Get whether the calling thread currently owns the given entity.
     *
     * @param entity the entity to test
     * @return true if the calling thread may touch that entity right now
     */
    boolean isOwnedByCurrentRegion(Entity entity);

    /**
     * Run a task on the thread owning global server state, on the next tick.
     *
     * @param task the task to run
     * @return a handle to the scheduled task
     */
    SchedulerTask runNextTick(Runnable task);

    /**
     * Run a task on the thread owning global server state, after a delay.
     *
     * @param task  the task to run
     * @param delay delay in ticks
     * @return a handle to the scheduled task
     */
    SchedulerTask runLater(Runnable task, long delay);

    /**
     * Run a repeating task on the thread owning global server state.
     *
     * @param task   the task to run
     * @param delay  delay in ticks before the first execution
     * @param period ticks between executions
     * @return a handle to the scheduled task
     */
    SchedulerTask runTimer(Runnable task, long delay, long period);

    /**
     * Run a task off any server thread.
     *
     * <p>
     * The task must not touch server state. Hop back through one of the
     * ownership-aware methods before doing so.
     * </p>
     *
     * @param task the task to run
     * @return a handle to the scheduled task
     */
    SchedulerTask runAsync(Runnable task);

    /**
     * Run a task off any server thread, after a delay.
     *
     * @param task  the task to run
     * @param delay delay in ticks
     * @return a handle to the scheduled task
     */
    SchedulerTask runLaterAsync(Runnable task, long delay);

    /**
     * Run a repeating task off any server thread.
     *
     * @param task   the task to run
     * @param delay  delay in ticks before the first execution
     * @param period ticks between executions
     * @return a handle to the scheduled task
     */
    SchedulerTask runTimerAsync(Runnable task, long delay, long period);

    /**
     * Run a task on the thread owning the region containing the given location.
     *
     * @param location the location whose region owns the task
     * @param task     the task to run
     * @return a handle to the scheduled task
     */
    SchedulerTask runAtLocation(Location location, Runnable task);

    /**
     * Run a repeating task on the thread owning the region containing the given
     * location.
     *
     * <p>
     * Folia re-resolves region ownership on every execution, so the task keeps
     * running correctly even if the surrounding regions are merged or split.
     * </p>
     *
     * @param location the location whose region owns the task
     * @param task     the task to run
     * @param delay    delay in ticks before the first execution
     * @param period   ticks between executions
     * @return a handle to the scheduled task
     */
    SchedulerTask runAtLocationTimer(Location location, Runnable task, long delay, long period);

    /**
     * Run a task on the thread owning the given entity.
     *
     * <p>
     * The task is silently dropped if the entity is removed before it runs.
     * </p>
     *
     * @param entity the entity that owns the task
     * @param task   the task to run
     * @return a handle to the scheduled task
     */
    SchedulerTask runAtEntity(Entity entity, Runnable task);

    /**
     * Compute a value on the thread owning the region containing the given
     * location.
     *
     * <p>
     * If the calling thread already owns that region the supplier runs inline and
     * an already-completed future is returned, which keeps the common
     * already-on-the-right-thread case free of a scheduler round trip.
     * </p>
     *
     * @param location the location whose region owns the computation
     * @param supplier the computation to run
     * @param <T>      the computed value type
     * @return a future completed with the computed value, or completed
     *         exceptionally if the supplier threw
     */
    <T> CompletableFuture<T> supplyAtLocation(Location location, Supplier<T> supplier);

    /**
     * Compute a value on the thread owning the given entity.
     *
     * <p>
     * The returned future completes exceptionally with an
     * {@link IllegalStateException} if the entity is removed before the supplier
     * runs, so callers never block forever on a dead entity.
     * </p>
     *
     * @param entity   the entity that owns the computation
     * @param supplier the computation to run
     * @param <T>      the computed value type
     * @return a future completed with the computed value
     */
    <T> CompletableFuture<T> supplyAtEntity(Entity entity, Supplier<T> supplier);

    /**
     * Cancel every task this scheduler has scheduled and is still tracking.
     *
     * <p>
     * Called on plugin disable.
     * </p>
     */
    void cancelAllTasks();

}
