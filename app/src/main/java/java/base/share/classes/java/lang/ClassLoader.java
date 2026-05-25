package java.base.share.classes.java.lang;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ClassLoader {

    private final ClassLoader parent;
    private final ConcurrentHashMap<String, Object> parallelLockMap;
    private final Map<String, Class<?>> loadedClasses;

    protected ClassLoader(ClassLoader parent) {
        this.parent = parent;
        this.parallelLockMap = new ConcurrentHashMap<>();
        this.loadedClasses = new ConcurrentHashMap<>();
    }

    protected ClassLoader() {
        this(getSystemClassLoader());
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                try {
                    if (parent != null) {
                        c = parent.loadClass(name, false);
                    } else {
                        c = findBootstrapClassOrNull(name);
                    }
                } catch (ClassNotFoundException e) {
                }

                if (c == null) {
                    c = findClass(name);
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }

    protected Object getClassLoadingLock(String className) {
        Object newLock = new Object();
        Object oldLock = parallelLockMap.putIfAbsent(className, newLock);
        return (oldLock != null) ? oldLock : newLock;
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        throw new ClassNotFoundException(name);
    }

    protected final Class<?> defineClass(String name, byte[] b, int off, int len) throws ClassFormatError {
        Objects.requireNonNull(b);
        try {
            Class<?> c = (Class<?>) java.lang.ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class)
                .invoke(java.lang.ClassLoader.getSystemClassLoader(), name, b, off, len);
            if (name != null) {
                loadedClasses.put(name, c);
            }
            return c;
        } catch (Exception e) {
            throw new ClassFormatError(e.getMessage());
        }
    }

    protected final void resolveClass(Class<?> c) {
        Objects.requireNonNull(c);
    }

    protected final Class<?> findLoadedClass(String name) {
        return loadedClasses.get(name);
    }

    public URL getResource(String name) {
        Objects.requireNonNull(name);
        URL url;
        if (parent != null) {
            url = parent.getResource(name);
        } else {
            url = findBootstrapResource(name);
        }
        if (url == null) {
            url = findResource(name);
        }
        return url;
    }

    public java.util.Enumeration<URL> getResources(String name) throws IOException {
        Objects.requireNonNull(name);
        java.util.Enumeration<URL>[] resources = (java.util.Enumeration<URL>[]) new java.util.Enumeration<?>[2];
        if (parent != null) {
            resources[0] = parent.getResources(name);
        } else {
            resources[0] = findBootstrapResources(name);
        }
        resources[1] = findResourceEnumeration(name);
        return new CompoundEnumeration<>(resources);
    }

    protected URL findResource(String name) {
        return null;
    }

    private java.util.Enumeration<URL> findResourceEnumeration(String name) throws IOException {
        return new Vector<URL>().elements();
    }

    private URL findBootstrapResource(String name) {
        return java.lang.ClassLoader.getSystemResource(name);
    }

    private java.util.Enumeration<URL> findBootstrapResources(String name) throws IOException {
        return java.lang.ClassLoader.getSystemResources(name);
    }

    public static ClassLoader getSystemClassLoader() {
        return SystemClassLoaderHolder.loader;
    }

    public final ClassLoader getParent() {
        return parent;
    }

    public InputStream getResourceAsStream(String name) {
        URL url = getResource(name);
        try {
            return url != null ? url.openStream() : null;
        } catch (IOException e) {
            return null;
        }
    }

    private Class<?> findBootstrapClassOrNull(String name) {
        try {
            return Class.forName(name, false, null);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static URL getSystemResource(String name) {
        ClassLoader system = getSystemClassLoader();
        if (system == null) {
            return java.lang.ClassLoader.getSystemResource(name);
        }
        return system.getResource(name);
    }

    public static java.util.Enumeration<URL> getSystemResources(String name) throws IOException {
        ClassLoader system = getSystemClassLoader();
        if (system == null) {
            return java.lang.ClassLoader.getSystemResources(name);
        }
        return system.getResources(name);
    }

    public static InputStream getSystemResourceAsStream(String name) {
        ClassLoader system = getSystemClassLoader();
        if (system == null) {
            return java.lang.ClassLoader.getSystemResourceAsStream(name);
        }
        return system.getResourceAsStream(name);
    }

    public void setDefaultAssertionStatus(boolean enabled) {}

    public void setPackageAssertionStatus(String packageName, boolean enabled) {}

    public void setClassAssertionStatus(String className, boolean enabled) {}

    public void clearAssertionStatus() {}

    private static class SystemClassLoaderHolder {
        static final ClassLoader loader = new ClassLoader(null) {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                return Class.forName(name);
            }
        };
    }

    private static class CompoundEnumeration<E> implements java.util.Enumeration<E> {
        private final java.util.Enumeration<E>[] enums;
        private int index = 0;

        public CompoundEnumeration(java.util.Enumeration<E>[] enums) {
            this.enums = enums;
        }

        private boolean next() {
            while (index < enums.length) {
                if (enums[index] != null && enums[index].hasMoreElements()) {
                    return true;
                }
                index++;
            }
            return false;
        }

        public boolean hasMoreElements() {
            return next();
        }

        public E nextElement() {
            if (!next()) {
                throw new java.util.NoSuchElementException();
            }
            return enums[index].nextElement();
        }
    }
}
