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

0. **Module/package conflict class addressed**
   - We no longer stage `javax.annotation.processing` sources from OpenJDK
     `java.compiler`, because this produces `package exists in another module`
     failures under the current toolchain.

1. **J2CL JRE emulation gaps vs. full javac needs**
   - Missing/unsupported APIs during transpilation include module/reflection and
     JDK APIs such as `java.io.ObjectInputStream`, `java.io.PrintWriter`,
     `java.util.regex`, `java.nio.file`, and `java.lang.ref`.
   - ✅ Closed one compatibility class in M4 staging: direct compile-time
     `java.lang.Module` type coupling in `javac` bootstrap entrypoints is now
     rewritten to reflection-backed helpers during workspace staging.

2. **Remaining compiler dependency/resource coverage**
   - Additional source/resource dependencies may still be required transitively
     beyond currently staged roots.

3. **M4 remains open until `//:javac_full_j2cl` is green**
   - Script and target are wired for full-source transpilation.
   - Next work is reducing the blocker list above until the full target builds.
