package com.ivan.xinput.natives;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Helper class to facilitate loading native libraries that are included in the .jar file.
 * Libraries are expected to be within a folder in the classpath and their naming should
 * be as follows:
 * <pre>
 * &lt;library-path&gt;/&lt;os&gt;-&lt;arch&gt;/&lt;lib-prefix&gt;&lt;lib-name&gt;.&ltext&gt;
 * </pre>
 * where:
 * <ul>
 * <li><code>library-path</code> is specified by the user on the {@link #load(String, String)} method</li>
 * <li><code>os</code> is one of:
 *   <ul>
 *   <li><code>win</code> on Windows platforms
 *   <li><code>linux</code> on Linux platforms
 *   <li><code>unix</code> on UNIX-like platforms
 *   <li><code>mac</code> on Mac OS platforms
 *   <li><code>other</code> on other platforms
 *   </ul>
 * </li>
 * <li><code>arch</code> is either <code>x86</code> or <code>x64</code></li>
 * <li><code>lib-prefix</code> is either <code>lib</code> or nothing, depending on the platform
 * (typically Linux and UNIX-like platforms adopt <code>lib</code>, whereas Windows platforms do not use a prefix)</li>
 * <li><code>lib-name</code> is the library name, specified by the user on the <code>load(String, String)</code> method</li>
 * <li><code>ext</code> is the library extension, according to the platform standards
 * (<code>dll</code> on Windows, <code>so</code> on Linux and UNIX-like platforms, and <code>dylib</code> on Mac OS)</li>
 * </ul>
 * <p>
 * Files are extracted to the path specified by the system property <code>native.lib.path</code>. If the property is not
 * specified, libraries will be extracted to the <code>lib</code> folder under the working directory.
 */
// TODO allow a custom string scheme
public class NativeLibraryHelper {
    /**
     * System architectures: x86 or x64.
     */
    private static enum SystemArch {
        x86, x64;
    }

    /**
     * Operating systems: Windows, Linux, UNIX-like, Mac OS or Other.
     */
    private static enum OS {
        WINDOWS("win", "", ".dll"),
        LINUX("linux", "lib", ".so"),
        UNIX_LIKE("unix", "lib", ".so"),
        MAC_OS("mac", "lib", ".dylib"),
        OTHER("other", "", "");

        /**
         * The short name of the operating system, such as <code>win</code>,
         * <code>linux</code> or <code>mac</code>.
         */
        private final String name;

        /**
         * The library prefix used by this operating system (typically either <code>lib</code>
         * for Linux and UNIX-like systems or nothing for others).
         */
        private final String libPrefix;

        /**
         * The library file extension used by this operating system, such as <code>dll</code>
         * on Windows or <code>so</code> on Linux.
         */
        private final String libExt;

        private OS(final String name, final String libPrefix, final String libExt) {
            this.name = name;
            this.libPrefix = libPrefix;
            this.libExt = libExt;
        }

        /**
         * Retrieves the short name of the operating system, such as <code>win</code>,
         * <code>linux</code> or <code>mac</code>.
         *
         * @return the short name of the operating system
         */
        public String getName() {
            return name;
        }

        /**
         * Converts a library name into the corresponding file name according to this
         * operating system's specifications.
         * The naming convention is:
         * <pre>
         * &lt;lib-prefix&gt;&lt;lib-name&gt;.&ltext&gt;
         * </pre>
         * where:
         * <ul>
         * <li><code>lib-prefix</code> is either <code>lib</code> or nothing, depending on the platform
         * (typically Linux and UNIX-like platforms adopt <code>lib</code>, whereas Windows platforms do not use a prefix)</li>
         * <li><code>lib-name</code> is the library name, specified by the user on the <code>load(String, String)</code> method</li>
         * <li><code>ext</code> is the library extension, according to the platform standards
         * (<code>dll</code> on Windows, <code>so</code> on Linux and UNIX-like platforms, and <code>dylib</code> on Mac OS)</li>
         * </ul>
         *
         * @param libName the library name
         * @return the library file name for this operating system
         */
        public String toFilename(final String libName) {
            return libPrefix + libName + libExt;
        }
    }

    /**
     * The current system architecture.
     */
    private static final SystemArch ARCHITECTURE;

    /**
     * The current operating system.
     */
    private static final OS OPERATING_SYSTEM;

    /**
     * Temporary directory to store the extracted libraries.
     */
    private static final File LIB_DIR;

    static {
        ARCHITECTURE = System.getProperty("os.arch").contains("64") ? SystemArch.x64 : SystemArch.x86;

        final String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("windows")) {
            OPERATING_SYSTEM = OS.WINDOWS;
        } else if (osName.contains("nux")) {
            OPERATING_SYSTEM = OS.LINUX;
        } else if (osName.contains("nix") || osName.contains("aix")) {
            OPERATING_SYSTEM = OS.UNIX_LIKE;
        } else if (osName.contains("mac")) {
            OPERATING_SYSTEM = OS.MAC_OS;
        } else {
            OPERATING_SYSTEM = OS.OTHER;
        }

        LIB_DIR = new File(System.getProperty("native.lib.path", "lib"), ARCHITECTURE.toString());
        LIB_DIR.mkdirs();

        // add the tempDir to the java.library.path
        final String pathSep = File.separator;
        System.setProperty("java.library.path",
            System.getProperty("java.library.path") + pathSep + LIB_DIR.getAbsolutePath());

        // hack to force the class loader to reload the java.library.path paths
        Field sysPathsField;
        try {
            sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
            sysPathsField.setAccessible(true);
            sysPathsField.set(null, null);
        } catch (final Throwable e) {}
    }

    private NativeLibraryHelper() {}

    /**
     * Loads a native library using the system class loader.
     *
     * @param path the base path for the library
     * @param libName the library name
     * @throws IOException if there is a problem reading or writing the library
     */
    public static void load(final String path, final String libName) throws IOException {
        load(path, libName, ClassLoader.getSystemClassLoader());
    }

    /**
     * Loads a native library using the given class loader.
     *
     * @param path the base path for the library
     * @param libName the library name
     * @param cl the ClassLoader to use to load the library
     * @throws IOException if there is a problem reading or writing the library
     */
    public static void load(final String path, final String libName, final ClassLoader cl) throws IOException {
        final String filename = OPERATING_SYSTEM.toFilename(libName);
        final String resourcePath = path + "/" + OPERATING_SYSTEM.getName() + "-" + ARCHITECTURE + "/" + filename;
        final InputStream input = cl.getResourceAsStream(resourcePath);
        if (input == null) {
            throw new IOException("Library " + libName + " not found. Full path: " + resourcePath);
        }
        final File outputFile;
        try {
            outputFile = new File(LIB_DIR, filename);
            if (outputFile.exists()) {
                final String md5sumJar = md5sum(cl.getResourceAsStream(resourcePath));
                final String md5sumFile = md5sum(new FileInputStream(outputFile));
                if (md5sumJar.equals(md5sumFile)) {
                    System.load(outputFile.getAbsolutePath());
                    return;
                }
            } else {
                outputFile.createNewFile();
            }

            final FileOutputStream fos = new FileOutputStream(outputFile);
            try {
                final byte[] buf = new byte[65536];
                int len;
                while ((len = input.read(buf)) > -1) {
                    fos.write(buf, 0, len);
                }
            } finally {
                fos.close();
            }
        } finally {
            input.close();
        }
        System.load(outputFile.getAbsolutePath());
    }

    /**
     * Calculates the MD5 sum of the given input stream.
     *
     * @param input the input stream
     * @return the MD5 sum of the contents of the input stream
     * @throws IOException if there is an error reading the input stream
     */
    private static String md5sum(final InputStream input) throws IOException {
        final BufferedInputStream in = new BufferedInputStream(input);

        try {
            final MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            final DigestInputStream digestInputStream = new DigestInputStream(in, digest);
            while (digestInputStream.read() >= 0) {}
            final ByteArrayOutputStream md5out = new ByteArrayOutputStream();
            md5out.write(digest.digest());
            return md5out.toString();
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm is not available: " + e);
        } finally {
            in.close();
        }
    }
}
