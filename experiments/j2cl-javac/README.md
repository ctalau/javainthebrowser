# J2CL javac experiment

This experiment validates two things:

1. The repository's in-browser `javac` port can compile a HelloWorld sample on JVM (smoke test).
2. A hello-world Java class can be fed through J2CL successfully in this environment.

## Why the patching step exists

`src/gwtjava/io/fs/FileSystemContent.java` embeds very large JRE resources as hex string literals. On modern JDKs, some literals exceed compiler constant limits, so this experiment rewrites those literals into `StringBuilder.append(...)` chains before JVM compilation.

## Run everything

```bash
./experiments/j2cl-javac/scripts/build_and_run_hello.sh
```

Expected output includes:

- `Compiled HelloWorld.class bytes=...`
- `J2CL transpilation build completed for //:hello_j2cl`

## Run only the J2CL phase

```bash
./experiments/j2cl-javac/scripts/run_j2cl_transpile.sh
```

This builds `experiments/j2cl-javac/src/HelloJ2cl.java` with `j2cl_library` in an isolated Bazel workspace under `experiments/j2cl-javac/out/j2cl-workspace`.
