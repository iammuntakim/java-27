package java.lang.expression;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.ArrayList;
import java.util.List;

@FunctionalInterface
public interface Expression<T, R> {

    R evaluate(T input);

    default <V> Expression<T, V> andThen(Expression<R, V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.evaluate(evaluate(t));
    }

    default <V> Expression<V, R> compose(Expression<V, T> before) {
        Objects.requireNonNull(before);
        return (V v) -> evaluate(before.evaluate(v));
    }

    static <T> Expression<T, T> identity() {
        return t -> t;
    }

    default Expression<T, R> filter(Predicate<R> predicate) {
        return (T t) -> {
            R result = evaluate(t);
            if (predicate.test(result)) return result;
            throw new RuntimeException("Expression filter constraint violated");
        };
    }

    default Expression<T, R> fallback(R defaultValue) {
        return (T t) -> {
            try {
                return evaluate(t);
            } catch (Exception e) {
                return defaultValue;
            }
        };
    }

    default Expression<T, R> retry(int attempts, long delay) {
        return (T t) -> {
            int count = 0;
            while (true) {
                try {
                    return evaluate(t);
                } catch (Exception e) {
                    if (++count >= attempts) throw e;
                    try { Thread.sleep(delay); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        };
    }

    default Expression<T, R> trace(java.util.function.Consumer<R> tracer) {
        return (T t) -> {
            R result = evaluate(t);
            tracer.accept(result);
            return result;
        };
    }

    default Expression<T, R> memoize() {
        return new Expression<T, R>() {
            private volatile R cache;
            private volatile boolean computed = false;
            public R evaluate(T t) {
                if (!computed) {
                    synchronized (this) {
                        if (!computed) {
                            cache = Expression.this.evaluate(t);
                            computed = true;
                        }
                    }
                }
                return cache;
            }
        };
    }

    default Expression<T, List<R>> batch(int size) {
        return (T t) -> {
            List<R> results = new ArrayList<>();
            for (int i = 0; i < size; i++) results.add(evaluate(t));
            return results;
        };
    }

    default Expression<T, R> throttle(long ms) {
        return new Expression<T, R>() {
            private long last = 0;
            public synchronized R evaluate(T t) {
                long now = System.currentTimeMillis();
                if (now - last < ms) throw new RuntimeException("Throttled");
                last = now;
                return Expression.this.evaluate(t);
            }
        };
    }

    static <T, R> Expression<T, R> constant(R value) {
        return t -> value;
    }

    default Expression<T, R> validate(Predicate<T> validator) {
        return (T t) -> {
            if (!validator.test(t)) throw new IllegalArgumentException("Validation failed");
            return evaluate(t);
        };
    }

    default <V> Expression<T, R> combine(Expression<T, V> other, java.util.function.BiFunction<R, V, R> merger) {
        return (T t) -> merger.apply(evaluate(t), other.evaluate(t));
    }

    default Expression<T, R> logErrors(java.util.function.Consumer<Throwable> logger) {
        return (T t) -> {
            try {
                return evaluate(t);
            } catch (Throwable e) {
                logger.accept(e);
                throw e;
            }
        };
    }

    default Expression<T, R> timeout(long ms) {
        return (T t) -> {
            java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
            try {
                return executor.submit(() -> evaluate(t)).get(ms, java.util.concurrent.TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                throw new RuntimeException("Timed out", e);
            } finally {
                executor.shutdownNow();
            }
        };
    }

    static <T, R> Expression<T, R> switchCase(java.util.Map<Predicate<T>, Expression<T, R>> cases, Expression<T, R> defaultCase) {
        return (T t) -> {
            for (var entry : cases.entrySet()) {
                if (entry.getKey().test(t)) return entry.getValue().evaluate(t);
            }
            return defaultCase.evaluate(t);
        };
    }

    default Expression<T, R> synchronize() {
        return (T t) -> {
            synchronized (this) {
                return evaluate(t);
            }
        };
    }

    default Expression<T, R> limit(int maxCalls) {
        return new Expression<T, R>() {
            private int calls = 0;
            public synchronized R evaluate(T t) {
                if (calls++ >= maxCalls) throw new RuntimeException("Limit reached");
                return Expression.this.evaluate(t);
            }
        };
    }

    default Expression<T, R> map(Function<R, R> mapper) {
        return (T t) -> mapper.apply(evaluate(t));
    }

    default Expression<T, R> pipe(Expression<R, R> next) {
        return andThen(next);
    }

    default R apply(T input) {
        return evaluate(input);
    }
}
