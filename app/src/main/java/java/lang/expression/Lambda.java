package java.lang.expression;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@FunctionalInterface
public interface Lambda<T, R> extends Serializable {

    R apply(T t);

    default <V> Lambda<V, R> compose(Lambda<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }

    default <V> Lambda<T, V> andThen(Lambda<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }

    static <T> Lambda<T, T> identity() {
        return t -> t;
    }

    default Lambda<T, Optional<R>> lift() {
        return t -> Optional.ofNullable(apply(t));
    }

    default Lambda<T, R> memoize() {
        return new MemoizedLambda<>(this);
    }

    default Lambda<T, R> retry(int maxAttempts) {
        return t -> {
            int attempts = 0;
            while (attempts < maxAttempts) {
                try {
                    return apply(t);
                } catch (Exception e) {
                    attempts++;
                    if (attempts >= maxAttempts) throw e;
                }
            }
            return null;
        };
    }

    default Lambda<T, R> decorate(Function<Lambda<T, R>, Lambda<T, R>> decorator) {
        return decorator.apply(this);
    }

    default Lambda<Iterable<T>, Stream<R>> mapAll() {
        return ts -> {
            List<R> results = new ArrayList<>();
            for (T t : ts) results.add(apply(t));
            return results.stream();
        };
    }

    static <T, R> Lambda<T, R> constant(R value) {
        return t -> value;
    }

    default Lambda<T, Boolean> check(Predicate<? super R> predicate) {
        return t -> predicate.test(apply(t));
    }

    default Lambda<T, R> fallback(Lambda<? super T, ? extends R> defaultValue) {
        return t -> {
            try {
                return apply(t);
            } catch (Exception e) {
                return defaultValue.apply(t);
            }
        };
    }

    default Lambda<T, R> trace(java.util.function.Consumer<R> logger) {
        return t -> {
            R result = apply(t);
            logger.accept(result);
            return result;
        };
    }

    default Lambda<T, R> throttle(long ms) {
        return new ThrottledLambda<>(this, ms);
    }

    default Lambda<T, R> async(java.util.concurrent.Executor executor) {
        return t -> {
            try {
                return java.util.concurrent.CompletableFuture.supplyAsync(() -> apply(t), executor).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    default <U> Lambda<T, Pair<R, U>> zip(Lambda<? super T, ? extends U> other) {
        return t -> new Pair<>(this.apply(t), other.apply(t));
    }

    final class MemoizedLambda<T, R> implements Lambda<T, R> {
        private final Lambda<T, R> delegate;
        private final java.util.Map<T, R> cache = new java.util.concurrent.ConcurrentHashMap<>();

        MemoizedLambda(Lambda<T, R> delegate) { this.delegate = delegate; }

        @Override
        public R apply(T t) {
            return cache.computeIfAbsent(t, delegate::apply);
        }
    }

    final class ThrottledLambda<T, R> implements Lambda<T, R> {
        private final Lambda<T, R> delegate;
        private final long interval;
        private long lastCall = 0;

        ThrottledLambda(Lambda<T, R> delegate, long interval) {
            this.delegate = delegate;
            this.interval = interval;
        }

        @Override
        public synchronized R apply(T t) {
            long now = System.currentTimeMillis();
            if (now - lastCall < interval) return null;
            lastCall = now;
            return delegate.apply(t);
        }
    }

    final class Pair<A, B> {
        public final A first;
        public final B second;
        Pair(A first, B second) { this.first = first; this.second = second; }
    }

    static <T, U, R> Lambda<T, R> curry(BiFunction<T, U, R> func, U u) {
        return t -> func.apply(t, u);
    }

    default Lambda<T, R> validate(Predicate<T> validator) {
        return t -> {
            if (!validator.test(t)) throw new IllegalArgumentException("Invalid input");
            return apply(t);
        };
    }

    default Lambda<T, R> debounce(long delay) {
        return new DebouncedLambda<>(this, delay);
    }

    final class DebouncedLambda<T, R> implements Lambda<T, R> {
        private final Lambda<T, R> delegate;
        private final long delay;
        private long lastRequest = 0;

        DebouncedLambda(Lambda<T, R> delegate, long delay) {
            this.delegate = delegate;
            this.delay = delay;
        }

        @Override
        public R apply(T t) {
            lastRequest = System.currentTimeMillis();
            try { Thread.sleep(delay); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            if (System.currentTimeMillis() - lastRequest >= delay) return delegate.apply(t);
            return null;
        }
    }

    default Lambda<T, R> profile(java.util.function.Consumer<Long> timer) {
        return t -> {
            long start = System.nanoTime();
            R result = apply(t);
            timer.accept(System.nanoTime() - start);
            return result;
        };
    }

    static <T> Lambda<T, Boolean> not(Predicate<T> p) {
        return t -> !p.test(t);
    }

    default Lambda<T, R> sync(Object lock) {
        return t -> {
            synchronized(lock) {
                return apply(t);
            }
        };
    }

    default Lambda<T, R> cacheResult() {
        return new CachingLambda<>(this);
    }

    final class CachingLambda<T, R> implements Lambda<T, R> {
        private final Lambda<T, R> delegate;
        private R lastResult;
        private T lastInput;
        CachingLambda(Lambda<T, R> delegate) { this.delegate = delegate; }
        @Override
        public synchronized R apply(T t) {
            if (Objects.equals(t, lastInput)) return lastResult;
            lastInput = t;
            lastResult = delegate.apply(t);
            return lastResult;
        }
    }

    default Lambda<T, R> retryExponential(int maxAttempts, long baseDelay) {
        return t -> {
            int attempt = 0;
            while(true) {
                try { return apply(t); }
                catch (Exception e) {
                    if (++attempt >= maxAttempts) throw e;
                    try { Thread.sleep(baseDelay * (long)Math.pow(2, attempt)); } 
                    catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        };
    }

    default Lambda<T, R> limitExecution(int max) {
        return new LimitedLambda<>(this, max);
    }

    final class LimitedLambda<T, R> implements Lambda<T, R> {
        private final Lambda<T, R> delegate;
        private final java.util.concurrent.atomic.AtomicInteger counter;
        LimitedLambda(Lambda<T, R> delegate, int max) {
            this.delegate = delegate;
            this.counter = new java.util.concurrent.atomic.AtomicInteger(max);
        }
        @Override
        public R apply(T t) {
            if (counter.getAndDecrement() > 0) return delegate.apply(t);
            throw new IllegalStateException("Execution limit reached");
        }
    }
}
