package java.lang.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public final class Chain<T, R> implements Expression<T, R> {

    private final List<Expression<Object, Object>> stages;

    public Chain() {
        this.stages = new ArrayList<>();
    }

    private Chain(List<Expression<Object, Object>> stages) {
        this.stages = new ArrayList<>(stages);
    }

    public Chain<T, R> add(Expression<Object, Object> stage) {
        Objects.requireNonNull(stage);
        stages.add(stage);
        return this;
    }

    public <V> Chain<T, V> pipeChain(Expression<Object, Object> next) {
        List<Expression<Object, Object>> newStages = new ArrayList<>(stages);
        newStages.add(next);
        return new Chain<>(newStages);
    }

    @Override
    @SuppressWarnings("unchecked")
    public R evaluate(T input) {
        Object current = input;
        for (Expression<Object, Object> stage : stages) {
            current = stage.evaluate(current);
        }
        return (R) current;
    }

    public List<Object> executeAll(T input) {
        List<Object> results = new ArrayList<>();
        Object current = input;
        for (Expression<Object, Object> stage : stages) {
            current = stage.evaluate(current);
            results.add(current);
        }
        return results;
    }

    public Chain<T, R> chainFilter(java.util.function.Predicate<Object> predicate) {
        stages.add(input -> {
            if (predicate.test(input)) return input;
            throw new RuntimeException("Chain filter rejected input");
        });
        return this;
    }

    public Chain<T, R> chainTrace(java.util.function.Consumer<Object> logger) {
        stages.add(input -> {
            logger.accept(input);
            return input;
        });
        return this;
    }

    public Chain<T, R> chainMap(java.util.function.Function<Object, Object> mapper) {
        stages.add(input -> mapper.apply(input));
        return this;
    }

    public Chain<T, R> chainValidate(java.util.function.Predicate<Object> validator) {
        stages.add(input -> {
            if (!validator.test(input)) throw new IllegalArgumentException("Chain validation failed");
            return input;
        });
        return this;
    }

    public Chain<T, R> retry(int times, long delay) {
        int lastIndex = stages.size() - 1;
        if (lastIndex < 0) return this;
        Expression<Object, Object> last = stages.get(lastIndex);
        stages.set(lastIndex, input -> {
            int attempt = 0;
            while (true) {
                try {
                    return last.evaluate(input);
                } catch (Exception e) {
                    if (++attempt >= times) throw e;
                    try { Thread.sleep(delay); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        });
        return this;
    }

    public Chain<T, R> timeout(long ms) {
        int lastIndex = stages.size() - 1;
        if (lastIndex < 0) return this;
        Expression<Object, Object> last = stages.get(lastIndex);
        stages.set(lastIndex, input -> {
            java.util.concurrent.ExecutorService ex = java.util.concurrent.Executors.newSingleThreadExecutor();
            try {
                return ex.submit(() -> last.evaluate(input)).get(ms, java.util.concurrent.TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                throw new RuntimeException("Chain timeout", e);
            } finally {
                ex.shutdownNow();
            }
        });
        return this;
    }

    public void clear() { stages.clear(); }
    public int stageCount() { return stages.size(); }
    public boolean isEmpty() { return stages.isEmpty(); }
}
