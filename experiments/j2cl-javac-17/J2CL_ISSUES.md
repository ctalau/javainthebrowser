# J2CL issue register (j2cl-javac-17)

This file tracks blockers while transpiling the full `javac`-rooted source set
(starting from `com/sun/tools/javac/main/Main.java`) with J2CL.

## Scope baseline (current target)

- `com/sun/tools/javac/**`
- `com/sun/source/**`
- `javax/lang/model/**`
- `javax/tools/**`
- `jdk/internal/javac/**`

## Current diagnostics workflow

- Canonical command: `./scripts/run_j2cl_transpile.sh`
- Log artifact: `out/m4-diagnostics/latest.log`
- Deterministic summary: `out/m4-diagnostics/latest-summary.md`
- History timeline: `out/m4-diagnostics/history.md`

## Open blockers

0. **`javax.annotation.processing` compatibility class addressed (closed)**
   - `run_j2cl_transpile.sh` now stages `javax.annotation.processing/**` into
     `src/shims/**` (instead of `src/java.compiler/share/classes/**`) so javac
     does not treat it as a conflicting module-owned package.
   - This closes both blocker classes together: `package exists in another
     module: java.base` and the large unresolved
     `javax.annotation.processing.*` symbol cascade (`Processor`,
     `ProcessingEnvironment`, `FilerException`, `SupportedSourceVersion`).

1. **J2CL JRE emulation gaps vs. full javac needs**
   - Missing/unsupported APIs during transpilation include module/reflection and
     JDK APIs such as `java.io.ObjectInputStream`, `java.io.PrintWriter`,
     `java.util.regex`, `java.nio.file`, and `java.lang.ref`.
   - ✅ Closed one compatibility class in M4 staging: direct compile-time
     `java.lang.Module` type coupling in `javac` bootstrap entrypoints is now
     rewritten to reflection-backed helpers during workspace staging.

2. **Compiler resource wrapper class addressed (partial close)**
   - Added staging-time generation of `com.sun.tools.javac.resources.CompilerProperties`
     with nested `Errors`/`Warnings`/`Fragments` factories based on call-site scans.
   - This removes the broad unresolved `Errors`/`Warnings`/`Fragments` symbol class
     and reduced total transpile errors from 2561 to 2129 in the latest run.

3. **Core tool API shim wave addressed (partial close)**
   - Staging patches now synthesize minimal JRE shims for `java.io.PrintWriter`,
     `java.net.URI`/`URL`/`URLConnection`, `java.util.ServiceLoader`,
     `java.lang.ClassLoader`, and `java.nio.CharBuffer` into `src/shims/**`.
   - This removed one broad unresolved-type class (tool I/O + URI/service-loader
     surfaces) and reduced total transpile errors from 2129 to 1779.

4. **Remaining compiler dependency/resource coverage**
   - Additional source/resource dependencies may still be required transitively
     beyond currently staged roots.

5. **M4 remains open until `//:javac_full_j2cl` is green**
   - Script and target are wired for full-source transpilation.
   - Next work is reducing the blocker list above until the full target builds.
