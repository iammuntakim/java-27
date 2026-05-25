package java.lang.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

public final class Registry {

    private static final Map<String, Object> STORE = new ConcurrentHashMap<>();
    private static final Map<String, Class<?>> TYPE_MAP = new ConcurrentHashMap<>();

    private Registry() {}

    public static <T> void register(String key, T value) {
        STORE.put(key, value);
        TYPE_MAP.put(key, value.getClass());
    }

    public static <T> void registerIfAbsent(String key, T value) {
        STORE.putIfAbsent(key, value);
        TYPE_MAP.putIfAbsent(key, value.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) STORE.get(key);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getOrDefault(String key, T defaultValue) {
        return (T) STORE.getOrDefault(key, defaultValue);
    }

    public static <T> Optional<T> find(String key) {
        return Optional.ofNullable(get(key));
    }

    public static boolean contains(String key) {
        return STORE.containsKey(key);
    }

    public static void remove(String key) {
        STORE.remove(key);
        TYPE_MAP.remove(key);
    }

    public static void clear() {
        STORE.clear();
        TYPE_MAP.clear();
    }

    public static int size() {
        return STORE.size();
    }

    public static List<String> keys() {
        return new ArrayList<>(STORE.keySet());
    }

    public static <T> T computeIfAbsent(String key, Supplier<T> supplier) {
        @SuppressWarnings("unchecked")
        T value = (T) STORE.computeIfAbsent(key, k -> {
            T val = supplier.get();
            if (val != null) TYPE_MAP.put(k, val.getClass());
            return val;
        });
        return value;
    }

    public static <T> void update(String key, T value) {
        if (STORE.containsKey(key)) {
            STORE.put(key, value);
            TYPE_MAP.put(key, value.getClass());
        }
    }

    public static <T> T getTyped(String key, Class<T> clazz) {
        Object val = STORE.get(key);
        return clazz.isInstance(val) ? clazz.cast(val) : null;
    }

    public static Map<String, Object> snapshot() {
        return Collections.unmodifiableMap(new ConcurrentHashMap<>(STORE));
    }

    public static List<Object> values() {
        return new ArrayList<>(STORE.values());
    }

    public static <T> List<T> findByType(Class<T> clazz) {
        return STORE.values().stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    public static void registerAll(Map<String, Object> entries) {
        entries.forEach(Registry::register);
    }

    public static boolean isEmpty() {
        return STORE.isEmpty();
    }

    public static void replaceAll(java.util.function.BiFunction<String, Object, Object> function) {
        STORE.replaceAll(function);
    }

    public static void removeAll(List<String> keys) {
        keys.forEach(Registry::remove);
    }

    public static String getEntryType(String key) {
        Class<?> type = TYPE_MAP.get(key);
        return type != null ? type.getName() : "null";
    }

    public static <T> T getAndRemove(String key) {
        T val = get(key);
        remove(key);
        return val;
    }

    public static boolean compareAndSet(String key, Object expected, Object newValue) {
        if (STORE.get(key) == expected) {
            STORE.put(key, newValue);
            TYPE_MAP.put(key, newValue.getClass());
            return true;
        }
        return false;
    }

    public static void compute(String key, java.util.function.BiFunction<String, Object, Object> remappingFunction) {
        STORE.compute(key, remappingFunction);
    }

    public static void merge(String key, Object value, java.util.function.BiFunction<Object, Object, Object> remappingFunction) {
        STORE.merge(key, value, remappingFunction);
    }

    public static void forEach(java.util.function.BiConsumer<String, Object> action) {
        STORE.forEach(action);
    }

    public static <T> T getRequired(String key) {
        T val = get(key);
        if (val == null) throw new java.util.NoSuchElementException("Registry key not found: " + key);
        return val;
    }

    public static Map<String, Object> filterByPrefix(String prefix) {
        return STORE.entrySet().stream()
                .filter(e -> e.getKey().startsWith(prefix))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static void putAll(Map<? extends String, ?> m) {
        m.forEach((k, v) -> {
            STORE.put(k, v);
            TYPE_MAP.put(k, v.getClass());
        });
    }

    public static boolean containsValue(Object value) {
        return STORE.containsValue(value);
    }

    public static Object getRaw(String key) {
        return STORE.get(key);
    }

    public static void lock(String key) {
        synchronized (STORE) {
            STORE.get(key);
        }
    }

    public static void putSynchronized(String key, Object value) {
        synchronized (STORE) {
            STORE.put(key, value);
            TYPE_MAP.put(key, value.getClass());
        }
    }

    public static void removeSynchronized(String key) {
        synchronized (STORE) {
            STORE.remove(key);
            TYPE_MAP.remove(key);
        }
    }
}
