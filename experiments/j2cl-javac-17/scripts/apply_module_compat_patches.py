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
            if f"{candidate}(" in text:
                continue
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

public class PrintWriter {
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
    public Object invoke(Object obj, Object... args) { return null; }
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
    public static boolean isRegularFile(Path path) { return false; }
    public static Path write(Path path, byte[] bytes) throws IOException { return path; }
    public static java.io.InputStream newInputStream(Path path) throws IOException { return null; }
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
}
""",
        "src/shims/java/nio/file/spi/FileSystemProvider.java": """package java.nio.file.spi;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Map;

public abstract class FileSystemProvider {
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
    public Object put(String key, String value) { return value; }
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
    public static Runtime version() { return new Runtime(); }
    public int feature() { return 0; }
}
""",
        "src/shims/java/lang/System.java": """package java.lang;

import java.io.PrintStream;

public final class System {
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
    private Character() {}
    public static boolean isDefined(int codePoint) { return false; }
    public static boolean isISOControl(int codePoint) { return false; }
    public static boolean isJavaIdentifierStart(int codePoint) { return false; }
    public static boolean isJavaIdentifierPart(int codePoint) { return false; }
    public static boolean isUnicodeIdentifierPart(char ch) { return false; }
    public static boolean isSpaceChar(int codePoint) { return false; }
    public static int charCount(int codePoint) { return 1; }
    public static boolean isWhitespace(char ch) { return false; }
    public static int digit(char ch, int radix) { return 0; }
    public static char forDigit(int digit, int radix) { return '0'; }
}
""",
        "src/shims/java/lang/Class.java": """package java.lang;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public final class Class<T> {
    public static Class<?> forName(String name) { return null; }
    public static Class<?> forName(String name, boolean initialize, ClassLoader loader) { return null; }
    public Method getMethod(String name, Class<?>... parameterTypes) { return null; }
    public ClassLoader getClassLoader() { return null; }
    public <U> Class<? extends U> asSubclass(Class<U> clazz) { return null; }
    public T cast(Object obj) { return null; }
    public boolean isAnnotation() { return false; }
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) { return false; }
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) { return null; }
    public String getName() { return ""; }
    public String getSimpleName() { return ""; }
}
""",
        "src/shims/java/lang/reflect/Array.java": """package java.lang.reflect;

public final class Array {
    private Array() {}
    public static Object newInstance(Class<?> componentType, int length) { return null; }
}
""",
        "src/shims/java/lang/Throwable.java": """package java.lang;

public class Throwable {
    public void printStackTrace(java.io.PrintStream s) {}
    public void printStackTrace(java.io.PrintWriter s) {}
}
""",
        "src/shims/java/lang/ClassNotFoundException.java": """package java.lang;

public class ClassNotFoundException extends Exception {
    public ClassNotFoundException() {}
    public ClassNotFoundException(String msg) { super(msg); }
}
""",
        "src/shims/java/lang/NoSuchMethodException.java": """package java.lang;

public class NoSuchMethodException extends Exception {
    public NoSuchMethodException() {}
    public NoSuchMethodException(String msg) { super(msg); }
}
""",
        "src/shims/java/io/InputStreamReader.java": """package java.io;

public class InputStreamReader extends Reader {
    public InputStreamReader(InputStream in) {}
    @Override public int read(char[] cbuf, int off, int len) { return -1; }
    @Override public void close() {}
}
""",
        "src/shims/java/io/BufferedReader.java": """package java.io;

public class BufferedReader extends Reader {
    public BufferedReader(Reader in) {}
    public String readLine() { return null; }
    @Override public int read(char[] cbuf, int off, int len) { return -1; }
    @Override public void close() {}
}
""",
        "src/shims/java/net/URLClassLoader.java": """package java.net;

public class URLClassLoader extends ClassLoader {
    public URLClassLoader(URL[] urls) {}
}
""",
        "src/shims/java/net/URLStreamHandler.java": """package java.net;

public abstract class URLStreamHandler {}
""",
        "src/shims/java/security/CodeSource.java": """package java.security;

public class CodeSource {
    public CodeSource(java.net.URL location, java.security.cert.Certificate[] certs) {}
}
""",
        "src/shims/java/security/ProtectionDomain.java": """package java.security;

public class ProtectionDomain {
    public CodeSource getCodeSource() { return null; }
}
""",
        "src/shims/java/util/StringTokenizer.java": """package java.util;

public class StringTokenizer {
    public StringTokenizer(String str, String delim) {}
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
    public static <E extends Enum<E>> EnumSet<E> copyOf(java.util.Set<E> s) { return new EnumSet<>(); }
    public static <E extends Enum<E>> EnumSet<E> noneOf(Class<E> elementType) { return new EnumSet<>(); }
}
""",
    })

    for rel_path, content in shims.items():
        out = workspace / rel_path
        out.parent.mkdir(parents=True, exist_ok=True)
        out.write_text(content, encoding="utf-8")


write_basic_jre_shims()
