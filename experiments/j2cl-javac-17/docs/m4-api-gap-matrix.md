# M4 API gap matrix

This matrix tracks major API families currently blocking full `//:javac_full_j2cl` transpilation and the selected handling strategy.

| API / surface | Typical failure shape | Strategy | Notes |
|---|---|---|---|
| `java.io` (`PrintWriter`, `ObjectInputStream`) | unsupported or missing method/type during J2CL compile | 2) polyfillable shim | Introduce narrow wrappers in project shim layer and patch call sites incrementally. |
| `java.util.regex` | missing regex APIs / behavior differences | 1) refactor usage to available APIs, then 2) shim as needed | Prefer call-site containment over broad replacement. |
| `java.nio.file` | path/file APIs not available in J2CL runtime | 3) isolate compiler file-manager interactions behind adapter | Keep JVM behavior untouched; branch at abstraction points. |
| `java.lang.ref` (`WeakReference`, `SoftReference`) | missing/partial reference semantics | 4) isolate unsupported semantics | Treat as deferred non-critical behavior for JS target where possible. |
| `java.lang.Module` and module metadata | missing module APIs | 3) conditional isolation | Route through service layer that can no-op in JS mode. |
| `com.sun.tools.javac.resources.*` | missing compiler resource bundles | 1) add source roots/resources first | Added staging of `resources` package in source graph. |

