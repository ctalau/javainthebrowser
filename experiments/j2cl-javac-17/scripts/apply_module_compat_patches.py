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


def method_defs(methods: list[str], return_type: str) -> str:
    return "\n".join(
        f"        public static {return_type} {name}(Object... args) {{ return null; }}"
        for name in methods
    )


def write_compiler_properties_stub() -> None:
    errors = collect_resource_methods("Errors")
    warnings = collect_resource_methods("Warnings")
    fragments = collect_resource_methods("Fragments")

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
{method_defs(errors, "JCDiagnostic.Error")}
    }}

    public static final class Warnings {{
        private Warnings() {{}}
{method_defs(warnings, "JCDiagnostic.Warning")}
    }}

    public static final class Fragments {{
        private Fragments() {{}}
{method_defs(fragments, "JCDiagnostic.Fragment")}
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

    @Override
    public String toString() { return ""; }
}
""",
        "src/shims/java/net/URL.java": """package java.net;

import java.io.IOException;

public class URL {
    public URL(String spec) {}
    public URL(URL context, String spec) {}

    public String getPath() { return ""; }
    public URLConnection openConnection() throws IOException { return new URLConnection(this); }

    @Override
    public String toString() { return ""; }
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
    public boolean hasArray() { return false; }
    public char[] array() { return new char[0]; }

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

public interface Path {}
""",
        "src/shims/java/util/regex/Pattern.java": """package java.util.regex;

public class Pattern {
    public static Pattern compile(String regex) { return new Pattern(); }
    public Matcher matcher(CharSequence input) { return new Matcher(); }
}
""",
        "src/shims/java/util/regex/Matcher.java": """package java.util.regex;

public class Matcher {
    public boolean find() { return false; }
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
}
""",
        "src/shims/java/io/DataOutputStream.java": """package java.io;

public class DataOutputStream extends FilterOutputStream {
    public DataOutputStream(OutputStream out) { super(out); }
    public void writeInt(int v) {}
    public void writeChar(int v) {}
    public void writeLong(long v) {}
}
""",
    }

    for rel_path, content in shims.items():
        out = workspace / rel_path
        out.parent.mkdir(parents=True, exist_ok=True)
        out.write_text(content, encoding="utf-8")


write_basic_jre_shims()
