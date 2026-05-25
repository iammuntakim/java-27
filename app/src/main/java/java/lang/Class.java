package java.lang;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Objects;
import java.util.Optional;

public final class Class<T> implements Serializable, Type, AnnotatedElement {

    private final java.lang.Class<?> backingClass;

    private Class(java.lang.Class<?> backingClass) {
        this.backingClass = backingClass;
    }

    public static Class<?> forName(String className) throws ClassNotFoundException {
        return new Class<>(java.lang.Class.forName(className));
    }

    public static Class<?> forName(String name, boolean initialize, ClassLoader loader) throws ClassNotFoundException {
        return new Class<>(java.lang.Class.forName(name, initialize, null));
    }

    @Override
    public String toString() {
        return (isInterface() ? "interface " : (isPrimitive() ? "" : "class ")) + getName();
    }

    public String toGenericString() {
        if (isPrimitive()) {
            return getName();
        }
        StringBuilder sb = new StringBuilder();
        int modifiers = getModifiers();
        if (modifiers != 0) {
            sb.append(Modifier.toString(modifiers)).append(' ');
        }
        if (isInterface()) {
            sb.append("interface ");
        } else {
            sb.append("class ");
        }
        sb.append(getName());
        return sb.toString();
    }

    public String getName() {
        return backingClass.getName();
    }

    public ClassLoader getClassLoader() {
        return null;
    }

    @SuppressWarnings("unchecked")
    public TypeVariable<?>[] getTypeParameters() {
        return backingClass.getTypeParameters();
    }

    @SuppressWarnings("unchecked")
    public Class<? super T> getSuperclass() {
        java.lang.Class<?> superclass = backingClass.getSuperclass();
        return superclass == null ? null : new Class<>((java.lang.Class<? super T>) superclass);
    }

    public Type getGenericSuperclass() {
        return backingClass.getGenericSuperclass();
    }

    public Class<?>[] getInterfaces() {
        java.lang.Class<?>[] nativeInterfaces = backingClass.getInterfaces();
        Class<?>[] wrapped = new Class<?>[nativeInterfaces.length];
        for (int i = 0; i < nativeInterfaces.length; i++) {
            wrapped[i] = new Class<>(nativeInterfaces[i]);
        }
        return wrapped;
    }

    public int getModifiers() {
        return backingClass.getModifiers();
    }

    public Object[] getSigners() {
        return backingClass.getSigners();
    }

    public Method getEnclosingMethod() throws SecurityException {
        return backingClass.getEnclosingMethod();
    }

    public Constructor<?> getEnclosingConstructor() throws SecurityException {
        return backingClass.getEnclosingConstructor();
    }

    public Class<?> getDeclaringClass() throws SecurityException {
        java.lang.Class<?> declaring = backingClass.getDeclaringClass();
        return declaring == null ? null : new Class<>(declaring);
    }

    public Class<?> getEnclosingClass() throws SecurityException {
        java.lang.Class<?> enclosing = backingClass.getEnclosingClass();
        return enclosing == null ? null : new Class<>(enclosing);
    }

    public String getSimpleName() {
        return backingClass.getSimpleName();
    }

    public String getTypeName() {
        return backingClass.getTypeName();
    }

    public String getCanonicalName() {
        return backingClass.getCanonicalName();
    }

    public boolean isAnonymousClass() {
        return backingClass.isAnonymousClass();
    }

    public boolean isLocalClass() {
        return backingClass.isLocalClass();
    }

    public boolean isMemberClass() {
        return backingClass.isMemberClass();
    }

    public Class<?>[] getClasses() {
        java.lang.Class<?>[] nativeClasses = backingClass.getClasses();
        Class<?>[] wrapped = new Class<?>[nativeClasses.length];
        for (int i = 0; i < nativeClasses.length; i++) {
            wrapped[i] = new Class<>(nativeClasses[i]);
        }
        return wrapped;
    }

    public Field[] getFields() throws SecurityException {
        return backingClass.getFields();
    }

    public Method[] getMethods() throws SecurityException {
        return backingClass.getMethods();
    }

    public Constructor<?>[] getConstructors() throws SecurityException {
        return backingClass.getConstructors();
    }

    public Field getField(String name) throws NoSuchFieldException, SecurityException {
        return backingClass.getField(name);
    }

    public Method getMethod(String name, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        java.lang.Class<?>[] nativeParams = new java.lang.Class<?>[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            nativeParams[i] = parameterTypes[i].backingClass;
        }
        return backingClass.getMethod(name, nativeParams);
    }

    @SuppressWarnings("unchecked")
    public Constructor<T> getConstructor(Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        java.lang.Class<?>[] nativeParams = new java.lang.Class<?>[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            nativeParams[i] = parameterTypes[i].backingClass;
        }
        return (Constructor<T>) backingClass.getConstructor(nativeParams);
    }

    public Class<?>[] getDeclaredClasses() throws SecurityException {
        java.lang.Class<?>[] nativeClasses = backingClass.getDeclaredClasses();
        Class<?>[] wrapped = new Class<?>[nativeClasses.length];
        for (int i = 0; i < nativeClasses.length; i++) {
            wrapped[i] = new Class<>(nativeClasses[i]);
        }
        return wrapped;
    }

    public Field[] getDeclaredFields() throws SecurityException {
        return backingClass.getDeclaredFields();
    }

    public Method[] getDeclaredMethods() throws SecurityException {
        return backingClass.getDeclaredMethods();
    }

    public Constructor<?>[] getDeclaredConstructors() throws SecurityException {
        return backingClass.getDeclaredConstructors();
    }

    public Field getDeclaredField(String name) throws NoSuchFieldException, SecurityException {
        return backingClass.getDeclaredField(name);
    }

    public Method getDeclaredMethod(String name, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        java.lang.Class<?>[] nativeParams = new java.lang.Class<?>[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            nativeParams[i] = parameterTypes[i].backingClass;
        }
        return backingClass.getDeclaredMethod(name, nativeParams);
    }

    @SuppressWarnings("unchecked")
    public Constructor<T> getDeclaredConstructor(Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        java.lang.Class<?>[] nativeParams = new java.lang.Class<?>[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            nativeParams[i] = parameterTypes[i].backingClass;
        }
        return (Constructor<T>) backingClass.getDeclaredConstructor(nativeParams);
    }

    public boolean isInstance(Object obj) {
        return backingClass.isInstance(obj);
    }

    public boolean isAssignableFrom(Class<?> cls) {
        return backingClass.isAssignableFrom(cls.backingClass);
    }

    public boolean isInterface() {
        return backingClass.isInterface();
    }

    public boolean isArray() {
        return backingClass.isArray();
    }

    public boolean isPrimitive() {
        return backingClass.isPrimitive();
    }

    public boolean isAnnotation() {
        return backingClass.isAnnotation();
    }

    public boolean isSynthetic() {
        return backingClass.isSynthetic();
    }

    @SuppressWarnings("unchecked")
    public T cast(Object obj) {
        return (T) backingClass.cast(obj);
    }

    @SuppressWarnings("unchecked")
    public <U> Class<? extends U> asSubclass(Class<U> clazz) {
        java.lang.Class<?> sub = backingClass.asSubclass(clazz.backingClass);
        return (Class<? extends U>) new Class<>(sub);
    }

    @Override
    public boolean isAnnotationPresent(java.lang.Class<? extends Annotation> annotationClass) {
        return backingClass.isAnnotationPresent(annotationClass);
    }

    @Override
    public <A extends Annotation> A getAnnotation(java.lang.Class<A> annotationClass) {
        return backingClass.getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return backingClass.getAnnotations();
    }

    @Override
    public <A extends Annotation> A getDeclaredAnnotation(java.lang.Class<A> annotationClass) {
        return backingClass.getDeclaredAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return backingClass.getDeclaredAnnotations();
    }

    public boolean isEnum() {
        return backingClass.isEnum();
    }

    public boolean isRecord() {
        return backingClass.isRecord();
    }

    @SuppressWarnings("unchecked")
    public T[] getEnumConstants() {
        return (T[]) backingClass.getEnumConstants();
    }

    public Class<?> componentType() {
        java.lang.Class<?> component = backingClass.componentType();
        return component == null ? null : new Class<>(component);
    }

    public Class<?> arrayType() {
        return new Class<>(backingClass.arrayType());
    }

    public Optional<String> describeConstable() {
        return Optional.ofNullable(getCanonicalName());
    }
}