# j2cl-javac-17 experiment

## Current status

- ✅ M1: Upstream source fetch reproducible.
- ✅ M2: JVM smoke test green with fetched sources.
- ✅ M3: Reduced source tree still green on JVM smoke.

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
```

## M3 implementation notes

- Added `scripts/prepare_reduced_sources.sh` to copy a focused subset of
  `src/jdk.compiler/share/classes` into `work/openjdk-jdk-17-reduced`.
- Kept `com/sun/tools/javac` (compiler implementation) and `com/sun/source`
  (language model APIs) and generated `REDUCTION_NOTES.md` with file counts.
- Extended `src/Javac17JvmSmokeTest.java` so it can validate either the upstream
  source tree or the reduced work tree before compiling a Hello World source.
- Updated `scripts/run_jvm_smoke_test.sh` to accept `upstream|work` and run the
  same smoke harness against either source tree.

## JVM smoke test implementation details

The smoke harness (`src/Javac17JvmSmokeTest.java`) now does the following:

1. Verifies selected `javac` source tree is present.
2. Ensures the selected source tree has non-zero `.java` sources.
3. Uses `ToolProvider.getSystemJavaCompiler()` to compile a tiny
   `HelloWorld.java` source.
4. Verifies `HelloWorld.class` exists, is non-empty, and starts with `CAFEBABE`.

This establishes a reproducible JVM baseline for both upstream and reduced
source layouts before moving on to J2CL transpilation.
