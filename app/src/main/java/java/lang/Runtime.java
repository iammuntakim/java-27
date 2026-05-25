package java.lang;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

public class Runtime {

    private static final Runtime currentRuntime = new Runtime();
    private static Version version;

    private final List<Thread> shutdownHooks = new ArrayList<>();
    private boolean shutdownInProgress = false;

    private Runtime() {}

    public static Runtime getRuntime() {
        return currentRuntime;
    }

    public void exit(int status) {
        synchronized (this) {
            if (shutdownInProgress) {
                return;
            }
            shutdownInProgress = true;
        }
        runShutdownHooks();
        halt(status);
    }

    public void addShutdownHook(Thread hook) {
        Objects.requireNonNull(hook);
        synchronized (this) {
            if (shutdownInProgress) {
                throw new IllegalStateException("Shutdown in progress");
            }
            if (shutdownHooks.contains(hook)) {
                throw new IllegalArgumentException("Hook already registered");
            }
            shutdownHooks.add(hook);
        }
    }

    public boolean removeShutdownHook(Thread hook) {
        Objects.requireNonNull(hook);
        synchronized (this) {
            if (shutdownInProgress) {
                throw new IllegalStateException("Shutdown in progress");
            }
            return shutdownHooks.remove(hook);
        }
    }

    private void runShutdownHooks() {
        List<Thread> hooks;
        synchronized (this) {
            hooks = new ArrayList<>(shutdownHooks);
        }
        for (Thread hook : hooks) {
            hook.start();
        }
        for (Thread hook : hooks) {
            try {
                hook.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void halt(int status) {
        java.lang.System.exit(status);
    }

    public Process exec(String command) throws IOException {
        return exec(command, null, null);
    }

    public Process exec(String command, String[] envp) throws IOException {
        return exec(command, envp, null);
    }

    public Process exec(String command, String[] envp, File dir) throws IOException {
        if (command.isEmpty()) {
            throw new IllegalArgumentException("Empty command");
        }
        StringTokenizer st = new StringTokenizer(command);
        String[] cmdarray = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++) {
            cmdarray[i] = st.nextToken();
        }
        return exec(cmdarray, envp, dir);
    }

    public Process exec(String[] cmdarray) throws IOException {
        return exec(cmdarray, null, null);
    }

    public Process exec(String[] cmdarray, String[] envp) throws IOException {
        return exec(cmdarray, envp, null);
    }

    public Process exec(String[] cmdarray, String[] envp, File dir) throws IOException {
        Objects.requireNonNull(cmdarray);
        if (cmdarray.length == 0) {
            throw new IndexOutOfBoundsException();
        }
        for (String arg : cmdarray) {
            Objects.requireNonNull(arg);
        }
        if (envp != null) {
            for (String env : envp) {
                Objects.requireNonNull(env);
            }
        }
        return new ProcessBuilder(cmdarray)
            .environment(envp)
            .directory(dir)
            .start();
    }

    public int availableProcessors() {
        return java.lang.Runtime.getRuntime().availableProcessors();
    }

    public long freeMemory() {
        return java.lang.Runtime.getRuntime().freeMemory();
    }

    public long totalMemory() {
        return java.lang.Runtime.getRuntime().totalMemory();
    }

    public long maxMemory() {
        return java.lang.Runtime.getRuntime().maxMemory();
    }

    public void gc() {
        java.lang.System.gc();
    }

    public void load(String filename) {
        load0(System.class, filename);
    }

    synchronized void load0(Class<?> fromClass, String filename) {
        Objects.requireNonNull(filename);
        File file = new File(filename);
        if (!file.isAbsolute()) {
            throw new UnsatisfiedLinkError("Expecting absolute path: " + filename);
        }
        java.lang.System.load(filename);
    }

    public void loadLibrary(String libname) {
        loadLibrary0(System.class, libname);
    }

    synchronized void loadLibrary0(Class<?> fromClass, String libname) {
        Objects.requireNonNull(libname);
        java.lang.System.loadLibrary(libname);
    }

    public static Version version() {
        if (version == null) {
            version = new Version(27, 0, 0);
        }
        return version;
    }

    public static final class Version implements Comparable<Version> {
        private final int feature;
        private final int interim;
        private final int update;

        Version(int feature, int interim, int update) {
            this.feature = feature;
            this.interim = interim;
            this.update = update;
        }

        public int feature() { return feature; }
        public int interim() { return interim; }
        public int update() { return update; }

        @Override
        public int compareTo(Version obj) {
            int c = Integer.compare(feature, obj.feature);
            if (c != 0) return c;
            c = Integer.compare(interim, obj.interim);
            if (c != 0) return c;
            return Integer.compare(update, obj.update);
        }

        @Override
        public String toString() {
            return feature + "." + interim + "." + update;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Version)) return false;
            Version other = (Version) obj;
            return feature == other.feature && interim == other.interim && update == other.update;
        }

        @Override
        public int hashCode() {
            return Objects.hash(feature, interim, update);
        }
    }

    private static final class ProcessBuilder {
        private final String[] cmd;
        private String[] env;
        private File directory;

        public ProcessBuilder(String[] cmd) {
            this.cmd = cmd;
        }

        public ProcessBuilder environment(String[] env) {
            this.env = env;
            return this;
        }

        public ProcessBuilder directory(File directory) {
            this.directory = directory;
            return this;
        }

        public Process start() throws IOException {
            try {
                java.lang.ProcessBuilder pb = new java.lang.ProcessBuilder(cmd);
                if (directory != null) {
                    pb.directory(directory);
                }
                if (env != null) {
                    Map<String, String> pbEnv = pb.environment();
                    pbEnv.clear();
                    for (String e : env) {
                        int idx = e.indexOf('=');
                        if (idx != -1) {
                            pbEnv.put(e.substring(0, idx), e.substring(idx + 1));
                        }
                    }
                }
                return new ProcessProxy(pb.start());
            } catch (Exception ex) {
                throw new IOException(ex);
            }
        }
    }

    private static final class ProcessProxy extends Process {
        private final java.lang.Process target;

        public ProcessProxy(java.lang.Process target) {
            this.target = target;
        }

        @Override
        public OutputStream getOutputStream() { return target.getOutputStream(); }

        @Override
        public InputStream getInputStream() { return target.getInputStream(); }

        @Override
        public InputStream getErrorStream() { return target.getErrorStream(); }

        @Override
        public int waitFor() throws InterruptedException { return target.waitFor(); }

        @Override
        public int exitValue() { return target.exitValue(); }

        @Override
        public void destroy() { target.destroy(); }
    }
}
