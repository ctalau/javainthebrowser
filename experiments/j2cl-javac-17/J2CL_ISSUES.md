# J2CL issue register (j2cl-javac-17)

This file tracks blockers while transpiling the full `javac`-rooted source set
(starting from `com/sun/tools/javac/main/Main.java`) with J2CL.

## Scope baseline (current target)

- `com/sun/tools/javac/**`
- `com/sun/source/**`
- `javax/lang/model/**`
- `javax/tools/**`
- `jdk/internal/javac/**`

## Open blockers

1. **J2CL JRE emulation gaps vs. full javac needs**
   - Missing/unsupported APIs during transpilation include modules and JDK APIs such as
     `java.lang.Module`, `java.io.ObjectInputStream`, `java.io.PrintWriter`,
     `java.util.regex`, `java.nio.file`, `java.lang.ref`, and related methods.

2. **Additional compiler module dependencies not yet staged**
   - `javac` still references resources/types not currently available in the staged set
     (for example `com.sun.tools.javac.resources.*` and annotation-processing APIs).

3. **M4 remains open until `//:javac_full_j2cl` is green**
   - Script and target are now wired for full-source transpilation.
   - Next work is reducing the blocker list above until the full target builds.
