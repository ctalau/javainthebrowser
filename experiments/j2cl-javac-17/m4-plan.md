# M4 execution plan: full javac-rooted J2CL transpilation

This plan defines concrete next steps to move M4 from “wired but failing” to
“green” for the full `javac`-rooted source graph beginning at
`com/sun/tools/javac/main/Main.java`.

## Current baseline

- Full-source target is wired in `scripts/run_j2cl_transpile.sh` as
  `//:javac_full_j2cl`.
- Current staged source roots include:
  - `src/jdk.compiler/share/classes/com/sun/tools/javac/**`
  - `src/jdk.compiler/share/classes/com/sun/source/**`
  - `src/java.compiler/share/classes/javax/lang/model/**`
  - `src/java.compiler/share/classes/javax/tools/**`
  - `src/java.base/share/classes/jdk/internal/javac/**`
- Current transpilation failure categories include missing/unsupported JRE and
  platform APIs (for example module/reflection/io/regex/path/reference APIs),
  plus unresolved compiler resources/types.

---

## Goal definition for M4

M4 is complete when all three conditions hold:

1. `./scripts/run_j2cl_transpile.sh` exits 0 on a clean checkout.
2. `bazel build //:javac_full_j2cl` (inside generated workspace) exits 0.
3. Failures are reduced to zero for current staged source roots (no ignored
   errors, no TODO “temporary skip” flags in build).

---

## Workstream A — deterministic failure inventory

### A1. Capture stable failing diagnostics

- Run `./scripts/run_j2cl_transpile.sh` and capture full output to
  `out/m4-diagnostics/latest.log`.
- Deduplicate errors by symbol and package, not by raw line text.
- Generate `out/m4-diagnostics/latest-summary.md` containing:
  - total error count
  - unique missing type/method count
  - top 20 failing packages
  - top 20 missing symbols

### A2. Categorize blockers

Classify each unique failure into exactly one bucket:

1. Missing OpenJDK source roots not yet staged.
2. J2CL JRE emulation gap (API absent/partial in J2CL runtime).
3. Source-level incompatibility (language/API usage accepted by javac but not by
   J2CL toolchain assumptions).
4. Resource/config mismatch (e.g. compiler resource bundles).
5. Build graph issue (target wiring, classpath shape, duplicate source roots).

### A3. Prioritize by “unblock multiplier”

Score each failure class by how many downstream errors it can collapse.
Prioritize in this order:

1. root missing packages/symbol providers
2. broad runtime APIs (`java.io`, `java.lang`, `java.util`, `java.nio`)
3. compiler resource classes
4. leaf utility types

---

## Workstream B — source graph completion

### B1. Identify minimal additional roots required by javac graph

Using import/use-site scans from current staged roots, compute candidate roots to
add from upstream checkout (without adding whole modules blindly).

Initial expected candidates:

- `src/jdk.compiler/share/classes/com/sun/tools/javac/resources/**`
- selected `javax.annotation.processing/**` from `src/java.compiler`
- any additional `jdk.compiler` sibling packages referenced transitively by
  existing staged sources

### B2. Update fetch + reduce scripts safely

- Extend `scripts/fetch_javac17_sources.sh` sparse checkout only for roots proven
  required by B1.
- Extend `scripts/prepare_reduced_sources.sh` copy list in lockstep.
- Keep `REDUCTION_NOTES.md` accurate with all newly included roots.

### B3. Re-run diagnostics and measure delta

After each root-addition change set:

- run transpile script
- record before/after counts in `out/m4-diagnostics/history.md`
- keep only changes that reduce net unique failures or are required prerequisites

---

## Workstream C — J2CL/JRE gap resolution strategy

### C1. Build an API gap matrix

Create `docs/m4-api-gap-matrix.md` (or similar) mapping each missing API to one
strategy:

1. available in J2CL with different usage pattern → refactor call sites
2. polyfillable in project code (`gwtjava`/shim) → implement shim
3. compiler feature can be conditionally isolated → guard and redirect
4. fundamentally unsupported for JS target → isolate behind pluggable service and
   defer to later milestone

### C2. Implement shims/adapters incrementally

For each selected API family (e.g. `PrintWriter`, `ObjectInputStream`, regex,
path/file, weak/soft references):

- add narrow adapter abstraction first
- patch small caller slice
- run transpile after each slice
- avoid broad mechanical replacement across whole tree in one commit

### C3. Keep JVM smoke baseline green

Every compatibility patch batch must preserve:

- `./scripts/run_jvm_smoke_test.sh upstream`
- `./scripts/run_jvm_smoke_test.sh work`

If a J2CL fix risks JVM behavior drift, split with explicit platform-specific
abstraction points.

---

## Workstream D — reproducibility and CI ergonomics

### D1. Make failure reports reproducible

Enhance transpile script with optional flags:

- `M4_DIAG_OUT=...` output path
- `M4_VERBOSE=1` for verbose failures
- deterministic sorting/formatting for summarized errors

### D2. Add a non-blocking CI check for progress tracking

Introduce a best-effort CI job that runs transpile and publishes diagnostics even
if failing (until M4 reaches green), so failure deltas are visible per commit.

### D3. Define M4 acceptance gate in automation

Once green locally, enforce M4 with required checks:

- full transpile command
- JVM upstream/work smoke tests
- no untracked generated-file diffs after run

---

## Incremental delivery checkpoints

1. **M4-P1 (diagnostics hardening)**
   - deterministic failure inventory + category summary in repo docs/artifacts.
2. **M4-P2 (graph completion)**
   - all required source roots staged; failure count materially reduced.
3. **M4-P3 (api-gap first wave)**
   - first high-multiplier API families handled; transpile progresses further.
4. **M4-P4 (green target)**
   - `//:javac_full_j2cl` builds successfully.
5. **M4-P5 (stabilization)**
   - command reliability + automated gate + updated milestone docs.

---

## Definition of done artifacts

Before marking M4 complete, the following should exist:

- green command transcripts for transpile and JVM smoke tests
- updated `plan.md` milestone status with brief implementation summary
- updated `README.md` with exact reproducible commands
- issue tracker (`J2CL_ISSUES.md`) trimmed to post-M4 remaining items only
- concise “what changed to make M4 green” note for future M5 work
