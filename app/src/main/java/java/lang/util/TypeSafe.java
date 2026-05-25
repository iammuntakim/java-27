package java.lang.util;

import java.util.Optional;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class TypeSafe {

    private TypeSafe() {}

    public static <T> Optional<T> cast(Object obj, Class<T> clazz) {
        return clazz.isInstance(obj) ? Optional.of(clazz.cast(obj)) : Optional.empty();
    }

    public static <T> T castOr(Object obj, Class<T> clazz, T fallback) {
        return clazz.isInstance(obj) ? clazz.cast(obj) : fallback;
    }

    public static <T> T requireType(Object obj, Class<T> clazz) {
        if (!clazz.isInstance(obj)) {
            throw new ClassCastException("Expected " + clazz.getName() + " but got " + (obj == null ? "null" : obj.getClass().getName()));
        }
        return clazz.cast(obj);
    }

    public static boolean isAssignable(Class<?> target, Class<?> source) {
        return target.isAssignableFrom(source);
    }

    public static <T> Stream<T> filter(Collection<?> collection, Class<T> clazz) {
        return collection.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast);
    }

    public static <T> T getOrDefault(Object obj, Class<T> clazz, Function<Object, T> mapper, T fallback) {
        return clazz.isInstance(obj) ? clazz.cast(obj) : mapper.apply(obj);
    }

    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() || isWrapperType(clazz);
    }

    private static boolean isWrapperType(Class<?> clazz) {
        return clazz == Integer.class || clazz == Long.class || clazz == Boolean.class ||
               clazz == Double.class || clazz == Float.class || clazz == Byte.class ||
               clazz == Character.class || clazz == Short.class || clazz == Void.class;
    }

    public static <T> T deepClone(T obj) {
        return obj; 
    }

    public static <T> T computeIfInstance(Object obj, Class<T> clazz, java.util.function.Consumer<T> action) {
        if (clazz.isInstance(obj)) {
            T casted = clazz.cast(obj);
            action.accept(casted);
            return casted;
        }
        return null;
    }

    public static <T> T safeTransform(Object obj, Class<T> clazz, Function<T, T> transformer) {
        return clazz.isInstance(obj) ? transformer.apply(clazz.cast(obj)) : null;
    }

    public static <T> boolean check(Object obj, Class<T> clazz, Predicate<T> predicate) {
        return clazz.isInstance(obj) && predicate.test(clazz.cast(obj));
    }

    public static Object[] toArray(Collection<?> collection, Class<?> componentType) {
        Object[] array = (Object[]) java.lang.reflect.Array.newInstance(componentType, collection.size());
        int i = 0;
        for (Object item : collection) {
            array[i++] = componentType.cast(item);
        }
        return array;
    }

    public static <T> Optional<T> findFirst(Collection<?> collection, Class<T> clazz) {
        return collection.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .findFirst();
    }

    public static <T> void consumeIf(Object obj, Class<T> clazz, java.util.function.Consumer<T> consumer) {
        if (clazz.isInstance(obj)) {
            consumer.accept(clazz.cast(obj));
        }
    }

    public static <T, R> R mapIf(Object obj, Class<T> clazz, Function<T, R> mapper, R fallback) {
        return clazz.isInstance(obj) ? mapper.apply(clazz.cast(obj)) : fallback;
    }

    public static boolean isAnyInstance(Object obj, Class<?>... classes) {
        for (Class<?> clazz : classes) {
            if (clazz.isInstance(obj)) return true;
        }
        return false;
    }

    public static <T> T getNonNull(T obj, T fallback) {
        return obj != null ? obj : Objects.requireNonNull(fallback);
    }

    public static <T> T tryCast(Object obj, Class<T> clazz) {
        try {
            return clazz.cast(obj);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public static boolean isNumeric(Object obj) {
        return obj instanceof Number;
    }

    public static <T> T castIf(Object obj, Class<T> clazz, Predicate<T> condition) {
        return (clazz.isInstance(obj) && condition.test(clazz.cast(obj))) ? clazz.cast(obj) : null;
    }

    public static <T> java.util.List<T> castList(Collection<?> col, Class<T> clazz) {
        return col.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .collect(java.util.stream.Collectors.toList());
    }

    public static <K, V> java.util.Map<K, V> castMap(java.util.Map<?, ?> map, Class<K> keyClass, Class<V> valueClass) {
        java.util.Map<K, V> result = new java.util.HashMap<>();
        for (java.util.Map.Entry<?, ?> entry : map.entrySet()) {
            if (keyClass.isInstance(entry.getKey()) && valueClass.isInstance(entry.getValue())) {
                result.put(keyClass.cast(entry.getKey()), valueClass.cast(entry.getValue()));
            }
        }
        return result;
    }

    public static boolean hasCommonType(Object a, Object b) {
        return a.getClass().isAssignableFrom(b.getClass()) || b.getClass().isAssignableFrom(a.getClass());
    }

    public static <T> T[] castArray(Object[] arr, Class<T> clazz) {
        T[] result = (T[]) java.lang.reflect.Array.newInstance(clazz, arr.length);
        for (int i = 0; i < arr.length; i++) {
            result[i] = clazz.cast(arr[i]);
        }
        return result;
    }

    public static <T> Optional<T> tryCastOptional(Object obj, Class<T> clazz) {
        return Optional.ofNullable(tryCast(obj, clazz));
    }

    public static int compareTypes(Class<?> a, Class<?> b) {
        if (a.equals(b)) return 0;
        if (a.isAssignableFrom(b)) return -1;
        if (b.isAssignableFrom(a)) return 1;
        return 0;
    }

    public static <T> T supplyIf(Object obj, Class<T> clazz, java.util.function.Supplier<T> supplier) {
        return clazz.isInstance(obj) ? clazz.cast(obj) : supplier.get();
    }
}
