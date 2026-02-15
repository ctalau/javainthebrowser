#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
EXPERIMENT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
REDUCED_ROOT="$EXPERIMENT_DIR/work/openjdk-jdk-17-reduced"
OUT_DIR="$EXPERIMENT_DIR/out"
WORKSPACE_DIR="$OUT_DIR/j2cl-workspace"
BAZEL_USER_ROOT="$OUT_DIR/bazel-user-root-full"
DIAG_DIR="${M4_DIAG_OUT:-$OUT_DIR/m4-diagnostics}"
M4_VERBOSE="${M4_VERBOSE:-0}"

if [[ ! -f "$REDUCED_ROOT/src/jdk.compiler/share/classes/com/sun/tools/javac/main/Main.java" ]]; then
  echo "Reduced sources are missing. Preparing reduced sources first..."
  "$SCRIPT_DIR/prepare_reduced_sources.sh"
fi

rm -rf "$WORKSPACE_DIR"
mkdir -p "$WORKSPACE_DIR" "$DIAG_DIR"

copy_tree() {
  local rel="$1"
  local src="$REDUCED_ROOT/$rel"
  local dst="$WORKSPACE_DIR/$rel"
  if [[ -d "$src" ]]; then
    mkdir -p "$(dirname "$dst")"
    cp -R "$src" "$dst"
  else
    echo "warning: missing source subtree $src" >&2
  fi
}

# Stage full reduced javac tree plus dependencies required by OpenJDK javac sources.
copy_tree "src/jdk.compiler/share/classes/com/sun/tools/javac"
copy_tree "src/jdk.compiler/share/classes/com/sun/source"
copy_tree "src/java.compiler/share/classes/javax/lang/model"
copy_tree "src/java.compiler/share/classes/javax/tools"
copy_tree "src/java.base/share/classes/jdk/internal/javac"

# Stage javax.annotation.processing away from OpenJDK module-layout paths to
# avoid package-ownership/module conflicts while preserving API availability.
if [[ -d "$REDUCED_ROOT/src/java.compiler/share/classes/javax/annotation/processing" ]]; then
  mkdir -p "$WORKSPACE_DIR/src/shims/javax/annotation"
  cp -R "$REDUCED_ROOT/src/java.compiler/share/classes/javax/annotation/processing" "$WORKSPACE_DIR/src/shims/javax/annotation/processing"
fi

"$SCRIPT_DIR/apply_module_compat_patches.py" "$WORKSPACE_DIR"

cat > "$WORKSPACE_DIR/MODULE.bazel" <<'MODULE'
module(name = "j2cl_javac17_experiment")

bazel_dep(name = "j2cl", version = "20250630")
MODULE

cat > "$WORKSPACE_DIR/.bazelrc" <<'BAZELRC'
build --java_language_version=21
build --java_runtime_version=21
build --tool_java_language_version=21
build --tool_java_runtime_version=21
build --strategy=J2cl=worker
build --strategy=J2clStrip=worker
build --strategy=Closure=worker
build --strategy=Javac=worker
BAZELRC

cat > "$WORKSPACE_DIR/BUILD" <<'BUILD'
load("@j2cl//build_defs:rules.bzl", "j2cl_library")

j2cl_library(
    name = "javac_full_j2cl",
    srcs = glob([
        "src/jdk.compiler/share/classes/com/sun/tools/javac/**/*.java",
        "src/jdk.compiler/share/classes/com/sun/source/**/*.java",
        "src/java.compiler/share/classes/javax/lang/model/**/*.java",
        "src/java.compiler/share/classes/javax/tools/**/*.java",
        "src/java.base/share/classes/jdk/internal/javac/**/*.java",
        "src/shims/**/*.java",
    ]),
    javacopts = ["-source", "17", "-target", "17"],
)
BUILD

LOG_PATH="$DIAG_DIR/latest.log"
SUMMARY_PATH="$DIAG_DIR/latest-summary.md"
HISTORY_PATH="$DIAG_DIR/history.md"

build_cmd=(USE_BAZEL_VERSION=8.3.1 bazel --batch --output_user_root="$BAZEL_USER_ROOT" build //:javac_full_j2cl --color=no --curses=no)
if [[ "$M4_VERBOSE" == "1" ]]; then
  build_cmd+=(--verbose_failures)
fi

set +e
(
  cd "$WORKSPACE_DIR"
  env "${build_cmd[@]}"
) 2>&1 | tee "$LOG_PATH"
build_status=${PIPESTATUS[0]}
set -e

python3 - "$LOG_PATH" "$SUMMARY_PATH" "$build_status" <<'PY'
import collections
import pathlib
import re
import sys

log_path = pathlib.Path(sys.argv[1])
summary_path = pathlib.Path(sys.argv[2])
build_status = int(sys.argv[3])
lines = log_path.read_text(encoding='utf-8', errors='replace').splitlines()

error_lines = [
    line for line in lines
    if line.startswith("Error:") or line.startswith("ERROR:")
]

source_counter = collections.Counter()
missing_symbols = []
missing_packages = []

for line in lines:
    src_match = re.search(r"Error:([^:]+\.java):", line)
    if src_match:
        source_counter[src_match.group(1)] += 1

    pkg_match = re.search(r"package\s+([A-Za-z0-9_.$]+)\s+(?:does not exist|cannot be resolved)", line)
    if pkg_match:
        missing_packages.append(pkg_match.group(1))

    for pattern in [
        r"Error:[^:]+:\d+:\s+([A-Za-z0-9_.$]+)\s+cannot be resolved(?:\s+to\s+a\s+type)?",
        r"Error:[^:]+:\d+:\s+([A-Za-z0-9_.$]+)\s+cannot be resolved or is not a field",
        r"The method\s+([A-Za-z0-9_.$]+)\([^)]*\)\s+is undefined",
        r"The import\s+([A-Za-z0-9_.$]+)\s+cannot be resolved",
    ]:
        m = re.search(pattern, line)
        if m:
            missing_symbols.append(m.group(1))

sym_counter = collections.Counter(missing_symbols)
pkg_counter = collections.Counter(missing_packages)

bucket_rules = [
    ("Missing OpenJDK roots/dependencies", lambda s: s.startswith(("com.sun.tools.javac.resources",))),
    ("J2CL JRE gap: java.io/java.nio/java.lang.ref", lambda s: s.startswith(("PrintWriter", "ObjectInputStream", "Path", "Files", "FileSystem", "SoftReference", "WeakReference"))),
    ("J2CL JRE gap: regex/module/reflection", lambda s: s.startswith(("Pattern", "Matcher", "Module", "Method"))),
    ("Source-level incompatibility", lambda s: s.startswith(("Runtime", "Character", "ClassLoader"))),
    ("Other/uncategorized", lambda s: True),
]

bucket_counts = collections.Counter()
for symbol, count in sym_counter.items():
    for bucket, matcher in bucket_rules:
        if matcher(symbol):
            bucket_counts[bucket] += count
            break

def sorted_top(counter, n=20):
    return sorted(counter.items(), key=lambda kv: (-kv[1], kv[0]))[:n]

out = []
out.append("# M4 diagnostics summary")
out.append("")
out.append(f"- Build status: `{build_status}`")
out.append(f"- Total error lines: `{len(error_lines)}`")
out.append(f"- Unique missing symbols: `{len(sym_counter)}`")
out.append(f"- Unique missing packages: `{len(pkg_counter)}`")
out.append("")
out.append("## Top failing sources")
out.append("")
for src, count in sorted_top(source_counter):
    out.append(f"- `{src}`: {count}")
if not source_counter:
    out.append("- none")
out.append("")
out.append("## Top missing symbols")
out.append("")
for symbol, count in sorted_top(sym_counter):
    out.append(f"- `{symbol}`: {count}")
if not sym_counter:
    out.append("- none")
out.append("")
out.append("## Top missing packages")
out.append("")
for package, count in sorted_top(pkg_counter):
    out.append(f"- `{package}`: {count}")
if not pkg_counter:
    out.append("- none")
out.append("")
out.append("## Blocker buckets")
out.append("")
for bucket, count in sorted_top(bucket_counts):
    out.append(f"- `{bucket}`: {count}")
if not bucket_counts:
    out.append("- none")
summary_path.write_text("\n".join(out) + "\n", encoding="utf-8")
PY

timestamp="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
{
  echo "- $timestamp status=$build_status log=$LOG_PATH summary=$SUMMARY_PATH"
} >> "$HISTORY_PATH"

if [[ "$build_status" -ne 0 ]]; then
  echo "J2CL transpilation build failed for //:javac_full_j2cl"
  echo "Diagnostics log: $LOG_PATH"
  echo "Diagnostics summary: $SUMMARY_PATH"
  exit "$build_status"
fi

echo "J2CL transpilation build completed for //:javac_full_j2cl"
echo "Workspace: $WORKSPACE_DIR"
echo "Bazel user root: $BAZEL_USER_ROOT"
echo "Diagnostics log: $LOG_PATH"
echo "Diagnostics summary: $SUMMARY_PATH"
