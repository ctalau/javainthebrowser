# M4 API gap matrix

This matrix tracks major API families currently blocking full `//:javac_full_j2cl` transpilation and the selected handling strategy.

| API / surface | Typical failure shape | Strategy | Notes |
|---|---|---|---|
| `java.io` (`PrintWriter`, `ObjectInputStream`) | unsupported or missing method/type during J2CL compile | 2) polyfillable shim | Introduce narrow wrappers in project shim layer and patch call sites incrementally. |
| `java.util.regex` | missing regex APIs / behavior differences | 1) refactor usage to available APIs, then 2) shim as needed | Prefer call-site containment over broad replacement. |
| `java.nio.file` | path/file APIs not available in J2CL runtime | 3) isolate compiler file-manager interactions behind adapter | Keep JVM behavior untouched; branch at abstraction points. |
| `java.net` + service-loader surface (`URI`, `URL`, `URLConnection`, `ServiceLoader`, `ClassLoader`) | unresolved core tool/file-manager API types | ✅ partially closed via generated shims in `src/shims/**` | `apply_module_compat_patches.py` now emits minimal compatibility definitions used by `javax.tools` and `javac` startup paths; deeper runtime semantics remain deferred. |
| `java.lang.ref` (`WeakReference`, `SoftReference`) | missing/partial reference semantics | 4) isolate unsupported semantics | Treat as deferred non-critical behavior for JS target where possible. |
| `java.lang.Module` and module metadata | missing module APIs | ✅ partially closed via reflective bridge in staged sources | Removed direct `Module` type references in `BasicJavacTask`, `ToolProvider`, and `ModuleHelper`; remaining module metadata handling still pending. |
| `javax.annotation.processing` package ownership + symbol availability | module package conflict plus unresolved `Processor`/`ProcessingEnvironment`/`FilerException`/`SupportedSourceVersion` | ✅ closed by remapped shim staging | Copy `src/java.compiler/share/classes/javax/annotation/processing/**` into `src/shims/javax/annotation/processing/**` for J2CL build input (outside OpenJDK module-layout paths). |
| `com.sun.tools.javac.resources.CompilerProperties` (`Errors`/`Warnings`/`Fragments`) | missing generated diagnostic wrapper classes | ✅ partially closed via generated staging shim | `apply_module_compat_patches.py` now synthesizes `CompilerProperties` nested classes/methods from usage sites, removing this unresolved-symbol cascade. |

