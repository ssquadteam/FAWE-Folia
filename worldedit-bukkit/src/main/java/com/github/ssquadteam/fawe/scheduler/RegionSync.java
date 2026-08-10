package com.github.ssquadteam.fawe.scheduler;

import com.fastasyncworldedit.core.util.TaskManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * "Run this where the server allows it, and give me the result" - the one shape almost every FAWE call site needs when it has
 * to touch server state from a FAWE worker thread.
 *
 * <p>On a regionised server the work is routed to the thread owning the target location or entity. Everywhere else it goes
 * through {@link TaskManager}, which hands off to the main thread. Either way the caller blocks until the value is ready.</p>
 *
 * <p>This exists so the future-plus-scheduler-plus-join dance is written once rather than once per adapter. Do not call any
 * of these from a thread that already owns the target - the underlying scheduler detects that case and runs inline, but
 * blocking a region thread on unrelated work is still a good way to stall a region.</p>
 */
public final class RegionSync {

    private RegionSync() {
    }

    /**
     * Compute a value on the thread allowed to touch the given block position.
     *
     * @param world    the world containing the position
     * @param x        block X
     * @param y        block Y
     * @param z        block Z
     * @param supplier the computation to run
     * @param <T>      the computed value type
     * @return the computed value
     */
    public static <T> T supply(World world, int x, int y, int z, Supplier<T> supplier) {
        if (!FaweScheduler.isFolia()) {
            return TaskManager.taskManager().sync(supplier);
        }
        return join(FaweScheduler.scheduler().supplyAtLocation(new Location(world, x, y, z), supplier));
    }

    /**
     * Compute a value on the thread allowed to touch the given location.
     *
     * @param location the location whose owner should run the computation
     * @param supplier the computation to run
     * @param <T>      the computed value type
     * @return the computed value
     */
    public static <T> T supply(Location location, Supplier<T> supplier) {
        if (!FaweScheduler.isFolia()) {
            return TaskManager.taskManager().sync(supplier);
        }
        return join(FaweScheduler.scheduler().supplyAtLocation(location, supplier));
    }

    /**
     * Compute a value on the thread allowed to touch the given entity.
     *
     * @param entity   the entity whose owner should run the computation
     * @param supplier the computation to run
     * @param <T>      the computed value type
     * @return the computed value
     */
    public static <T> T supplyAtEntity(Entity entity, Supplier<T> supplier) {
        if (!FaweScheduler.isFolia()) {
            return TaskManager.taskManager().sync(supplier);
        }
        return join(FaweScheduler.scheduler().supplyAtEntity(entity, supplier));
    }

    /**
     * Hand a task to the region owning the given block position, without waiting for it.
     *
     * <p>Off a regionised server this runs the task inline on the calling thread, exactly as the unpatched code did. On a
     * regionised server it is handed to the owning region instead - which may be a later tick, so only use this where the
     * caller does not depend on the work having finished.</p>
     *
     * @param world the world containing the position
     * @param x     block X
     * @param y     block Y
     * @param z     block Z
     * @param task  the task to run
     */
    public static void dispatch(World world, int x, int y, int z, Runnable task) {
        if (!FaweScheduler.isFolia()) {
            task.run();
            return;
        }
        FaweScheduler.scheduler().runAtLocation(new Location(world, x, y, z), task);
    }

    /**
     * Hand a task to the region owning the given block position, falling back to a platform executor.
     *
     * <p>Same as {@link #dispatch(World, int, int, int, Runnable)}, except that off a regionised server the task goes to
     * {@code fallback} instead of running inline - for call sites whose unpatched form already dispatched somewhere
     * specific, such as the server's own tick executor.</p>
     *
     * @param world    the world containing the position
     * @param x        block X
     * @param y        block Y
     * @param z        block Z
     * @param fallback the executor to use off a regionised server
     * @param task     the task to run
     */
    public static void dispatch(World world, int x, int y, int z, Executor fallback, Runnable task) {
        if (!FaweScheduler.isFolia()) {
            fallback.execute(task);
            return;
        }
        FaweScheduler.scheduler().runAtLocation(new Location(world, x, y, z), task);
    }

    /**
     * Run a task on the thread allowed to touch the given location, and wait for it.
     *
     * @param location the location whose owner should run the task
     * @param task     the task to run
     */
    public static void run(Location location, Runnable task) {
        supply(location, () -> {
            task.run();
            return null;
        });
    }

    /**
     * Wait for {@code future}, unwrapping the {@link CompletionException} so callers see the exception their own supplier
     * threw rather than a scheduler-shaped wrapper.
     */
    private static <T> T join(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw e;
        }
    }

}
