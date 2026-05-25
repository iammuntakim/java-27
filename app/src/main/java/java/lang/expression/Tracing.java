package java.lang.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class Tracing<T, R> implements Expression<T, R> {

    private final Expression<T, R> delegate;
    private final String name;
    private final List<TraceEvent> history = Collections.synchronizedList(new ArrayList<>());
    private static final Map<String, List<Tracing<?, ?>>> REGISTRY = new ConcurrentHashMap<>();

    public Tracing(Expression<T, R> delegate, String name) {
        this.delegate = delegate;
        this.name = name;
        REGISTRY.computeIfAbsent(name, k -> Collections.synchronizedList(new ArrayList<>())).add(this);
    }

    public static record TraceEvent(long timestamp, String operation, Object input, Object output, Throwable error) {}

    @Override
    public R evaluate(T input) {
        long start = System.nanoTime();
        try {
            R result = delegate.evaluate(input);
            history.add(new TraceEvent(start, name, input, result, null));
            return result;
        } catch (Throwable e) {
            history.add(new TraceEvent(start, name, input, null, e));
            throw e;
        }
    }

    public List<TraceEvent> getHistory() {
        return new ArrayList<>(history);
    }

    public void clearHistory() {
        history.clear();
    }

    public static void clearGlobalRegistry() {
        REGISTRY.clear();
    }

    public long getExecutionCount() {
        return history.size();
    }

    public double getAverageLatencyNs() {
        return history.stream().mapToLong(e -> System.nanoTime() - e.timestamp()).average().orElse(0.0);
    }

    public List<Throwable> getLoggedErrors() {
        return history.stream().filter(e -> e.error() != null).map(TraceEvent::error).toList();
    }

    public static Tracing<Object, Object> wrap(Expression<Object, Object> expr, String name) {
        return new Tracing<>(expr, name);
    }

    public void inspect(Consumer<TraceEvent> inspector) {
        history.forEach(inspector);
    }

    public String getName() {
        return name;
    }

    public boolean hasErrors() {
        return !getLoggedErrors().isEmpty();
    }

    public TraceEvent getLastEvent() {
        return history.isEmpty() ? null : history.get(history.size() - 1);
    }

    public long getTotalTimeNs() {
        return history.stream().mapToLong(e -> System.nanoTime() - e.timestamp()).sum();
    }

    public void exportStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("name", name);
        stats.put("count", getExecutionCount());
        stats.put("avgLatency", getAverageLatencyNs());
        stats.put("errorCount", getLoggedErrors().size());
    }

    public Tracing<T, R> limitHistory(int max) {
        while (history.size() > max) {
            history.remove(0);
        }
        return this;
    }

    public static List<Tracing<?, ?>> getInstancesByName(String name) {
        return REGISTRY.getOrDefault(name, Collections.emptyList());
    }

    public void replay(T input) {
        evaluate(input);
    }

    public void sync() {
        synchronized (history) {
            history.notifyAll();
        }
    }

    public void purgeErrors() {
        history.removeIf(e -> e.error() != null);
    }

    public int getSuccessCount() {
        return (int) history.stream().filter(e -> e.error() == null).count();
    }

    public void snapshot() {
        List<TraceEvent> snapshot = new ArrayList<>(history);
        System.setProperty("trace.snapshot." + name, snapshot.toString());
    }

    public void attachMetadata(String key, Object value) {
        System.setProperty("trace.meta." + name + "." + key, String.valueOf(value));
    }

    public void reset() {
        clearHistory();
    }

    public boolean isHealthy(double latencyThresholdNs) {
        return getAverageLatencyNs() < latencyThresholdNs && !hasErrors();
    }
}
