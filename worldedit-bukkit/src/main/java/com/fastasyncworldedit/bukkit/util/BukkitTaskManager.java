package com.fastasyncworldedit.bukkit.util;

import com.fastasyncworldedit.core.util.TaskManager;
import com.github.ssquadteam.fawe.scheduler.FaweScheduler;
import com.github.ssquadteam.fawe.scheduler.SchedulerTask;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link TaskManager} backed by {@link FaweScheduler}, so the same code path works on both a regionised and a
 * non-regionised server.
 *
 * <p>Everything scheduled here is global server work: FAWE's tick loop, memory checks, and listener bookkeeping. Work that
 * belongs to a specific location or entity goes through {@link FaweScheduler#scheduler()} directly instead.</p>
 */
public class BukkitTaskManager extends TaskManager {

    /**
     * {@link TaskManager} hands out {@code int} task IDs, but the platform scheduler hands out task handles, so IDs are
     * assigned here and mapped back to their handle for {@link #cancel(int)}.
     */
    private final Map<Integer, SchedulerTask> tasks = new ConcurrentHashMap<>();
    private final AtomicInteger nextTaskId = new AtomicInteger();

    @Override
    public int repeat(@Nonnull final Runnable runnable, final int interval) {
        return track(FaweScheduler.scheduler().runTimer(runnable, interval, interval));
    }

    @Override
    public int repeatAsync(@Nonnull final Runnable runnable, final int interval) {
        return track(FaweScheduler.scheduler().runTimerAsync(runnable, interval, interval));
    }

    @Override
    public void async(@Nonnull final Runnable runnable) {
        FaweScheduler.scheduler().runAsync(runnable);
    }

    @Override
    public void task(@Nonnull final Runnable runnable) {
        FaweScheduler.scheduler().runNextTick(runnable);
    }

    @Override
    public void later(@Nonnull final Runnable runnable, final int delay) {
        FaweScheduler.scheduler().runLater(runnable, delay);
    }

    @Override
    public void laterAsync(@Nonnull final Runnable runnable, final int delay) {
        FaweScheduler.scheduler().runLaterAsync(runnable, delay);
    }

    @Override
    public void cancel(final int task) {
        if (task == -1) {
            return;
        }
        SchedulerTask scheduled = tasks.remove(task);
        if (scheduled != null) {
            scheduled.cancel();
        }
    }

    private int track(SchedulerTask task) {
        int id = nextTaskId.incrementAndGet();
        tasks.put(id, task);
        return id;
    }

}
