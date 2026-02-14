# j2cl-javac-17 experiment

## Current status

- ✅ M1: Upstream source fetch reproducible.
- ✅ M2: JVM smoke test green with fetched sources.

## Commands

```bash
# Fetch pinned OpenJDK 17 sources (sparse checkout).
./scripts/fetch_javac17_sources.sh

# Run the JVM smoke test harness.
./scripts/run_jvm_smoke_test.sh
```

## JVM smoke test implementation details

The smoke harness (`src/Javac17JvmSmokeTest.java`) does the following:

1. Verifies fetched OpenJDK 17 `javac` sources are present under `upstream/`.
2. Uses `ToolProvider.getSystemJavaCompiler()` to compile a tiny `HelloWorld.java` source.
3. Verifies `HelloWorld.class` exists, is non-empty, and starts with `CAFEBABE`.

This establishes a reproducible JVM baseline before moving on to source reduction and J2CL transpilation.
