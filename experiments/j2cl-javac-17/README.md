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
