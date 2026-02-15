# j2cl-javac-17 experiment

## Current status

- ✅ M1: Upstream source fetch reproducible.
- ✅ M2: JVM smoke test green with fetched sources.
- ✅ M3: Reduced source tree still green on JVM smoke.
- ⏳ M4: Full `javac`-rooted J2CL transpilation is wired but currently blocked on J2CL/JRE-emulation gaps.

## Commands

```bash
# Fetch pinned OpenJDK 17 sources (sparse checkout).
./scripts/fetch_javac17_sources.sh

# Create a reduced javac source tree under work/.
./scripts/prepare_reduced_sources.sh

# Run the JVM smoke test against upstream sources.
./scripts/run_jvm_smoke_test.sh upstream

# Run the JVM smoke test against the reduced work tree.
./scripts/run_jvm_smoke_test.sh work

# Run the M4 J2CL transpilation check for full javac-rooted sources.
./scripts/run_j2cl_transpile.sh

# Optional: deterministic diagnostics output location + verbose failures.
M4_DIAG_OUT=out/m4-diagnostics M4_VERBOSE=1 ./scripts/run_j2cl_transpile.sh
```

## M4 wiring notes

- Added `scripts/run_j2cl_transpile.sh` to build the full reduced OpenJDK 17
  `javac` source tree rooted at `com/sun/tools/javac/main/Main.java` through
  `j2cl_library`.
- The script stages source trees into an isolated Bazel workspace under
  `out/j2cl-workspace`:
  - `src/jdk.compiler/share/classes/com/sun/tools/javac/**`
  - `src/jdk.compiler/share/classes/com/sun/source/**`
  - `src/java.compiler/share/classes/javax/lang/model/**`
  - `src/java.compiler/share/classes/javax/tools/**`
  - `src/java.base/share/classes/jdk/internal/javac/**`
  - `src/shims/javax/annotation/processing/**` (staged outside OpenJDK module-layout paths)
- The J2CL toolchain tuple is pinned and reproducible:
  - J2CL: `20250630`
  - Bazel: `8.3.1` (via `USE_BAZEL_VERSION`)
  - Java language/runtime: `21`
- Current target: `//:javac_full_j2cl` (full `com/sun/tools/javac` + dependencies).

- The script now emits deterministic M4 diagnostics artifacts (including blocker buckets):
  - `out/m4-diagnostics/latest.log`
  - `out/m4-diagnostics/latest-summary.md`
  - `out/m4-diagnostics/history.md`
- `run_j2cl_transpile.sh` now uses `--batch` Bazel mode to avoid stale server/zombie
  process reuse issues in long-running non-interactive sessions.
- Staging patches now synthesize `com.sun.tools.javac.resources.CompilerProperties`
  nested diagnostics helpers (`Errors`/`Warnings`/`Fragments`) from observed call-sites
  to close generated-resource wrapper gaps during J2CL transpilation.
- J2CL compile now uses `--patch-module=java.base=src/shims/java:src/shims/javax`
  to allow staged compatibility classes under `java.*`/`javax.*` namespaces.
- Added additional staged API shims for security/util/io families
  (`AccessController`, `PrivilegedAction`, `ServiceConfigurationError`,
  `BreakIterator`, `Properties`, `WeakHashMap`, `FileWriter`,
  `FileNotFoundException`, `DataInputStream`, `DataOutputStream`) which reduced
  current transpile failures from 1779 to 1248.

- Added a follow-up shim wave for reflection/runtime/file APIs (`Class`, `Runtime`,
  `System`, `Throwable`, `ClassNotFoundException`, `NoSuchMethodException`,
  `InputStreamReader`, `BufferedReader`, `URLClassLoader`, `URLStreamHandler`,
  `ProtectionDomain`) and expanded `CompilerProperties` synthesis to emit
  static fields and `Notes` entries in addition to factories, reducing
  transpile failures from 1248 to 496.

- Expanded staged compatibility shims across core utility/runtime surfaces (notably `String`, `Objects`, `Character`, `Thread`, `ByteBuffer`, charset APIs, reflection helpers, and `Files` helpers), reducing current transpile failures from 496 to 360.

- Added targeted follow-up staging rewrites/shims (DocLint bootstrap shim, broader filesystem/module/jar/path utilities, and additional String/charset/path/URL compatibility methods), reducing transpile failures from 360 to 198.

- Added another compatibility shim wave for module/file/runtime utility gaps (including `Configuration.resolveAndBind`, `ModuleLayer.defineModulesWithOneLoader`, URI scheme helpers, filesystem path/file operations, `Normalizer`, and digest/no-such-file stubs), reducing transpile failures from 198 to 138.

- Added follow-up compatibility coverage for classloading/network I/O/path APIs (`ClassLoader` hierarchy hooks, `URL`/`URLConnection` overrides, `Path`/`Files`/`FileSystems` gaps, jar manifest constructors, and normalization/object-input stubs), reducing transpile failures from 138 to 96.
- Added a follow-up staging compatibility pass that backports Java 16+ stream terminal calls (`Stream.toList()` -> `Collectors.toList()`) for J2CL compatibility and expands `CompilerProperties` static-import method synthesis, reducing transpile failures from 81 to 71.

## Initial J2CL issue tracking

See `J2CL_ISSUES.md` for active blockers currently preventing full-target transpilation success.

## JVM smoke test implementation details

The smoke harness (`src/Javac17JvmSmokeTest.java`) now does the following:

1. Verifies selected `javac` source tree is present.
2. Ensures the selected source tree has non-zero `.java` sources.
3. Uses `ToolProvider.getSystemJavaCompiler()` to compile a tiny
   `HelloWorld.java` source.
4. Verifies `HelloWorld.class` exists, is non-empty, and starts with `CAFEBABE`.

This establishes a reproducible JVM baseline for both upstream and reduced
source layouts before moving on to broader J2CL transpilation.
