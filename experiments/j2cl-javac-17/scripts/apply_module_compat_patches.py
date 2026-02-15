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

supported_source_version = workspace / "src/java.compiler/share/classes/javax/annotation/processing/SupportedSourceVersion.java"
if not supported_source_version.exists():
    supported_source_version.parent.mkdir(parents=True, exist_ok=True)
    supported_source_version.write_text(
        """/*
 * Minimal compatibility shim for J2CL builds.
 */

package javax.annotation.processing;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.lang.model.SourceVersion;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SupportedSourceVersion {
    SourceVersion value();
}
""",
        encoding='utf-8',
    )
