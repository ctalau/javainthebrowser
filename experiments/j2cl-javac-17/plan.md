# j2cl-javac-17 plan

## Scope

Build an experiment that ports OpenJDK 17 `javac` (`jdk.compiler`) to JavaScript via J2CL, validates behavior with smoke tests, and progressively expands compatibility with upstream tests.

## Repository layout

- `scripts/fetch_javac17_sources.sh` — fetches OpenJDK sources (without committing them).
- `upstream/` — local checkout location for fetched OpenJDK source.
- `work/` — reduced/edited copy used for J2CL migration work.
- `out/` — generated build artifacts.

## Execution plan

1. **Bootstrap experiment directory**
   - Create this experiment folder with scripts, docs, and ignored output directories.
   - Add fetch script for OpenJDK 17 sources (no vendored sources committed yet).

2. **Fetch and pin upstream `javac` sources**
   - Run `scripts/fetch_javac17_sources.sh` to clone OpenJDK with sparse checkout for:
     - `src/jdk.compiler/share/classes`
     - `test/langtools/tools/javac` (for later compatibility validation)
   - Pin a known revision/tag in the script defaults to keep runs reproducible.

3. **Build upstream `javac` on JVM + smoke test**
   - Add a JVM harness that compiles minimal Java input using the fetched compiler sources.
   - Establish a smoke test (Hello World class -> bytecode bytes emitted).
   - Record required classpath/stubs to run the compiler outside full JDK build.

4. **Create a reduced `javac` source subset**
   - Copy sources from `upstream` into `work`.
   - Iteratively remove unrelated JDK code while keeping JVM smoke test green.
   - Keep each reduction step validated by the same smoke test.

5. **Run J2CL on reduced sources**
   - Integrate J2CL build target(s) for reduced `javac` sources.
   - Use `experiments/j2cl-javac/wiki.md` (Java 7 port notes) to proactively handle known blockers.
   - Generate and maintain an issue list of compile/transpile failures.

6. **Resolve J2CL compatibility issues**
   - Work through issue list:
     - unsupported JDK APIs
     - reflection/resource loading assumptions
     - file manager / filesystem abstractions
     - any forbidden bytecode/runtime patterns for JS output
   - Re-run J2CL compile after each change set.

7. **JS `javac` smoke test**
   - Build a JS runtime harness (Node/browser-targeted as appropriate) that invokes transpiled `javac`.
   - Add a smoke test that compiles a small Java source and validates class output exists and is non-empty.

8. **Run upstream `javac` tests against JS port**
   - Start from a curated subset of `test/langtools/tools/javac`.
   - Execute JS `javac` and track failing cases.
   - Categorize failures (semantic mismatch, unsupported feature, infra/test harness gap).

9. **Fix failing cases iteratively**
   - Prioritize correctness-critical failures first.
   - Add regression tests per fixed category.
   - Expand test coverage until a meaningful subset (or full target subset) passes consistently.

10. **Stabilize and document**
    - Document build/test commands, known limitations, and remaining blockers.
    - Provide reproducible scripts for fetch, build, transpile, and test phases.
    - Prepare summary metrics (pass/fail counts, unsupported features list).

## Milestone checkpoints

- [x] **M1:** Upstream source fetch reproducible.
- [x] **M2:** JVM smoke test green with fetched sources.
  - Implemented with `scripts/run_jvm_smoke_test.sh` and `src/Javac17JvmSmokeTest.java`.
  - Harness verifies fetched OpenJDK `javac` source presence, compiles `HelloWorld.java` on JVM, and validates emitted `.class` output is non-empty with `CAFEBABE` magic.
- [x] **M3:** Reduced source tree still green on JVM smoke.
  - Implemented with `scripts/prepare_reduced_sources.sh` and enhancements to
    `scripts/run_jvm_smoke_test.sh` + `src/Javac17JvmSmokeTest.java`.
  - Reduction keeps `com/sun/tools/javac` and `com/sun/source` in
    `work/openjdk-jdk-17-reduced`, emits `REDUCTION_NOTES.md`, and reuses the
    same JVM smoke assertions against both `upstream` and `work` trees.
- [ ] **M4:** J2CL transpilation succeeds.
  - Implemented wiring in `scripts/run_j2cl_transpile.sh` for full `javac`-rooted sources (`com/sun/tools/javac/main/Main.java` entrypoint graph) plus required `javax.*` and `jdk.internal.javac` dependencies.
  - Current `j2cl_library` target `//:javac_full_j2cl` compiles the full staged source set with Java 17 source/target settings.
  - Remaining blockers are tracked in `J2CL_ISSUES.md` (J2CL JRE emulation/API gaps and additional missing dependencies).
- [ ] **M5:** JS smoke test green.
- [ ] **M6:** Initial upstream test subset green with tracked failures/fixes.
