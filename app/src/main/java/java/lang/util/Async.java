package java.lang.util;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public final class Async {

    private static final ExecutorService WORKER_POOL = Executors.newWorkStealingPool();
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    private Async() {}

    public static <T> CompletableFuture<T> supply(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, WORKER_POOL);
    }

    public static CompletableFuture<Void> run(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, WORKER_POOL);
    }

    public static <T> T await(CompletableFuture<T> future) {
        try {
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T await(CompletableFuture<T> future, long timeout, TimeUnit unit) {
        try {
            return future.get(timeout, unit);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> awaitAll(List<CompletableFuture<T>> futures) {
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allOf.get();
            return futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> CompletableFuture<T> withTimeout(CompletableFuture<T> future, long delay, TimeUnit unit) {
        CompletableFuture<T> timeoutFuture = new CompletableFuture<>();
        SCHEDULER.schedule(() -> timeoutFuture.completeExceptionally(new TimeoutException()), delay, unit);
        return future.applyToEither(timeoutFuture, Function.identity());
    }

    public static <T> CompletableFuture<T> retry(Supplier<CompletableFuture<T>> supplier, int attempts, long delayMs) {
        return supplier.get().handle((result, ex) -> {
            if (ex == null) return CompletableFuture.completedFuture(result);
            if (attempts <= 1) throw new RuntimeException(ex);
            try { Thread.sleep(delayMs); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            return retry(supplier, attempts - 1, delayMs);
        }).thenCompose(f -> f);
    }

    public static void schedule(Runnable task, long delay, TimeUnit unit) {
        SCHEDULER.schedule(task, delay, unit);
    }

    public static void scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        SCHEDULER.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    public static <T> CompletableFuture<T> race(List<CompletableFuture<T>> futures) {
        CompletableFuture<T> winner = new CompletableFuture<>();
        for (CompletableFuture<T> future : futures) {
            future.thenAccept(winner::complete);
            future.exceptionally(ex -> { winner.completeExceptionally(ex); return null; });
        }
        return winner;
    }

    public static <T, R> List<R> parallelMap(List<T> items, java.util.function.Function<T, R> mapper) {
        return awaitAll(items.stream().map(item -> supply(() -> mapper.apply(item))).collect(Collectors.toList()));
    }

    public static void runOnce(AtomicBoolean flag, Runnable task) {
        if (flag.compareAndSet(false, true)) {
            run(task);
        }
    }

    public static CompletableFuture<Void> runInSequence(Runnable... tasks) {
        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        for (Runnable task : tasks) {
            chain = chain.thenRunAsync(task, WORKER_POOL);
        }
        return chain;
    }

    public static <T> CompletableFuture<T> pipe(CompletableFuture<T> first, java.util.function.Function<T, CompletableFuture<T>> next) {
        return first.thenCompose(next);
    }

    public static <T> CompletableFuture<T> recover(CompletableFuture<T> future, java.util.function.Function<Throwable, T> fallback) {
        return future.exceptionally(fallback);
    }

    public static void shutdown() {
        WORKER_POOL.shutdown();
        SCHEDULER.shutdown();
    }

    public static boolean isPoolActive() {
        return !WORKER_POOL.isTerminated();
    }

    public static <T> CompletableFuture<T> memoizeAsync(Supplier<CompletableFuture<T>> supplier) {
        return new MemoizedAsync<>(supplier).get();
    }

    private static class MemoizedAsync<T> {
        private final Supplier<CompletableFuture<T>> supplier;
        private volatile CompletableFuture<T> cache;
        MemoizedAsync(Supplier<CompletableFuture<T>> supplier) { this.supplier = supplier; }
        synchronized CompletableFuture<T> get() {
            if (cache == null) cache = supplier.get();
            return cache;
        }
    }

    public static <T> void monitor(CompletableFuture<T> future, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        future.whenComplete((result, ex) -> {
            if (ex != null) onError.accept(ex);
            else onSuccess.accept(result);
        });
    }

    public static ExecutorService getWorkerPool() {
        return WORKER_POOL;
    }

    public static CompletableFuture<Void> runWithCleanup(Runnable task, Runnable cleanup) {
        return run(task).whenComplete((v, e) -> cleanup.run());
    }

    public static <T> CompletableFuture<List<T>> collect(List<CompletableFuture<T>> futures) {
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
    }

    public static void executeWithDelay(Runnable task, long delay, TimeUnit unit) {
        CompletableFuture.runAsync(() -> {
            try { unit.sleep(delay); task.run(); } catch (Exception e) { throw new RuntimeException(e); }
        }, WORKER_POOL);
    }

    public static <T> CompletableFuture<T> supplyDelayed(Supplier<T> supplier, long delay, TimeUnit unit) {
        return CompletableFuture.supplyAsync(() -> {
            try { unit.sleep(delay); return supplier.get(); } catch (Exception e) { throw new RuntimeException(e); }
        }, WORKER_POOL);
    }

    public static <T> void fireAndForget(Supplier<T> supplier) {
        WORKER_POOL.submit(supplier::get);
    }

    public static void batchRun(List<Runnable> tasks) {
        tasks.forEach(WORKER_POOL::submit);
    }
}
