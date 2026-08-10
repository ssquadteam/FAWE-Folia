package com.github.ssquadteam.fawe.scheduler;

/**
 * A handle to a task submitted to a {@link PlatformScheduler}.
 *
 * <p>
 * The handle is platform independent: the same type is returned whether the task
 * was scheduled through Bukkit's scheduler or through one of Folia's regionised
 * schedulers.
 * </p>
 */
public interface SchedulerTask {

    /**
     * Cancel this task.
     *
     * <p>
     * A task that is currently executing is not interrupted; cancellation only
     * prevents further executions. Cancelling an already cancelled or already
     * finished task is a no-op.
     * </p>
     */
    void cancel();

    /**
     * Get whether this task has been cancelled.
     *
     * @return true if {@link #cancel()} has been called on this task
     */
    boolean isCancelled();

}
