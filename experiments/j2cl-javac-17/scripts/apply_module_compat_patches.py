#!/usr/bin/env python3
from pathlib import Path
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

for path, old, new in replacements:
    text = path.read_text(encoding='utf-8')
    if old in text:
        path.write_text(text.replace(old, new, 1), encoding='utf-8')
        continue
    if new in text:
        continue
    raise SystemExit(f"Expected patch hunk not found in {path}")
