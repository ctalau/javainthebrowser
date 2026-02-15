#!/usr/bin/env python3
from pathlib import Path
import re
import sys

workspace = Path(sys.argv[1])

replacements = [
    (
        workspace / "src/jdk.compiler/share/classes/com/sun/tools/javac/api/BasicJavacTask.java",
        """    private void initPlugin(Plugin p, String... args) {
        Module m = p.getClass().getModule();
        if (m.isNamed() && options.isSet(\"accessInternalAPI\")) {
            ModuleHelper.addExports(getClass().getModule(), m);
        }
        p.init(this, args);
    }
""",
        """    private void initPlugin(Plugin p, String... args) {
        if (options.isSet(\"accessInternalAPI\")) {
            ModuleHelper.addExports(getClass(), p.getClass());
        }
        p.init(this, args);
    }
""",
    ),
    (
        workspace / "src/java.compiler/share/classes/javax/tools/ToolProvider.java",
        """    @SuppressWarnings(\"removal\")
    private static <T> boolean matches(T tool, String moduleName) {
        PrivilegedAction<Boolean> pa = () -> {
            Module toolModule = tool.getClass().getModule();
            String toolModuleName = toolModule.getName();
            return Objects.equals(toolModuleName, moduleName);
        };
        return AccessController.doPrivileged(pa);
    }
}
""",
        """    @SuppressWarnings(\"removal\")
    private static <T> boolean matches(T tool, String moduleName) {
        PrivilegedAction<Boolean> pa = () -> {
            String toolModuleName = ModuleSupport.moduleName(tool.getClass());
            return Objects.equals(toolModuleName, moduleName);
        };
        return AccessController.doPrivileged(pa);
    }

    private static final class ModuleSupport {
        private static final java.lang.reflect.Method GET_MODULE;
        private static final java.lang.reflect.Method GET_NAME;

        static {
            java.lang.reflect.Method getModule = null;
            java.lang.reflect.Method getName = null;
            try {
                getModule = Class.class.getMethod(\"getModule\");
                Class<?> moduleClass = Class.forName(\"java.lang.Module\");
                getName = moduleClass.getMethod(\"getName\");
            } catch (ReflectiveOperationException ignored) {
                // Module system API not present in this runtime.
            }
            GET_MODULE = getModule;
            GET_NAME = getName;
        }

        static String moduleName(Class<?> clazz) {
            if (GET_MODULE == null || GET_NAME == null) {
                return null;
            }
            try {
                Object module = GET_MODULE.invoke(clazz);
                return module == null ? null : (String) GET_NAME.invoke(module);
            } catch (ReflectiveOperationException ex) {
                return null;
            }
        }
    }
}
""",
    ),
    (
        workspace / "src/jdk.compiler/share/classes/com/sun/tools/javac/util/ModuleHelper.java",
        """    public static void addExports(Module from, Module to) {
        for (String pack: javacInternalPackages) {
            from.addExports(pack, to);
        }
    }
}
""",
        """    public static void addExports(Class<?> fromClass, Class<?> toClass) {
        try {
            java.lang.reflect.Method getModule = Class.class.getMethod(\"getModule\");
            Class<?> moduleClass = Class.forName(\"java.lang.Module\");
            java.lang.reflect.Method isNamed = moduleClass.getMethod(\"isNamed\");
            java.lang.reflect.Method addExports = moduleClass.getMethod(\"addExports\", String.class, moduleClass);

            Object fromModule = getModule.invoke(fromClass);
            Object toModule = getModule.invoke(toClass);
            if (fromModule == null || toModule == null) {
                return;
            }
            Object named = isNamed.invoke(toModule);
            if (!(named instanceof Boolean) || !((Boolean) named)) {
                return;
            }

            for (String pack: javacInternalPackages) {
                addExports.invoke(fromModule, pack, toModule);
            }
        } catch (ReflectiveOperationException ignored) {
            // Module system API not present; nothing to export.
        }
    }

    public static void addExports(Object from, Object to) {
        if (from instanceof Class<?> && to instanceof Class<?>) {
            addExports((Class<?>) from, (Class<?>) to);
        }
    }
}
""",
    ),
]

serialization_replacements = [
    (
        workspace / "src/java.compiler/share/classes/javax/lang/model/type/MirroredTypeException.java",
        "import java.io.ObjectInputStream;\n",
        "",
    ),
    (
        workspace / "src/java.compiler/share/classes/javax/lang/model/type/MirroredTypeException.java",
        """    /**
     * Explicitly set all transient fields.
     * @param s the serial stream
     * @throws ClassNotFoundException for a missing class during
     * deserialization
     * @throws IOException for an IO problem during deserialization
     */
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        type = null;
        types = null;
    }
""",
        "",
    ),
    (
        workspace / "src/java.compiler/share/classes/javax/lang/model/type/MirroredTypesException.java",
        "import java.io.ObjectInputStream;\n",
        "",
    ),
    (
        workspace / "src/java.compiler/share/classes/javax/lang/model/type/MirroredTypesException.java",
        """    /**
     * Explicitly set all transient fields.
     * @param s the serial stream
     * @throws ClassNotFoundException for a missing class during
     * deserialization
     * @throws IOException for an IO problem during deserialization
     */
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        types = null;
    }
""",
        "",
    ),
]

for path, old, new in replacements:
    text = path.read_text(encoding='utf-8')
    if old in text:
        path.write_text(text.replace(old, new, 1), encoding='utf-8')
        continue
    if new in text:
        continue
    raise SystemExit(f"Expected patch hunk not found in {path}")


for path, old, new in serialization_replacements:
    text = path.read_text(encoding='utf-8')
    if old in text:
        path.write_text(text.replace(old, new, 1), encoding='utf-8')


def collect_resource_methods(kind: str) -> list[str]:
    javac_root = workspace / "src/jdk.compiler/share/classes/com/sun/tools/javac"
    method_names: set[str] = set()
    qualified = re.compile(rf"\bCompilerProperties\.{kind}\.([A-Za-z_][A-Za-z0-9_]*)\(")
    direct = re.compile(rf"\b{kind}\.([A-Za-z_][A-Za-z0-9_]*)\(")

    for src in javac_root.rglob("*.java"):
        text = src.read_text(encoding="utf-8", errors="ignore")
        method_names.update(qualified.findall(text))
        method_names.update(direct.findall(text))

    return sorted(method_names)


def collect_resource_fields(kind: str) -> list[str]:
    javac_root = workspace / "src/jdk.compiler/share/classes/com/sun/tools/javac"
    field_names: set[str] = set()
    qualified = re.compile(rf"\bCompilerProperties\.{kind}\.([A-Za-z_][A-Za-z0-9_]*)\b")
    direct = re.compile(rf"\b{kind}\.([A-Za-z_][A-Za-z0-9_]*)\b")

    for src in javac_root.rglob("*.java"):
        text = src.read_text(encoding="utf-8", errors="ignore")
        for candidate in qualified.findall(text) + direct.findall(text):
            field_names.add(candidate)

    return sorted(field_names)


def method_defs(methods: list[str], return_type: str) -> str:
    return "\n".join(
        f"        public static {return_type} {name}(Object... args) {{ return null; }}"
        for name in methods
    )


def field_defs(fields: list[str], return_type: str) -> str:
    return "\n".join(
        f"        public static final {return_type} {name} = null;"
        for name in fields
    )


def write_compiler_properties_stub() -> None:
    errors = collect_resource_methods("Errors")
    warnings = collect_resource_methods("Warnings")
    fragments = collect_resource_methods("Fragments")
    notes = collect_resource_methods("Notes")
    errors_fields = collect_resource_fields("Errors")
    warnings_fields = collect_resource_fields("Warnings")
    fragments_fields = collect_resource_fields("Fragments")
    notes_fields = collect_resource_fields("Notes")

    out = workspace / "src/jdk.compiler/share/classes/com/sun/tools/javac/resources/CompilerProperties.java"
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(
        f"""/*
 * Auto-generated compatibility shim for J2CL transpilation experiments.
 */
package com.sun.tools.javac.resources;

import com.sun.tools.javac.util.JCDiagnostic;

public final class CompilerProperties {{
    private CompilerProperties() {{}}

    public static final class Errors {{
        private Errors() {{}}
{field_defs(errors_fields, "JCDiagnostic.Error")}
{method_defs(errors, "JCDiagnostic.Error")}
    }}

    public static final class Warnings {{
        private Warnings() {{}}
{field_defs(warnings_fields, "JCDiagnostic.Warning")}
{method_defs(warnings, "JCDiagnostic.Warning")}
    }}

    public static final class Fragments {{
        private Fragments() {{}}
{field_defs(fragments_fields, "JCDiagnostic.Fragment")}
{method_defs(fragments, "JCDiagnostic.Fragment")}
    }}

    public static final class Notes {{
        private Notes() {{}}
{field_defs(notes_fields, "JCDiagnostic.Note")}
{method_defs(notes, "JCDiagnostic.Note")}
    }}
}}
""",
        encoding="utf-8",
    )


write_compiler_properties_stub()


def write_basic_jre_shims() -> None:
    shims = {
        "src/shims/java/io/PrintWriter.java": """package java.io;

import java.util.Locale;

public class PrintWriter extends Writer {
    public PrintWriter(Object out) {}
    public PrintWriter(Object out, boolean autoFlush) {}
    public PrintWriter(String fileName) {}
    public PrintWriter(String fileName, String csn) {}

    public void flush() {}
    public void close() {}
    public boolean checkError() { return false; }

    public void print(Object obj) {}
    public void print(String s) {}
    public void print(boolean b) {}
    public void print(char c) {}
    public void print(int i) {}
    public void print(long l) {}
    public void print(float f) {}
    public void print(double d) {}
    public void println() {}
    public void println(Object obj) {}
    public void println(String s) {}
    public void println(boolean b) {}
    public void println(char c) {}
    public void println(int i) {}
    public void println(long l) {}
    public void println(float f) {}
    public void println(double d) {}

    public void write(int c) {}
    public void write(char[] buf) {}
    public void write(char[] buf, int off, int len) {}
    public void write(String s) {}
    public void write(String s, int off, int len) {}

    public PrintWriter printf(String format, Object... args) { return this; }
    public PrintWriter printf(Locale l, String format, Object... args) { return this; }
    public PrintWriter format(String format, Object... args) { return this; }
    public PrintWriter format(Locale l, String format, Object... args) { return this; }
    public PrintWriter append(CharSequence csq) { return this; }
    public PrintWriter append(CharSequence csq, int start, int end) { return this; }
    public PrintWriter append(char c) { return this; }
}
""",
        "src/shims/java/net/URI.java": """package java.net;

public class URI {
    public URI(String str) {}

    public static URI create(String str) { return new URI(str); }
    public boolean isAbsolute() { return true; }
    public URI normalize() { return this; }
    public String getPath() { return ""; }
    public URL toURL() throws MalformedURLException { return new URL(""); }

    @Override
    public String toString() { return ""; }
}
""",
        "src/shims/java/net/URL.java": """package java.net;

import java.io.IOException;

public class URL {
    public URL(String spec) throws MalformedURLException {}
    public URL(URL context, String spec) throws MalformedURLException {}

    public String getPath() { return ""; }
    public URLConnection openConnection() throws IOException { return new URLConnection(this); }
    public java.io.InputStream openStream() throws IOException { return null; }
    public java.net.URI toURI() throws java.net.URISyntaxException { return null; }
    public String getProtocol() { return ""; }

    @Override
    public String toString() { return ""; }
}
""",
        "src/shims/java/net/MalformedURLException.java": """package java.net;

public class MalformedURLException extends java.io.IOException {
    public MalformedURLException() {}
    public MalformedURLException(String msg) { super(msg); }
}
""",
        "src/shims/java/net/URISyntaxException.java": """package java.net;

public class URISyntaxException extends Exception {
    public URISyntaxException(String input, String reason) { super(reason); }
}
""",
        "src/shims/java/net/URLConnection.java": """package java.net;

import java.io.IOException;
import java.io.InputStream;

public class URLConnection {
    protected URLConnection(URL url) {}

    public void connect() throws IOException {}
    public InputStream getInputStream() throws IOException { return null; }
}
""",
        "src/shims/java/util/ServiceLoader.java": """package java.util;

import java.util.Iterator;

public final class ServiceLoader<S> implements Iterable<S> {
    private ServiceLoader() {}

    public static <S> ServiceLoader<S> load(Class<S> service) {
        return new ServiceLoader<>();
    }

    public static <S> ServiceLoader<S> load(Class<S> service, ClassLoader loader) {
        return new ServiceLoader<>();
    }

    public void reload() {}

    @Override
    public Iterator<S> iterator() {
        return Collections.emptyIterator();
    }
}
""",
        "src/shims/java/lang/ClassLoader.java": """package java.lang;

import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

public class ClassLoader {
    public static ClassLoader getSystemClassLoader() { return null; }
    public Object getUnnamedModule() { return null; }
    public URL getResource(String name) { return null; }
    public Enumeration<URL> getResources(String name) { return null; }
    public Class<?> loadClass(String name) { return null; }
}
""",
        "src/shims/java/nio/CharBuffer.java": """package java.nio;

public class CharBuffer implements CharSequence {
    public static CharBuffer wrap(CharSequence csq) { return new CharBuffer(); }
    public static CharBuffer wrap(char[] array, int offset, int length) { return new CharBuffer(); }
    public static CharBuffer allocate(int capacity) { return new CharBuffer(); }
    public boolean hasArray() { return false; }
    public char[] array() { return new char[0]; }
    public int capacity() { return 0; }
    public int limit() { return 0; }
    public CharBuffer limit(int newLimit) { return this; }
    public CharBuffer flip() { return this; }
    public CharBuffer put(CharBuffer src) { return this; }
    public CharBuffer put(char c) { return this; }
    public CharBuffer position(int newPosition) { return this; }

    @Override
    public int length() { return 0; }

    @Override
    public char charAt(int index) { return 0; }

    @Override
    public CharSequence subSequence(int start, int end) { return ""; }

    @Override
    public String toString() { return ""; }
}
""",

        "src/shims/java/lang/Deprecated.java": """package java.lang;

public @interface Deprecated {
    String since() default "";
    boolean forRemoval() default false;
}
""",
        "src/shims/java/lang/reflect/Method.java": """package java.lang.reflect;

public class Method {
    public Object invoke(Object obj, Object... args) throws IllegalAccessException, IllegalArgumentException, java.lang.reflect.InvocationTargetException { return null; }
    public Class<?> getReturnType() { return null; }
}
""",
        "src/shims/java/lang/ReflectiveOperationException.java": """package java.lang;

public class ReflectiveOperationException extends Exception {
    public ReflectiveOperationException() {}
    public ReflectiveOperationException(String message) { super(message); }
}
""",
        "src/shims/java/lang/InternalError.java": """package java.lang;

public class InternalError extends Error {
    public InternalError() {}
    public InternalError(String message) { super(message); }
    public InternalError(Throwable cause) { super(cause); }
    public InternalError(String message, Throwable cause) { super(message, cause); }
}
""",
        "src/shims/java/lang/ref/SoftReference.java": """package java.lang.ref;

public class SoftReference<T> {
    public SoftReference(T referent) {}
    public T get() { return null; }
}
""",
        "src/shims/java/lang/ref/WeakReference.java": """package java.lang.ref;

public class WeakReference<T> {
    public WeakReference(T referent) {}
    public T get() { return null; }
}
""",
        "src/shims/java/io/File.java": """package java.io;

import java.net.URI;

public class File {
    public File(String pathname) {}
    public File(File parent, String child) {}
    public File(URI uri) {}

    public String getPath() { return ""; }
    public java.nio.file.Path toPath() { return null; }
}
""",
        "src/shims/java/io/CharArrayReader.java": """package java.io;

public class CharArrayReader extends Reader {
    public CharArrayReader(char[] buf) {}

    @Override
    public int read(char[] cbuf, int off, int len) { return -1; }

    @Override
    public void close() {}
}
""",
        "src/shims/java/io/OutputStreamWriter.java": """package java.io;

public class OutputStreamWriter extends Writer {
    public OutputStreamWriter(OutputStream out) {}

    @Override
    public void write(char[] cbuf, int off, int len) {}

    @Override
    public void flush() {}

    @Override
    public void close() {}
}
""",
        "src/shims/java/nio/file/Path.java": """package java.nio.file;

import java.net.URI;

public interface Path {
    Path resolve(String other);
    Path resolveSibling(String other);
    Path toAbsolutePath();
    Path getFileName();
    URI toUri();
    java.io.File toFile();
    FileSystem getFileSystem();
    Path relativize(Path other);
    boolean startsWith(Path other);
    boolean endsWith(String other);
    Path getParent();
}
""",
        "src/shims/java/nio/file/Paths.java": """package java.nio.file;

import java.net.URI;

public final class Paths {
    private Paths() {}
    public static Path get(String first, String... more) { return null; }
    public static Path get(URI uri) { return null; }
}
""",
        "src/shims/java/nio/file/Files.java": """package java.nio.file;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

public final class Files {
    private Files() {}

    public static byte[] readAllBytes(Path path) throws IOException { return new byte[0]; }
    public static Path createDirectories(Path dir) throws IOException { return dir; }
    public static boolean exists(Path path) { return false; }
    public static boolean isDirectory(Path path) { return false; }
    public static boolean isSymbolicLink(Path path) { return false; }
    public static boolean isRegularFile(Path path) { return false; }
    public static boolean isDirectory(Path path) { return false; }
    public static boolean isSymbolicLink(Path path) { return false; }
    public static Path write(Path path, byte[] bytes) throws IOException { return path; }
    public static java.io.InputStream newInputStream(Path path) throws IOException { return null; }
    public static Path readSymbolicLink(Path link) throws IOException { return null; }
    public static java.util.stream.Stream<Path> list(Path dir) throws IOException { return java.util.stream.Stream.empty(); }
    public static Path walkFileTree(Path start, java.util.Set<FileVisitOption> options, int maxDepth, FileVisitor<? super Path> visitor) throws IOException { return start; }
    public static DirectoryStream<Path> newDirectoryStream(Path dir) throws IOException { return null; }
    public static java.util.stream.Stream<Path> list(Path dir) throws IOException { return java.util.stream.Stream.empty(); }
    public static List<String> readAllLines(Path path, Charset cs) throws IOException { return Collections.emptyList(); }
}
""",
        "src/shims/java/nio/file/FileSystem.java": """package java.nio.file;

public abstract class FileSystem implements AutoCloseable {
    public abstract Path getPath(String first, String... more);
    public Iterable<Path> getRootDirectories() { return java.util.Collections.emptyList(); }
    public String getSeparator() { return "/"; }
    public void close() {}
}
""",
        "src/shims/java/nio/file/FileSystems.java": """package java.nio.file;

import java.net.URI;
import java.util.Map;

public final class FileSystems {
    private FileSystems() {}
    public static FileSystem getDefault() { return null; }
    public static FileSystem getFileSystem(URI uri) { return null; }
    public static FileSystem newFileSystem(URI uri, Map<String, ?> env) { return null; }
    public static FileSystem newFileSystem(Path path, ClassLoader loader) { return null; }
}
""",
        "src/shims/java/nio/file/DirectoryStream.java": """package java.nio.file;

public interface DirectoryStream<T> extends Iterable<T>, AutoCloseable {
    interface Filter<T> { boolean accept(T entry) throws java.io.IOException; }
    @Override
    void close();
}
""",
        "src/shims/java/nio/file/InvalidPathException.java": """package java.nio.file;

public class InvalidPathException extends IllegalArgumentException {
    public InvalidPathException(String input, String reason) { super(reason); }
}
""",
        "src/shims/java/nio/file/ProviderNotFoundException.java": """package java.nio.file;

public class ProviderNotFoundException extends RuntimeException {
    public ProviderNotFoundException() {}
    public ProviderNotFoundException(String msg) { super(msg); }
}
""",
        "src/shims/java/nio/file/FileVisitResult.java": """package java.nio.file;

public enum FileVisitResult {
    CONTINUE,
    TERMINATE,
    SKIP_SUBTREE,
    SKIP_SIBLINGS
}
""",
        "src/shims/java/nio/file/FileVisitOption.java": """package java.nio.file;

public enum FileVisitOption {
    FOLLOW_LINKS
}
""",
        "src/shims/java/nio/file/attribute/BasicFileAttributes.java": """package java.nio.file.attribute;

public interface BasicFileAttributes {
    boolean isDirectory();
    boolean isRegularFile();
    boolean isSymbolicLink();
}
""",
        "src/shims/java/nio/file/spi/FileSystemProvider.java": """package java.nio.file.spi;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Map;

public abstract class FileSystemProvider {
    public static java.util.List<FileSystemProvider> installedProviders() { return java.util.Collections.emptyList(); }
    public abstract String getScheme();
    public abstract FileSystem getFileSystem(java.net.URI uri);
    public abstract Path getPath(java.net.URI uri);
    public FileSystem newFileSystem(java.net.URI uri, Map<String, ?> env) { return null; }
}
""",
        "src/shims/java/util/regex/Pattern.java": """package java.util.regex;

public class Pattern {
    public static final int CASE_INSENSITIVE = 2;
    public static String quote(String s) { return s; }
    public static Pattern compile(String regex) { return new Pattern(); }
    public static Pattern compile(String regex, int flags) { return new Pattern(); }
    public Matcher matcher(CharSequence input) { return new Matcher(); }
}
""",
        "src/shims/java/util/regex/Matcher.java": """package java.util.regex;

public class Matcher {
    public boolean find() { return false; }
    public boolean find(int start) { return false; }
    public boolean matches() { return false; }
    public int start() { return 0; }
    public static String quoteReplacement(String replacement) { return replacement; }
    public String group(int i) { return ""; }
    public String replaceAll(String replacement) { return ""; }
}
""",
        "src/shims/java/util/ResourceBundle.java": """package java.util;

public abstract class ResourceBundle {
    public Object getObject(String key) { return null; }
    public String getString(String key) { return ""; }

    public static ResourceBundle getBundle(String baseName) { return null; }
    public static ResourceBundle getBundle(String baseName, Locale locale) { return null; }
}
""",
        "src/shims/java/text/MessageFormat.java": """package java.text;

public class MessageFormat {
    public static String format(String pattern, Object... arguments) { return ""; }
}
""",

        "src/shims/java/security/PrivilegedAction.java": """package java.security;

public interface PrivilegedAction<T> {
    T run();
}
""",
        "src/shims/java/security/AccessController.java": """package java.security;

public final class AccessController {
    private AccessController() {}

    public static <T> T doPrivileged(PrivilegedAction<T> action) {
        return action.run();
    }
}
""",
        "src/shims/java/util/ServiceConfigurationError.java": """package java.util;

public class ServiceConfigurationError extends Error {
    public ServiceConfigurationError(String msg) { super(msg); }
    public ServiceConfigurationError(String msg, Throwable cause) { super(msg, cause); }
}
""",
        "src/shims/java/text/BreakIterator.java": """package java.text;

public abstract class BreakIterator {
    public static BreakIterator getSentenceInstance() { return null; }
    public void setText(String newText) {}
    public abstract int first();
    public abstract int next();
}
""",
        "src/shims/java/util/Hashtable.java": """package java.util;

public class Hashtable<K, V> extends HashMap<K, V> {
    public Hashtable() {}
}
""",
        "src/shims/java/util/WeakHashMap.java": """package java.util;

public class WeakHashMap<K, V> extends HashMap<K, V> {
    public WeakHashMap() {}
}
""",
        "src/shims/java/util/Properties.java": """package java.util;

public class Properties extends Hashtable<Object, Object> {
    public Properties() {}
    public String getProperty(String key) { return null; }
    public Object setProperty(String key, String value) { return put(key, value); }
    @Override
    public Object put(Object key, Object value) { return value; }
}
""",
        "src/shims/java/io/StringWriter.java": """package java.io;

public class StringWriter extends Writer {
    public StringWriter() {}

    @Override
    public void write(char[] cbuf, int off, int len) {}

    @Override
    public void flush() {}

    @Override
    public void close() {}

    @Override
    public String toString() { return ""; }
}
""",
        "src/shims/java/io/IOError.java": """package java.io;

public class IOError extends Error {
    public IOError(Throwable cause) { super(cause); }
}
""",
        "src/shims/java/io/FileNotFoundException.java": """package java.io;

public class FileNotFoundException extends IOException {
    public FileNotFoundException() {}
    public FileNotFoundException(String s) { super(s); }
}
""",
        "src/shims/java/io/FileWriter.java": """package java.io;

public class FileWriter extends Writer {
    public FileWriter(String fileName) {}

    @Override
    public void write(char[] cbuf, int off, int len) {}

    @Override
    public void flush() {}

    @Override
    public void close() {}
}
""",
        "src/shims/java/io/DataInputStream.java": """package java.io;

public class DataInputStream extends FilterInputStream {
    public DataInputStream(InputStream in) { super(in); }
    public int readInt() { return 0; }
    public char readChar() { return 0; }
    public long readLong() { return 0L; }
    public float readFloat() { return 0f; }
    public double readDouble() { return 0d; }
}
""",
        "src/shims/java/io/DataOutputStream.java": """package java.io;

public class DataOutputStream extends FilterOutputStream {
    public DataOutputStream(OutputStream out) { super(out); }
    public void writeInt(int v) {}
    public void writeChar(int v) {}
    public void writeLong(long v) {}
    public void writeFloat(float v) {}
    public void writeDouble(double v) {}
}
""",
    }

    shims.update({
        "src/shims/java/lang/Runtime.java": """package java.lang;

public class Runtime {
    public static Runtime getRuntime() { return new Runtime(); }
    public static Version version() { return new Version(); }
    public static final class Version {
        public int feature() { return 0; }
    }
}
""",
        "src/shims/java/lang/System.java": """package java.lang;

import java.io.PrintStream;

public final class System {
    public static final java.io.InputStream in = null;
    public static final PrintStream out = null;
    public static final PrintStream err = null;
    private System() {}
    public static void exit(int status) {}
    public static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length) {}
    public static int identityHashCode(Object x) { return 0; }
    public static long currentTimeMillis() { return 0L; }
    public static String getProperty(String key) { return null; }
}
""",
        "src/shims/java/lang/Character.java": """package java.lang;

public final class Character {
    public static final int MIN_RADIX = 2;
    public static final int MAX_RADIX = 36;
    public static final char MIN_VALUE = 0;
    public static final char MAX_VALUE = 65535;
    private Character() {}
    public static boolean isDefined(int codePoint) { return false; }
    public static boolean isISOControl(int codePoint) { return false; }
    public static boolean isJavaIdentifierStart(int codePoint) { return false; }
    public static boolean isJavaIdentifierPart(int codePoint) { return false; }
    public static boolean isUnicodeIdentifierPart(char ch) { return false; }
    public static boolean isUnicodeIdentifierStart(char ch) { return false; }
    public static boolean isSpaceChar(int codePoint) { return false; }
    public static int charCount(int codePoint) { return 1; }
    public static boolean isWhitespace(char ch) { return false; }
    public static int digit(char ch, int radix) { return 0; }
    public static int digit(int codePoint, int radix) { return 0; }
    public static char forDigit(int digit, int radix) { return '0'; }
    public static boolean isDigit(char ch) { return false; }
    public static boolean isIdentifierIgnorable(char ch) { return false; }
    public static char highSurrogate(int codePoint) { return 0; }
    public static char lowSurrogate(int codePoint) { return 0; }
    public static int toLowerCase(char ch) { return ch; }
    public static boolean isHighSurrogate(char ch) { return false; }
    public static boolean isLowSurrogate(char ch) { return false; }
    public static int toCodePoint(char high, char low) { return 0; }
}
""",
        "src/shims/java/lang/Class.java": """package java.lang;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public final class Class<T> {
    public static Class<?> forName(String name) throws ClassNotFoundException { return null; }
    public static Class<?> forName(String name, boolean initialize, ClassLoader loader) throws ClassNotFoundException { return null; }
    public Method getMethod(String name, Class<?>... parameterTypes) throws NoSuchMethodException { return null; }
    public java.lang.reflect.Constructor<T> getConstructor(Class<?>... parameterTypes) throws NoSuchMethodException { return null; }
    public ClassLoader getClassLoader() { return null; }
    public <U> Class<? extends U> asSubclass(Class<U> clazz) { return null; }
    public T cast(Object obj) { return null; }
    public boolean isAnnotation() { return false; }
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) { return false; }
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) { return null; }
    public String getName() { return ""; }
    public boolean isEnum() { return false; }
    public boolean isInstance(Object obj) { return false; }
    public String getSimpleName() { return ""; }
    public String getCanonicalName() { return ""; }
    public Object getModule() { return null; }
    public Class<?> getComponentType() { return null; }
    public java.security.ProtectionDomain getProtectionDomain() { return null; }
    public java.net.URL getResource(String name) { return null; }
    public java.io.InputStream getResourceAsStream(String name) { return null; }
}
""",
        "src/shims/java/lang/reflect/Array.java": """package java.lang.reflect;

public final class Array {
    private Array() {}
    public static Object newInstance(Class<?> componentType, int length) { return null; }
    public static void set(Object array, int index, Object value) {}
}
""",
        "src/shims/java/lang/Throwable.java": """package java.lang;

public class Throwable {
    public Throwable() {}
    public Throwable(String message) {}
    public Throwable(String message, Throwable cause) {}
    public String getMessage() { return null; }
    public String getLocalizedMessage() { return getMessage(); }
    public Throwable getCause() { return null; }
    public Throwable initCause(Throwable cause) { return this; }
    public StackTraceElement[] getStackTrace() { return new StackTraceElement[0]; }
    public void setStackTrace(StackTraceElement[] trace) {}
    public void printStackTrace(java.io.PrintStream s) {}
    public void printStackTrace(java.io.PrintWriter s) {}
}
""",
        "src/shims/java/lang/ClassNotFoundException.java": """package java.lang;

public class ClassNotFoundException extends ReflectiveOperationException {
    public ClassNotFoundException() {}
    public ClassNotFoundException(String msg) { super(msg); }
}
""",
        "src/shims/java/lang/LinkageError.java": """package java.lang;

public class LinkageError extends Error {
    public LinkageError() {}
    public LinkageError(String msg) { super(msg); }
}
""",
        "src/shims/java/lang/ClassFormatError.java": """package java.lang;

public class ClassFormatError extends LinkageError {
    public ClassFormatError() {}
    public ClassFormatError(String msg) { super(msg); }
}
""",
        "src/shims/java/lang/UnsupportedClassVersionError.java": """package java.lang;

public class UnsupportedClassVersionError extends ClassFormatError {
    public UnsupportedClassVersionError() {}
    public UnsupportedClassVersionError(String msg) { super(msg); }
}
""",
        "src/shims/java/lang/IllegalAccessException.java": """package java.lang;

public class IllegalAccessException extends ReflectiveOperationException {
    public IllegalAccessException() {}
    public IllegalAccessException(String msg) { super(msg); }
}
""",
        "src/shims/java/lang/NoSuchMethodException.java": """package java.lang;

public class NoSuchMethodException extends ReflectiveOperationException {
    public NoSuchMethodException() {}
    public NoSuchMethodException(String msg) { super(msg); }
}
""",
        "src/shims/java/io/InputStreamReader.java": """package java.io;

public class InputStreamReader extends Reader {
    public InputStreamReader(InputStream in) {}
    public InputStreamReader(InputStream in, String charsetName) {}
    @Override public int read(char[] cbuf, int off, int len) { return -1; }
    @Override public void close() throws IOException {}
}
""",
        "src/shims/java/io/BufferedReader.java": """package java.io;

public class BufferedReader extends Reader {
    public BufferedReader(Reader in) {}
    public String readLine() { return null; }
    @Override public int read(char[] cbuf, int off, int len) { return -1; }
    @Override public void close() throws IOException {}
}
""",
        "src/shims/java/net/URLClassLoader.java": """package java.net;

public class URLClassLoader extends ClassLoader {
    public URLClassLoader(URL[] urls) {}
    public URLClassLoader(URL[] urls, ClassLoader parent) {}
}
""",
        "src/shims/java/net/URLStreamHandler.java": """package java.net;

public abstract class URLStreamHandler {}
""",
        "src/shims/java/security/CodeSource.java": """package java.security;

public class CodeSource {
    public CodeSource(java.net.URL location, java.security.cert.Certificate[] certs) {}
    public java.net.URL getLocation() { return null; }
}
""",
        "src/shims/java/security/ProtectionDomain.java": """package java.security;

public class ProtectionDomain {
    public CodeSource getCodeSource() { return null; }
}
""",
        "src/shims/java/lang/Thread.java": """package java.lang;

public class Thread {
    public Thread() {}
    public Thread(String name) {}
    public void start() {}
    public void interrupt() {}
    public void setDaemon(boolean on) {}
    public static void sleep(long millis) throws InterruptedException {}
    public void run() {}
}
""",
        "src/shims/java/lang/reflect/Constructor.java": """package java.lang.reflect;

public class Constructor<T> {
    public T newInstance(Object... initargs) { return null; }
}
""",
        "src/shims/java/nio/ByteBuffer.java": """package java.nio;

public class ByteBuffer {
    public static ByteBuffer allocate(int capacity) { return new ByteBuffer(); }
    public int position() { return 0; }
    public ByteBuffer position(int newPosition) { return this; }
    public int limit() { return 0; }
    public ByteBuffer limit(int newLimit) { return this; }
    public ByteBuffer flip() { return this; }
    public byte get() { return 0; }
    public ByteBuffer put(byte b) { return this; }
    public ByteBuffer put(ByteBuffer src) { return this; }
    public int remaining() { return 0; }
    public int capacity() { return 0; }
    public ByteBuffer clear() { return this; }
    public byte[] array() { return new byte[0]; }
}
""",
        "src/shims/java/nio/charset/Charset.java": """package java.nio.charset;

public class Charset {
    public static Charset defaultCharset() { return new Charset(); }
    public static Charset forName(String charsetName) { return new Charset(); }
    public CharsetDecoder newDecoder() { return new CharsetDecoder(); }
    public String name() { return ""; }
}
""",
        "src/shims/java/nio/charset/CharsetDecoder.java": """package java.nio.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public class CharsetDecoder {
    public CharBuffer decode(ByteBuffer in) { return CharBuffer.allocate(0); }
    public CoderResult decode(ByteBuffer in, CharBuffer out, boolean endOfInput) { return CoderResult.UNDERFLOW; }
    public CoderResult flush(CharBuffer out) { return CoderResult.UNDERFLOW; }
    public CharsetDecoder onMalformedInput(CodingErrorAction newAction) { return this; }
    public CharsetDecoder onUnmappableCharacter(CodingErrorAction newAction) { return this; }
    public float averageCharsPerByte() { return 1.0f; }
    public float maxCharsPerByte() { return 1.0f; }
}
""",
        "src/shims/java/nio/charset/CoderResult.java": """package java.nio.charset;

public class CoderResult {
    public static final CoderResult UNDERFLOW = new CoderResult();
    public static final CoderResult OVERFLOW = new CoderResult();
    public boolean isUnderflow() { return false; }
    public boolean isOverflow() { return false; }
    public boolean isMalformed() { return false; }
    public boolean isUnmappable() { return false; }
    public int length() { return 0; }
}
""",
        "src/shims/java/nio/charset/CodingErrorAction.java": """package java.nio.charset;

public class CodingErrorAction {
    public static final CodingErrorAction IGNORE = new CodingErrorAction();
    public static final CodingErrorAction REPLACE = new CodingErrorAction();
    public static final CodingErrorAction REPORT = new CodingErrorAction();
}
""",
        "src/shims/java/io/FilterWriter.java": """package java.io;

public class FilterWriter extends Writer {
    protected Writer out;
    protected FilterWriter(Writer out) { this.out = out; }
    @Override public void write(char[] cbuf, int off, int len) {}
    @Override public void flush() {}
    @Override public void close() throws IOException {}
}
""",
        "src/shims/java/util/Objects.java": """package java.util;

public final class Objects {
    private Objects() {}
    public static int checkFromIndexSize(int fromIndex, int size, int length) { return fromIndex; }
    public static <T> T requireNonNull(T obj) { return obj; }
    public static <T> T requireNonNull(T obj, String message) { return obj; }
    public static boolean equals(Object a, Object b) { return a == b || (a != null && a.equals(b)); }
    public static int hashCode(Object o) { return o == null ? 0 : o.hashCode(); }
}
""",
        "src/shims/java/util/StringTokenizer.java": """package java.util;

public class StringTokenizer {
    public StringTokenizer(String str, String delim) {}
    public StringTokenizer(String str) {}
    public boolean hasMoreTokens() { return false; }
    public String nextToken() { return ""; }
}
""",
        "src/shims/java/lang/reflect/InvocationTargetException.java": """package java.lang.reflect;

public class InvocationTargetException extends ReflectiveOperationException {
    public InvocationTargetException(Throwable cause) { super(); }
}
""",
        "src/shims/java/util/EnumSet.java": """package java.util;

public class EnumSet<E extends Enum<E>> extends HashSet<E> {
    public static <E extends Enum<E>> EnumSet<E> allOf(Class<E> elementType) { return new EnumSet<>(); }
    public static <E extends Enum<E>> EnumSet<E> range(E from, E to) { return new EnumSet<>(); }
    public static <E extends Enum<E>> EnumSet<E> of(E e1) { return new EnumSet<>(); }
    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2) { return new EnumSet<>(); }
    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3) { return new EnumSet<>(); }
    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3, E e4, E e5) { return new EnumSet<>(); }
    @SafeVarargs
    public static <E extends Enum<E>> EnumSet<E> of(E first, E... rest) { return new EnumSet<>(); }
    public static <E extends Enum<E>> EnumSet<E> copyOf(java.util.Set<E> s) { return new EnumSet<>(); }
    public static <E extends Enum<E>> EnumSet<E> copyOf(java.util.Collection<E> c) { return new EnumSet<>(); }
    public static <E extends Enum<E>> EnumSet<E> noneOf(Class<E> elementType) { return new EnumSet<>(); }
    public static <E extends Enum<E>> EnumSet<E> complementOf(EnumSet<E> s) { return new EnumSet<>(); }
}
""",
    })


    shims.update({


        "src/shims/java/lang/Record.java": """package java.lang;

public abstract class Record {}
""",
        "src/shims/java/lang/String.java": """package java.lang;

import java.util.Locale;

public final class String implements CharSequence {
    public String() {}
    public String(char[] value) {}
    public String(char[] value, int offset, int count) {}
    public String(byte[] bytes) {}
    public String(String original) {}

    public int length() { return 0; }
    public char charAt(int index) { return 0; }
    public CharSequence subSequence(int start, int end) { return this; }
    public int codePointAt(int index) { return 0; }
    public String[] split(String regex) { return new String[0]; }
    public String[] split(String regex, int limit) { return new String[0]; }
    public boolean startsWith(String prefix) { return false; }
    public boolean endsWith(String suffix) { return false; }
    public boolean matches(String regex) { return false; }
    public String substring(int beginIndex) { return this; }
    public String substring(int beginIndex, int endIndex) { return this; }
    public int indexOf(char ch) { return -1; }
    public int indexOf(char ch, int fromIndex) { return -1; }
    public int indexOf(String str, int fromIndex) { return -1; }
    public int indexOf(String str) { return -1; }
    public int lastIndexOf(char ch) { return -1; }
    public int lastIndexOf(String str) { return -1; }
    public String replace(String target, String replacement) { return this; }
    public String replace(char oldChar, char newChar) { return this; }
    public String replaceAll(String regex, String replacement) { return this; }
    public String toLowerCase(Locale locale) { return this; }
    public String toUpperCase(Locale locale) { return this; }
    public String stripTrailing() { return this; }
    public String stripLeading() { return this; }
    public String trim() { return this; }
    public boolean contains(CharSequence s) { return false; }
    public boolean isBlank() { return false; }
    public boolean contentEquals(CharSequence cs) { return false; }
    public int compareTo(String another) { return 0; }
    public String replaceFirst(String regex, String replacement) { return this; }
    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {}
    public char[] toCharArray() { return new char[0]; }
    public boolean isEmpty() { return false; }
    public String stripIndent() { return this; }
    public String translateEscapes() { return this; }

    public static String valueOf(Object obj) { return ""; }
    public static String valueOf(char c) { return ""; }
    public static String valueOf(int i) { return ""; }
    public static String valueOf(long l) { return ""; }
    public static String valueOf(char[] data) { return ""; }
    public static String toString(char c) { return ""; }
    public static String format(String format, Object... args) { return ""; }
    public static String format(Locale l, String format, Object... args) { return ""; }

    @Override
    public String toString() { return this; }
}
""",
        "src/shims/java/lang/Long.java": """package java.lang;

public final class Long {
    public static final long MIN_VALUE = -9223372036854775808L;
    public static final long MAX_VALUE = 9223372036854775807L;
    public static Long valueOf(long v) { return new Long(); }
    public static long parseLong(String s, int radix) { return 0L; }
}
""",
        "src/shims/java/io/OutputStreamWriter.java": """package java.io;

import java.nio.charset.Charset;

public class OutputStreamWriter extends Writer {
    public OutputStreamWriter(OutputStream out) {}
    public OutputStreamWriter(OutputStream out, Charset cs) {}
    @Override public void write(char[] cbuf, int off, int len) {}
    @Override public void flush() {}
    @Override public void close() throws IOException {}
}
""",
        "src/shims/java/lang/annotation/ElementType.java": """package java.lang.annotation;

public enum ElementType {
    TYPE,
    FIELD,
    METHOD,
    PARAMETER,
    CONSTRUCTOR,
    LOCAL_VARIABLE,
    ANNOTATION_TYPE,
    PACKAGE,
    TYPE_PARAMETER,
    TYPE_USE,
    MODULE,
    RECORD_COMPONENT
}
""",
        "src/shims/java/nio/file/Files.java": """package java.nio.file;

import java.io.IOException;
import java.io.BufferedWriter;
import java.nio.charset.Charset;

public final class Files {
    private Files() {}
    public static BufferedWriter newBufferedWriter(Path path) throws IOException { return null; }
    public static BufferedWriter newBufferedWriter(Path path, Charset cs) throws IOException { return null; }
    public static boolean exists(Path path) { return false; }
    public static boolean isRegularFile(Path path) { return false; }
    public static boolean isDirectory(Path path) { return false; }
    public static boolean isSymbolicLink(Path path) { return false; }
    public static java.nio.file.DirectoryStream<Path> newDirectoryStream(Path path) throws IOException { return null; }
    public static java.util.stream.Stream<String> lines(Path path, Charset cs) throws IOException { return java.util.stream.Stream.empty(); }
    public static <A extends java.nio.file.attribute.BasicFileAttributes> A readAttributes(Path path, Class<A> type) throws IOException { return null; }
    public static java.io.InputStream newInputStream(Path path) throws IOException { return null; }
    public static Path readSymbolicLink(Path link) throws IOException { return null; }
    public static java.util.stream.Stream<Path> list(Path dir) throws IOException { return java.util.stream.Stream.empty(); }
    public static Path walkFileTree(Path start, java.util.Set<FileVisitOption> options, int maxDepth, FileVisitor<? super Path> visitor) throws IOException { return start; }
}
""",
        "src/shims/java/util/jar/Attributes.java": """package java.util.jar;

public class Attributes extends java.util.HashMap<Object, Object> {
    public static class Name {
        public static final Name CLASS_PATH = new Name();
    }
    public String getValue(Name name) { return null; }
}
""",
        "src/shims/java/util/jar/Manifest.java": """package java.util.jar;

public class Manifest {
    public Attributes getMainAttributes() { return new Attributes(); }
}
""",
        "src/shims/java/util/jar/JarFile.java": """package java.util.jar;

import java.io.IOException;

public class JarFile implements AutoCloseable {
    public JarFile(String name) throws IOException {}
    public Manifest getManifest() throws IOException { return new Manifest(); }
    @Override public void close() throws IOException {}
}
""",
        "src/shims/java/nio/file/FileSystemNotFoundException.java": """package java.nio.file;

public class FileSystemNotFoundException extends RuntimeException {
    public FileSystemNotFoundException() {}
    public FileSystemNotFoundException(String msg) { super(msg); }
}
""",
        "src/shims/java/nio/file/LinkOption.java": """package java.nio.file;

public enum LinkOption {
    NOFOLLOW_LINKS
}
""",
        "src/shims/java/nio/file/FileVisitor.java": """package java.nio.file;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;

public interface FileVisitor<T> {
    FileVisitResult preVisitDirectory(T dir, BasicFileAttributes attrs) throws IOException;
    FileVisitResult visitFile(T file, BasicFileAttributes attrs) throws IOException;
    FileVisitResult visitFileFailed(T file, IOException exc) throws IOException;
    FileVisitResult postVisitDirectory(T dir, IOException exc) throws IOException;
}
""",
        "src/shims/java/nio/file/SimpleFileVisitor.java": """package java.nio.file;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;

public class SimpleFileVisitor<T> implements FileVisitor<T> {
    public FileVisitResult preVisitDirectory(T dir, BasicFileAttributes attrs) throws IOException { return FileVisitResult.CONTINUE; }
    public FileVisitResult visitFile(T file, BasicFileAttributes attrs) throws IOException { return FileVisitResult.CONTINUE; }
    public FileVisitResult visitFileFailed(T file, IOException exc) throws IOException { return FileVisitResult.CONTINUE; }
    public FileVisitResult postVisitDirectory(T dir, IOException exc) throws IOException { return FileVisitResult.CONTINUE; }
}
""",
        "src/shims/java/lang/module/ModuleFinder.java": """package java.lang.module;

public final class ModuleFinder {
    private ModuleFinder() {}
    public static ModuleFinder of() { return new ModuleFinder(); }
    public static ModuleFinder of(java.nio.file.Path... entries) { return new ModuleFinder(); }
}
""",
        "src/shims/java/lang/module/ModuleDescriptor.java": """package java.lang.module;

public final class ModuleDescriptor {
    public String toNameAndVersion() { return ""; }
}
""",
        "src/shims/java/lang/ModuleLayer.java": """package java.lang;

public class ModuleLayer {
    public static ModuleLayer boot() { return new ModuleLayer(); }
}
""",
        "src/shims/java/text/Collator.java": """package java.text;

public class Collator {
    public static Collator getInstance() { return new Collator(); }
    public int compare(String source, String target) { return 0; }
}
""",
        "src/shims/java/text/SimpleDateFormat.java": """package java.text;

public class SimpleDateFormat {
    public SimpleDateFormat(String pattern) {}
    public String format(java.util.Date date) { return ""; }
}
""",
        "src/shims/java/util/Calendar.java": """package java.util;

public class Calendar {
    public static Calendar getInstance() { return new Calendar(); }
    public Date getTime() { return new Date(); }
}
""",
        "src/shims/java/lang/OutOfMemoryError.java": """package java.lang;

public class OutOfMemoryError extends VirtualMachineError {
    public OutOfMemoryError() {}
}
""",
        "src/shims/java/lang/StackOverflowError.java": """package java.lang;

public class StackOverflowError extends VirtualMachineError {
    public StackOverflowError() {}
}
""",
        "src/shims/java/lang/IllegalAccessError.java": """package java.lang;

public class IllegalAccessError extends IncompatibleClassChangeError {
    public IllegalAccessError() {}
}
""",
        "src/shims/java/lang/VirtualMachineError.java": """package java.lang;

public class VirtualMachineError extends Error {
    public VirtualMachineError() {}
    public VirtualMachineError(String msg) { super(msg); }
}
""",
        "src/shims/java/lang/IncompatibleClassChangeError.java": """package java.lang;

public class IncompatibleClassChangeError extends LinkageError {
    public IncompatibleClassChangeError() {}
    public IncompatibleClassChangeError(String msg) { super(msg); }
}
""",
        "src/shims/com/sun/tools/doclint/DocLint.java": """package com.sun.tools.doclint;

import java.util.List;
import java.util.Set;

public class DocLint {
    public static final String XMSGS_OPTION = "-Xmsgs";
    public static final String XCHECK_PACKAGE = "-XcheckPackage:";
    public static final String XMSGS_CUSTOM_PREFIX = "-Xmsgs:";
    public static final String XCUSTOM_TAGS_PREFIX = "-XcustomTags:";

    public static boolean isValidOption(String opt) { return false; }
    public static DocLint newDocLint() { return new DocLint(); }
    public void init(java.util.function.Consumer<String> out, List<String> opts, boolean javacMode) {}
    public void setCheckMissing(boolean b) {}
    public void setDeclScanner(Object scanner) {}
    public void reportStats(java.io.PrintWriter out) {}
}
""",
        "src/shims/java/security/cert/Certificate.java": """package java.security.cert;

public abstract class Certificate {}
""",
    })

    for rel_path, content in shims.items():
        out = workspace / rel_path
        out.parent.mkdir(parents=True, exist_ok=True)
        out.write_text(content, encoding="utf-8")


write_basic_jre_shims()
