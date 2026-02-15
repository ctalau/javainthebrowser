#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
EXPERIMENT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
REDUCED_ROOT="$EXPERIMENT_DIR/work/openjdk-jdk-17-reduced"
OUT_DIR="$EXPERIMENT_DIR/out"
WORKSPACE_DIR="$OUT_DIR/j2cl-workspace"
BAZEL_USER_ROOT="$OUT_DIR/bazel-user-root-full"

if [[ ! -f "$REDUCED_ROOT/src/jdk.compiler/share/classes/com/sun/tools/javac/main/Main.java" ]]; then
  echo "Reduced sources are missing. Preparing reduced sources first..."
  "$SCRIPT_DIR/prepare_reduced_sources.sh"
fi

rm -rf "$WORKSPACE_DIR"
mkdir -p "$WORKSPACE_DIR"

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
    ]),
    javacopts = ["-source", "17", "-target", "17"],
)
BUILD

(
  cd "$WORKSPACE_DIR"
  USE_BAZEL_VERSION=8.3.1 bazel --output_user_root="$BAZEL_USER_ROOT" build //:javac_full_j2cl --color=no --curses=no
)

echo "J2CL transpilation build completed for //:javac_full_j2cl"
echo "Workspace: $WORKSPACE_DIR"
echo "Bazel user root: $BAZEL_USER_ROOT"
