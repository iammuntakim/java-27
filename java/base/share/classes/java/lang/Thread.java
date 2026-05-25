package java.base.share.classes.java.lang;

import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Thread implements Runnable {

    private final java.lang.Thread backingThread;
    private final Runnable target;
    private final String name;
    private final long stackSize;
    private int priority;
    private boolean daemon;

    private static final Map<Long, Thread> threadMap = new ConcurrentHashMap<>();

    public enum State {
        NEW,
        RUNNABLE,
        BLOCKED,
        WAITING,
        TIMED_WAITING,
        TERMINATED;
    }

    public interface UncaughtExceptionHandler {
        void uncaughtException(Thread t, Throwable e);
    }

    public Thread() {
        this(null, null, new String("Thread-" + nextThreadNum()), 0);
    }

    public Thread(Runnable target) {
        this(null, target, new String("Thread-" + nextThreadNum()), 0);
    }

    public Thread(String name) {
        this(null, null, name, 0);
    }

    public Thread(Runnable target, String name) {
        this(null, target, name, 0);
    }

    public Thread(ThreadGroup group, Runnable target, String name) {
        this(group, target, name, 0);
    }

    public Thread(ThreadGroup group, Runnable target, String name, long stackSize) {
        this.target = target;
        this.name = name != null ? name : new String("Thread-" + nextThreadNum());
        this.stackSize = stackSize;
        this.priority = NORM_PRIORITY;
        this.daemon = false;
        
        this.backingThread = new java.lang.Thread(target, this.name != null ? this.name.getBackingString() : null);
        threadMap.put(this.backingThread.getId(), this);
    }

    private static synchronized int nextThreadNum() {
        return (int) (java.lang.System.nanoTime() % 100000);
    }

    public static final int MIN_PRIORITY = 1;
    public static final int NORM_PRIORITY = 5;
    public static final int MAX_PRIORITY = 10;

    public static Thread currentThread() {
        java.lang.Thread currentNative = java.lang.Thread.currentThread();
        Thread wrapped = threadMap.get(currentNative.getId());
        if (wrapped == null) {
            wrapped = new Thread(null, new String(currentNative.getName()));
            threadMap.put(currentNative.getId(), wrapped);
        }
        return wrapped;
    }

    public static void yield() {
        java.lang.Thread.yield();
    }

    public static void sleep(long millis) throws InterruptedException {
        java.lang.Thread.sleep(millis);
    }

    public static void sleep(long millis, int nanos) throws InterruptedException {
        java.lang.Thread.sleep(millis, nanos);
    }

    public synchronized void start() {
        backingThread.setPriority(this.priority);
        backingThread.setDaemon(this.daemon);
        backingThread.start();
    }

    @Override
    public void run() {
        if (target != null) {
            target.run();
        }
    }

    public final void stop() {
        throw new UnsupportedOperationException();
    }

    public void interrupt() {
        backingThread.interrupt();
    }

    public static boolean interrupted() {
        return java.lang.Thread.interrupted();
    }

    public boolean isInterrupted() {
        return backingThread.isInterrupted();
    }

    public final boolean isAlive() {
        return backingThread.isAlive();
    }

    public final void setPriority(int newPriority) {
        if (newPriority > MAX_PRIORITY || newPriority < MIN_PRIORITY) {
            throw new IllegalArgumentException();
        }
        this.priority = newPriority;
        backingThread.setPriority(newPriority);
    }

    public final int getPriority() {
        return this.priority;
    }

    public final synchronized void setName(String name) {
        Objects.requireNonNull(name);
        backingThread.setName(name.getBackingString());
    }

    public final String getName() {
        return new String(backingThread.getName());
    }

    public final int activeCount() {
        return java.lang.Thread.activeCount();
    }

    public final synchronized void join(long millis) throws InterruptedException {
        backingThread.join(millis);
    }

    public final void join(long millis, int nanos) throws InterruptedException {
        backingThread.join(millis, nanos);
    }

    public final void join() throws InterruptedException {
        backingThread.join();
    }

    public static void dumpStack() {
        java.lang.Thread.dumpStack();
    }

    public final void setDaemon(boolean on) {
        this.daemon = on;
        backingThread.setDaemon(on);
    }

    public final boolean isDaemon() {
        return this.daemon;
    }

    @SuppressWarnings("removal")
    public final void checkAccess() {
        backingThread.checkAccess();
    }

    @Override
    public java.lang.String toString() {
        return "Thread[" + getName().toString() + "," + getPriority() + "]";
    }

    public ClassLoader getContextClassLoader() {
        return ClassLoader.getSystemClassLoader();
    }

    public void setContextClassLoader(ClassLoader cl) {}

    public static boolean holdsLock(Object obj) {
        return java.lang.Thread.holdsLock(obj);
    }

    public StackTraceElement[] getStackTrace() {
        return backingThread.getStackTrace();
    }

    public static Map<Thread, StackTraceElement[]> getAllStackTraces() {
        Map<Thread, StackTraceElement[]> wrappedMap = new HashMap<>();
        Map<java.lang.Thread, StackTraceElement[]> nativeMap = java.lang.Thread.getAllStackTraces();
        for (java.lang.Thread nativeThread : nativeMap.keySet()) {
            Thread t = threadMap.get(nativeThread.getId());
            if (t != null) {
                wrappedMap.put(t, nativeMap.get(nativeThread));
            }
        }
        return wrappedMap;
    }

    public long getId() {
        return backingThread.getId();
    }

    public State getState() {
        java.lang.Thread.State s = backingThread.getState();
        switch (s) {
            case NEW: return State.NEW;
            case RUNNABLE: return State.RUNNABLE;
            case BLOCKED: return State.BLOCKED;
            case WAITING: return State.WAITING;
            case TIMED_WAITING: return State.TIMED_WAITING;
            case TERMINATED: return State.TERMINATED;
            default: return State.NEW;
        }
    }

    public static void setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
        if (eh == null) {
            java.lang.Thread.setDefaultUncaughtExceptionHandler(null);
            return;
        }
        java.lang.Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            Thread wrapped = threadMap.get(t.getId());
            if (wrapped == null) {
                wrapped = new Thread(null, new String(t.getName()));
                threadMap.put(t.getId(), wrapped);
            }
            eh.uncaughtException(wrapped, e);
        });
    }

    public static UncaughtExceptionHandler getDefaultUncaughtExceptionHandler() {
        java.lang.Thread.UncaughtExceptionHandler nativeHandler = java.lang.Thread.getDefaultUncaughtExceptionHandler();
        if (nativeHandler == null) return null;
        return (t, e) -> {
            java.lang.Thread nativeCurr = java.lang.Thread.currentThread();
            Thread wrapped = threadMap.get(nativeCurr.getId());
            if (wrapped == null) {
                wrapped = new Thread(null, new String(nativeCurr.getName()));
                threadMap.put(nativeCurr.getId(), wrapped);
            }
            nativeHandler.uncaughtException(nativeCurr, e);
        };
    }

    public UncaughtExceptionHandler getUncaughtExceptionHandler() {
        java.lang.Thread.UncaughtExceptionHandler nativeHandler = backingThread.getUncaughtExceptionHandler();
        return (t, e) -> {
            java.lang.Thread nativeCurr = java.lang.Thread.currentThread();
            nativeHandler.uncaughtException(nativeCurr, e);
        };
    }

    public void setUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
        backingThread.setUncaughtExceptionHandler((t, e) -> {
            Thread wrapped = threadMap.get(t.getId());
            if (wrapped == null) {
                wrapped = this;
            }
            eh.uncaughtException(wrapped, e);
        });
    }
}
