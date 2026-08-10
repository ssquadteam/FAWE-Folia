package com.github.ssquadteam.fawe.scheduler;

/**
 * The handle returned when a task was executed inline instead of being
 * scheduled, because the calling thread already owned the work.
 *
 * <p>
 * There is nothing left to cancel, so {@link #cancel()} is a no-op.
 * </p>
 */
enum CompletedSchedulerTask implements SchedulerTask {

    INSTANCE;

    @Override
    public void cancel() {
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

}
