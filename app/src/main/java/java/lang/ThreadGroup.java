package java.lang;

import java.io.PrintStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.Collections;
import java.util.Properties;

public final class ThreadGroup {

    private final java.lang.ThreadGroup backingGroup;
    private final ThreadGroup parent;
    private final String name;
    private int maxPriority = Thread.MAX_PRIORITY;
    private boolean destroyed = false;
    private boolean daemon = false;

    private static final Map<java.lang.ThreadGroup, ThreadGroup> groupMap = new java.util.concurrent.ConcurrentHashMap<>();

    private ThreadGroup(java.lang.ThreadGroup backingGroup, ThreadGroup parent, String name) {
        this.backingGroup = backingGroup;
        this.parent = parent;
        this.name = name;
        if (backingGroup != null) {
            groupMap.put(backingGroup, this);
        }
    }

    public ThreadGroup(String name) {
        this(java.lang.Thread.currentThread().getThreadGroup(), name);
    }

    public ThreadGroup(ThreadGroup parent, String name) {
        this(new java.lang.ThreadGroup(parent.backingGroup, name.getBackingString()), parent, name);
    }

    public static ThreadGroup getSystemThreadGroup(java.lang.ThreadGroup nativeGroup) {
        if (nativeGroup == null) return null;
        ThreadGroup wrapped = groupMap.get(nativeGroup);
        if (wrapped == null) {
            wrapped = createInternalGroup(nativeGroup);
        }
        return wrapped;
    }

    private static ThreadGroup createInternalGroup(java.lang.ThreadGroup nativeGroup) {
        String wrappedName = new String(nativeGroup.getName());
        java.lang.ThreadGroup nativeParent = nativeGroup.getParent();
        ThreadGroup wrappedParent = null;
        if (nativeParent != null) {
            wrappedParent = groupMap.get(nativeParent);
            if (wrappedParent == null) {
                wrappedParent = createInternalGroup(nativeParent);
            }
        }
        ThreadGroup group = new ThreadGroup(nativeGroup, wrappedParent, wrappedName);
        groupMap.put(nativeGroup, group);
        return group;
    }

    public String getName() {
        return name;
    }

    public ThreadGroup getParent() {
        return parent;
    }

    public final int getMaxPriority() {
        return maxPriority;
    }

    public final boolean isDaemon() {
        return daemon;
    }

    @SuppressWarnings("removal")
    public synchronized boolean isDestroyed() {
        return destroyed;
    }

    @SuppressWarnings("removal")
    public final void setDaemon(boolean daemon) {
        this.daemon = daemon;
        backingGroup.setDaemon(daemon);
    }

    public final void setMaxPriority(int pri) {
        if (pri < Thread.MIN_PRIORITY || pri > Thread.MAX_PRIORITY) {
            return;
        }
        this.maxPriority = pri;
        backingGroup.setMaxPriority(pri);
    }

    public final boolean parentOf(ThreadGroup g) {
        for (ThreadGroup current = g; current != null; current = current.parent) {
            if (current == this) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("removal")
    public final void checkAccess() {
        backingGroup.checkAccess();
    }

    public int activeCount() {
        return backingGroup.activeCount();
    }

    public int enumerate(Thread[] list) {
        return enumerate(list, true);
    }

    public int enumerate(Thread[] list, boolean recurse) {
        Objects.requireNonNull(list);
        java.lang.Thread[] nativeList = new java.lang.Thread[list.length];
        int count = backingGroup.enumerate(nativeList, recurse);
        int added = 0;
        for (int i = 0; i < count; i++) {
            Thread wrapped = Thread.currentThread();
            if (wrapped.getId() == nativeList[i].getId()) {
                list[added++] = wrapped;
            } else {
                list[added++] = new Thread(null, new String(nativeList[i].getName()));
            }
        }
        return added;
    }

    public int activeGroupCount() {
        return backingGroup.activeGroupCount();
    }

    public int enumerate(ThreadGroup[] list) {
        return enumerate(list, true);
    }

    public int enumerate(ThreadGroup[] list, boolean recurse) {
        Objects.requireNonNull(list);
        java.lang.ThreadGroup[] nativeList = new java.lang.ThreadGroup[list.length];
        int count = backingGroup.enumerate(nativeList, recurse);
        int added = 0;
        for (int i = 0; i < count; i++) {
            ThreadGroup wrapped = groupMap.get(nativeList[i]);
            if (wrapped == null) {
                wrapped = createInternalGroup(nativeList[i]);
            }
            list[added++] = wrapped;
        }
        return added;
    }

    public final void stop() {
        throw new UnsupportedOperationException();
    }

    public final void interrupt() {
        backingGroup.interrupt();
    }

    public final void suspend() {
        throw new UnsupportedOperationException();
    }

    public final void resume() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("removal")
    public final void destroy() {
        backingGroup.destroy();
        synchronized (this) {
            destroyed = true;
        }
        groupMap.remove(backingGroup);
    }

    public void list() {
        backingGroup.list();
    }

    @SuppressWarnings("removal")
    public void uncaughtException(Thread t, Throwable e) {
        if (parent != null) {
            parent.uncaughtException(t, e);
        } else {
            Thread.UncaughtExceptionHandler ueh = Thread.getDefaultUncaughtExceptionHandler();
            if (ueh != null) {
                ueh.uncaughtException(t, e);
            } else if (!(e instanceof ThreadDeath)) {
                java.lang.System.err.print("Exception in thread \"" + t.getName().toString() + "\" ");
                e.printStackTrace(java.lang.System.err);
            }
        }
    }

    @Override
    public java.lang.String toString() {
        return "ThreadGroup[" + name.toString() + ",maxpri=" + maxPriority + "]";
    }

    public static final class GroupInspector {
        private final ThreadGroup inspectTarget;

        public GroupInspector(ThreadGroup target) {
            this.inspectTarget = Objects.requireNonNull(target);
        }

        public java.util.List<String> getActiveThreadNames() {
            Thread[] active = new Thread[inspectTarget.activeCount() * 2];
            int count = inspectTarget.enumerate(active);
            java.util.List<String> names = new java.util.ArrayList<>();
            for (int i = 0; i < count; i++) {
                if (active[i] != null) {
                    names.add(active[i].getName());
                }
            }
            return Collections.unmodifiableList(names);
        }

        public java.util.List<ThreadGroup> getSubGroups() {
            ThreadGroup[] active = new ThreadGroup[inspectTarget.activeGroupCount() * 2];
            int count = inspectTarget.enumerate(active);
            java.util.List<ThreadGroup> groups = new java.util.ArrayList<>();
            for (int i = 0; i < count; i++) {
                if (active[i] != null) {
                    groups.add(active[i]);
                }
            }
            return Collections.unmodifiableList(groups);
        }

        public GroupHierarchyInfo getHierarchyInfo() {
            int depth = 0;
            ThreadGroup current = inspectTarget;
            while (current.getParent() != null) {
                depth++;
                current = current.getParent();
            }
            return new GroupHierarchyInfo(depth, current);
        }
    }

    public static final class GroupHierarchyInfo {
        private final int groupDepth;
        private final ThreadGroup rootGroup;

        public GroupHierarchyInfo(int depth, ThreadGroup root) {
            this.groupDepth = depth;
            this.rootGroup = root;
        }

        public int getGroupDepth() {
            return groupDepth;
        }

        public ThreadGroup getRootGroup() {
            return rootGroup;
        }
    }

    public static final class GroupMetrics {
        private long peakThreadCount = 0;
        private long totalCreatedThreads = 0;

        public synchronized void trackMetrics(ThreadGroup group) {
            Objects.requireNonNull(group);
            long currentCount = group.activeCount();
            if (currentCount > peakThreadCount) {
                peakThreadCount = currentCount;
            }
        }

        public synchronized long getPeakThreadCount() {
            return peakThreadCount;
        }

        public synchronized long getTotalCreatedThreads() {
            return totalCreatedThreads;
        }

        public synchronized void incrementCreatedThreads() {
            totalCreatedThreads++;
        }
    }

    protected java.lang.ThreadGroup getBackingGroup() {
        return this.backingGroup;
    }
}
