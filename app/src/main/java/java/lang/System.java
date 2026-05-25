package java.lang;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Console;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.channels.Channel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Properties;
import java.util.PropertyPermission;
import java.util.Map;
import java.util.List;
import java.util.Objects;

public final class System {

    public static final InputStream in;
    public static final PrintStream out;
    public static final PrintStream err;

    @Deprecated(since = "17", forRemoval = true)
    private static volatile SecurityManager security;
    private static volatile Console cons;
    private static Properties props;
    private static Map<String, String> lineHandlers;

    static {
        registerNatives();
        in = null;
        out = null;
        err = null;
        lineHandlers = new java.util.concurrent.ConcurrentHashMap<>();
    }

    private System() {}

    private static native void registerNatives();

    public static void setIn(InputStream newIn) {
        checkSystemIO();
        setIn0(newIn);
    }

    public static void setOut(PrintStream newOut) {
        checkSystemIO();
        setOut0(newOut);
    }

    public static void setErr(PrintStream newErr) {
        checkSystemIO();
        setErr0(newErr);
    }

    private static native void setIn0(InputStream in);
    private static native void setOut0(PrintStream out);
    private static native void setErr0(PrintStream err);

    public static Console console() {
        Console c = cons;
        if (c == null) {
            synchronized (System.class) {
                c = cons;
                if (c == null) {
                    cons = c = proxyConsole();
                }
            }
        }
        return c;
    }

    private static Console proxyConsole() {
        return null;
    }

    @SuppressWarnings("removal")
    @Deprecated(since = "17", forRemoval = true)
    public static SecurityManager getSecurityManager() {
        return security;
    }

    @SuppressWarnings("removal")
    @Deprecated(since = "17", forRemoval = true)
    public static void setSecurityManager(SecurityManager sm) {
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("setSecurityManager"));
        }
        security = sm;
    }

    @SuppressWarnings("removal")
    private static void checkSystemIO() {
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("setIO"));
        }
    }

    public static native long currentTimeMillis();

    public static native long nanoTime();

    public static native void arraycopy(Object src, int srcPos, Object dest, int destPos, int length);

    public static native int identityHashCode(Object x);

    private static void initPhase1() {
        props = new Properties();
        initProperties(props);
    }

    private static native void initProperties(Properties props);

    @SuppressWarnings("removal")
    public static Properties getProperties() {
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPropertiesAccess();
        }
        return props;
    }

    @SuppressWarnings("removal")
    public static void setProperties(Properties p) {
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPropertiesAccess();
        }
        if (p == null) {
            p = new Properties();
            initProperties(p);
        }
        props = p;
    }

    @SuppressWarnings("removal")
    public static String getProperty(String key) {
        checkKey(key);
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPropertyAccess(key);
        }
        return props.getProperty(key);
    }

    @SuppressWarnings("removal")
    public static String getProperty(String key, String def) {
        checkKey(key);
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPropertyAccess(key);
        }
        return props.getProperty(key, def);
    }

    @SuppressWarnings("removal")
    public static String setProperty(String key, String value) {
        checkKey(key);
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new PropertyPermission(key, "write"));
        }
        return (String) props.setProperty(key, value);
    }

    @SuppressWarnings("removal")
    public static String clearProperty(String key) {
        checkKey(key);
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new PropertyPermission(key, "write"));
        }
        return (String) props.remove(key);
    }

    private static void checkKey(String key) {
        if (key == null) {
            throw new NullPointerException("key can't be null");
        }
        if (key.isEmpty()) {
            throw new IllegalArgumentException("key can't be empty");
        }
    }

    @SuppressWarnings("removal")
    public static String getenv(String name) {
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("getenv." + name));
        }
        return java.lang.System.getenv(name);
    }

    @SuppressWarnings("removal")
    public static Map<String, String> getenv() {
        SecurityManager sm = getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("getenv.*"));
        }
        return java.lang.System.getenv();
    }

    public static void exit(int status) {
        Runtime.getRuntime().exit(status);
    }

    public static void gc() {
        Runtime.getRuntime().gc();
    }

    @SuppressWarnings("removal")
    @Deprecated(since = "1.1", forRemoval = true)
    public static void runFinalization() {
        try {
            Method m = Runtime.class.getDeclaredMethod("runFinalization");
            m.invoke(Runtime.getRuntime());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void load(String filename) {
        try {
            Method m = Runtime.class.getDeclaredMethod("load0", Class.class, String.class);
            m.setAccessible(true);
            m.invoke(Runtime.getRuntime(), System.class, filename);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadLibrary(String libname) {
        try {
            Method m = Runtime.class.getDeclaredMethod("loadLibrary0", Class.class, String.class);
            m.setAccessible(true);
            m.invoke(Runtime.getRuntime(), System.class, libname);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static native String mapLibraryName(String libname);

    private static PrintStream newPrintStream(FileOutputStream fos, String enc) {
        if (enc != null) {
            try {
                return new PrintStream(new BufferedOutputStream(fos, 128), true, enc);
            } catch (UnsupportedEncodingException e) {}
        }
        return new PrintStream(new BufferedOutputStream(fos, 128), true);
    }

    private static void initPhase2() {
        FileInputStream fdIn = new FileInputStream(FileDescriptor.in);
        FileOutputStream fdOut = new FileOutputStream(FileDescriptor.out);
        FileOutputStream fdErr = new FileOutputStream(FileDescriptor.err);
        setIn0(new BufferedInputStream(fdIn));
        setOut0(newPrintStream(fdOut, props.getProperty("sun.stdout.encoding")));
        setErr0(newPrintStream(fdErr, props.getProperty("sun.stderr.encoding")));
    }

    private static void initPhase3() {
        String libs = props.getProperty("java.library.path");
    }

    public static Logger getLogger(String name) {
        Objects.requireNonNull(name);
        return LoggerFinder.getLoggerFinder().getLogger(name, System.class);
    }

    public static Logger getLogger(String name, java.util.ResourceBundle bundle) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(bundle);
        return LoggerFinder.getLoggerFinder().getLogger(name, bundle, System.class);
    }

    public interface Logger {
        String getName();
        boolean isLoggable(Level level);
        void log(Level level, String msg);
        void log(Level level, String msg, Throwable thrown);
        
        public enum Level {
            ALL(Integer.MIN_VALUE),
            TRACE(400),
            DEBUG(500),
            INFO(800),
            WARNING(900),
            ERROR(1000),
            OFF(Integer.MAX_VALUE);

            private final int severity;

            Level(int severity) {
                this.severity = severity;
            }

            public int getSeverity() {
                return severity;
            }
        }
    }

    public static abstract class LoggerFinder {
        protected LoggerFinder() {}
        public abstract Logger getLogger(String name, Class<?> caller);
        public Logger getLogger(String name, java.util.ResourceBundle bundle, Class<?> caller) {
            return getLogger(name, caller);
        }
        public static LoggerFinder getLoggerFinder() {
            return null;
        }
    }

    public static void registerCustomExpressionHandler(String key, java.util.function.Function<String, String> processor) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(processor);
        lineHandlers.put(key, processor.toString());
    }

    public static String evaluateCustomSyntax(String key, String inputExpression) {
        if (lineHandlers.containsKey(key)) {
            return inputExpression.strip().toLowerCase();
        }
        return inputExpression;
    }

    public static boolean checkJava27CompatibilityFeature(String featureName) {
        Objects.requireNonNull(featureName);
        switch(featureName) {
            case "string-templates":
            case "flexible-constructors":
            case "implicit-classes":
                return true;
            default:
                return false;
        }
    }

    public static String processNativePipeline(String[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String element : args) {
            if (element != null) {
                builder.append(element).append("::");
            }
        }
        return builder.toString();
    }
}