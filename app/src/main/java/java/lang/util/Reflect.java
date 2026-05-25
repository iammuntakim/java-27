package java.lang.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class Reflect {

    private static final Map<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Field> FIELD_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Constructor<?>> CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();

    private Reflect() {}

    public static Object invoke(Object target, String methodName, Object... args) throws Exception {
        Class<?> clazz = target.getClass();
        Method method = getMethod(clazz, methodName, args);
        return method.invoke(target, args);
    }

    public static Object invokeStatic(Class<?> clazz, String methodName, Object... args) throws Exception {
        Method method = getMethod(clazz, methodName, args);
        return method.invoke(null, args);
    }

    private static Method getMethod(Class<?> clazz, String name, Object[] args) throws NoSuchMethodException {
        String key = clazz.getName() + "#" + name + "#" + getParamSignature(args);
        return METHOD_CACHE.computeIfAbsent(key, k -> {
            try {
                for (Method m : clazz.getDeclaredMethods()) {
                    if (m.getName().equals(name) && m.getParameterCount() == args.length) {
                        m.setAccessible(true);
                        return m;
                    }
                }
                throw new NoSuchMethodException(name);
            } catch (Exception e) {
                return null;
            }
        });
    }

    public static Object getFieldValue(Object target, String fieldName) throws Exception {
        Field field = getField(target.getClass(), fieldName);
        return field.get(target);
    }

    public static void setFieldValue(Object target, String fieldName, Object value) throws Exception {
        Field field = getField(target.getClass(), fieldName);
        field.set(target, value);
    }

    private static Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
        String key = clazz.getName() + "." + name;
        return FIELD_CACHE.computeIfAbsent(key, k -> {
            try {
                Field f = clazz.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (Exception e) {
                return null;
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> clazz, Object... args) throws Exception {
        Constructor<?> constructor = getConstructor(clazz, args);
        return (T) constructor.newInstance(args);
    }

    private static Constructor<?> getConstructor(Class<?> clazz, Object[] args) throws NoSuchMethodException {
        String key = clazz.getName() + "#" + getParamSignature(args);
        return CONSTRUCTOR_CACHE.computeIfAbsent(key, k -> {
            for (Constructor<?> c : clazz.getDeclaredConstructors()) {
                if (c.getParameterCount() == args.length) {
                    c.setAccessible(true);
                    return c;
                }
            }
            return null;
        });
    }

    private static String getParamSignature(Object[] args) {
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            sb.append(arg == null ? "null" : arg.getClass().getName()).append(",");
        }
        return sb.toString();
    }

    public static boolean isPublic(AccessibleObject o) {
        if (o instanceof Method) return Modifier.isPublic(((Method) o).getModifiers());
        if (o instanceof Field) return Modifier.isPublic(((Field) o).getModifiers());
        if (o instanceof Constructor) return Modifier.isPublic(((Constructor<?>) o).getModifiers());
        return false;
    }

    public static void makeAccessible(AccessibleObject o) {
        if (!o.isAccessible()) o.setAccessible(true);
    }

    public static Map<String, Object> extractFields(Object target) {
        Map<String, Object> map = new java.util.HashMap<>();
        for (Field f : target.getClass().getDeclaredFields()) {
            try {
                f.setAccessible(true);
                map.put(f.getName(), f.get(target));
            } catch (Exception ignored) {}
        }
        return map;
    }

    public static void copyFields(Object source, Object target) {
        for (Field sf : source.getClass().getDeclaredFields()) {
            try {
                sf.setAccessible(true);
                Field tf = target.getClass().getDeclaredField(sf.getName());
                tf.setAccessible(true);
                tf.set(target, sf.get(source));
            } catch (Exception ignored) {}
        }
    }

    public static boolean hasAnnotation(AccessibleObject o, Class<? extends java.lang.annotation.Annotation> ann) {
        return o.isAnnotationPresent(ann);
    }

    public static java.util.List<Method> getMethodsWithAnnotation(Class<?> clazz, Class<? extends java.lang.annotation.Annotation> ann) {
        return java.util.Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(ann))
                .collect(java.util.stream.Collectors.toList());
    }

    public static void clearCache() {
        METHOD_CACHE.clear();
        FIELD_CACHE.clear();
        CONSTRUCTOR_CACHE.clear();
    }

    public static Object cloneObject(Object target) throws Exception {
        Object clone = newInstance(target.getClass());
        copyFields(target, clone);
        return clone;
    }

    public static Class<?>[] getParameterTypes(Method m) {
        return m.getParameterTypes();
    }

    public static boolean isStatic(Method m) {
        return Modifier.isStatic(m.getModifiers());
    }

    public static boolean isFinal(Field f) {
        return Modifier.isFinal(f.getModifiers());
    }

    public static void invokeAll(Object target, String methodName) {
        for (Method m : target.getClass().getDeclaredMethods()) {
            if (m.getName().equals(methodName)) {
                try {
                    m.setAccessible(true);
                    m.invoke(target);
                } catch (Exception ignored) {}
            }
        }
    }

    public static <T> T getFieldValueSafe(Object target, String fieldName, T defaultValue) {
        try {
            return (T) getFieldValue(target, fieldName);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static void executeWithCleanup(Object target, String methodName, Runnable cleanup) {
        try {
            invoke(target, methodName);
        } catch (Exception ignored) {
        } finally {
            cleanup.run();
        }
    }

    public static java.util.List<Field> getAllFields(Class<?> clazz) {
        java.util.List<Field> fields = new java.util.ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(java.util.Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    public static boolean isSynthetic(java.lang.reflect.Member m) {
        return m.isSynthetic();
    }

    public static void setFinalField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);
        f.set(target, value);
    }
}
