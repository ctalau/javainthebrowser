# J2CL porting wiki (setbacks and resolutions)

This page captures practical issues hit while trying to move the Java-in-the-browser compiler stack toward J2CL, and what worked to unblock progress.

## 1) Could not use Maven to fetch J2CL CLI artifacts

### Symptom
- `mvn dependency:get ... com.google.j2cl:j2cl:...` failed with network reachability errors to Maven Central.

### Why it mattered
- A direct "download a standalone J2CL jar and run it" path was unreliable in this environment.

### Resolution
- Switched to the Bazel + `j2cl_library` route (the canonical upstream integration path).
- Pinned to J2CL module release + Bazel version compatible with that release.

### Takeaway for future ports
- Prefer the Bazel module workflow over ad-hoc CLI artifacts.
- Treat Maven-only flows as optional convenience, not required path.

## 2) Bazel/J2CL version mismatch

### Symptom
- With Bazel 9.x, analysis failed in J2CL dependencies (rule attribute incompatibilities, e.g. `exports`/`jars` shape mismatches).

### Why it mattered
- J2CL module expected Bazel 8.3.x behavior.

### Resolution
- Forced Bazel version `8.3.1` using `USE_BAZEL_VERSION=8.3.1`.
- Added Java tool/runtime 21 flags in `.bazelrc` for the J2CL workspace.

### Takeaway for future ports
- Always align Bazel version with the target J2CL release's compatibility range.
- Capture version tuple explicitly: **(J2CL release, Bazel version, Java version)**.

## 3) Very large Java string constants broke compilation

### Symptom
- `src/gwtjava/io/fs/FileSystemContent.java` contains huge hex literals; javac reported constant-size failures (`constant string too long`).

### Why it mattered
- This blocked both JVM-side smoke compilation and any downstream transpilation attempts involving those sources.

### Resolution
- Added `patch_filesystem_content.py` that rewrites `files.put(..., "<huge hex>")` into chunked `StringBuilder.append(...).toString()` expressions.
- Ran patching as a generated-source step in experiment scripts instead of editing original source directly.

### Takeaway for future ports
- Keep large embedded resources in generated form and chunk them before compile.
- Prefer generated patched source over hand-editing giant literals.

## 4) Full javac-port J2CL transpilation failed on JRE-emulation gaps

### Symptom
- Feeding the entire `src/javac` + `src/gwtjava` set into J2CL exposed compatibility gaps (missing emulated APIs and Java-model differences), including:
  - missing `gwtjava.lang.NoSuchMethodException`
  - `EnumSet.allOf(...)` usage incompatibility in this emulated stack
  - `finalize()` override issues in `ZipFileIndex`

### Why it mattered
- A full compiler-port transpile is significantly harder than a minimal hello-world J2CL validation.

### Resolution
- Added minimal compatibility shims/fixes where safe:
  - new `gwtjava.lang.NoSuchMethodException`
  - replaced `EnumSet.allOf(...)` with `noneOf(...) + values()` population
  - replaced `finalize()` override with explicit cleanup method
- Kept a **separate minimal J2CL hello target** (`HelloJ2cl`) to ensure J2CL pipeline validation remains green while broader porting continues.

### Takeaway for future ports
- Split work into:
  1. "J2CL pipeline sanity" (small target)
  2. "Full compiler slice migration" (iterative compatibility backlog)
- This avoids blocking all progress on one giant migration step.

## 5) Bazel server/process stability and long build bootstrap time

### Symptom
- Long first builds due to toolchain + dependency bootstrap.
- Occasional stale output-base lock / stale server process behavior.

### Why it mattered
- Re-runs could fail for infrastructure reasons unrelated to source correctness.

### Resolution
- Use isolated workspace and explicit output user root for experiment builds.
- Re-run after stale lock clears; avoid sharing output base with unrelated builds.
- Expect first run to be slow; subsequent cached builds are much faster.

### Takeaway for future ports
- Budget first-build warm-up time.
- Keep experiment builds isolated from repo-global Bazel state.

## 6) Scope control was essential

### Symptom
- Attempting to prove "full compiler transpiles" and "hello-world compile works" in one shot created unstable feedback loops.

### Resolution
- Established two deterministic checks:
  1. JVM smoke test for the in-repo javac port (`HelloWorldCompileSmokeTest`).
  2. Real J2CL transpilation check for a minimal hello class (`hello_j2cl.js` output).

### Takeaway for future ports
- Keep checks orthogonal and small.
- Promote additional compiler modules into the J2CL target incrementally, with each incompatibility tracked and fixed one-by-one.

---

## Suggested migration playbook for newer javac versions

1. **Pin toolchain tuple first** (J2CL, Bazel, Java).
2. **Run minimal J2CL hello target** to validate infra.
3. **Import next compiler package slice** (e.g., `util` then `code` then `parser` etc.).
4. **Record each incompatibility** as either:
   - missing emulated class/API,
   - reflection/annotation mismatch,
   - unsupported JVM-specific behavior.
5. **Patch via shim or refactor** in smallest safe change.
6. **Keep JVM smoke test green** while expanding J2CL scope.
7. **Regenerate/patch oversized literals** as build step, never manually.
