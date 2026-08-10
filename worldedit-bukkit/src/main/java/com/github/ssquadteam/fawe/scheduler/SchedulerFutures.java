package com.github.ssquadteam.fawe.scheduler;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Shared plumbing for turning "run this where it is owned" into "give me the
 * value it produced".
 *
 * <p>
 * Kept in one place so the {@code CompletableFuture} plus try/catch dance is not
 * repeated at every call site that needs a value from an owning thread.
 * </p>
 */
final class SchedulerFutures {

    private SchedulerFutures() {
    }

    /**
     * Dispatches a task onto the thread owning {@code O}.
     *
     * @param <O> the owning object type - a location or an entity
     */
    @FunctionalInterface
    interface Dispatcher<O> {

        SchedulerTask dispatch(O owner, Runnable task);

    }

    /**
     * Run {@code supplier} on the thread owning {@code owner} and return a future
     * for its result.
     *
     * @param dispatcher the ownership-aware dispatch method to route through
     * @param owner      the object whose owning thread should run the supplier
     * @param supplier   the computation to run
     * @param <O>        the owning object type
     * @param <T>        the computed value type
     * @return a future completed with the value, or completed exceptionally if
     *         the supplier threw
     */
    static <O, T> CompletableFuture<T> supplyOn(Dispatcher<O> dispatcher, O owner, Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        dispatcher.dispatch(owner, () -> complete(future, supplier));
        return future;
    }

    /**
     * Complete {@code future} from {@code supplier}, routing any thrown exception
     * into the future rather than letting it escape into a scheduler thread.
     *
     * @param future   the future to complete
     * @param supplier the computation to run
     * @param <T>      the computed value type
     */
    static <T> void complete(CompletableFuture<T> future, Supplier<T> supplier) {
        try {
            future.complete(supplier.get());
        } catch (Throwable throwable) {
            future.completeExceptionally(throwable);
        }
    }

}
