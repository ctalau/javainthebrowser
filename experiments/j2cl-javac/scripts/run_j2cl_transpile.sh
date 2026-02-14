#!/usr/bin/env bash
set -euo pipefail

ROOT=$(cd "$(dirname "$0")/../../.." && pwd)
EXP="$ROOT/experiments/j2cl-javac"
OUT="$EXP/out"
WS="$OUT/j2cl-workspace"

rm -rf "$WS"
mkdir -p "$WS/src"

cp "$EXP/src/HelloJ2cl.java" "$WS/src/"

cat > "$WS/MODULE.bazel" <<'MODULE'
module(name = "j2cl_javac_experiment")

bazel_dep(name = "j2cl", version = "20250630")
MODULE

cat > "$WS/.bazelrc" <<'BAZELRC'
build --java_language_version=21
build --java_runtime_version=21
build --tool_java_language_version=21
build --tool_java_runtime_version=21
build --strategy=J2cl=worker
build --strategy=J2clStrip=worker
build --strategy=Closure=worker
build --strategy=Javac=worker
BAZELRC

cat > "$WS/BUILD" <<'BUILD'
load("@j2cl//build_defs:rules.bzl", "j2cl_library")

j2cl_library(
    name = "hello_j2cl",
    srcs = ["src/HelloJ2cl.java"],
)
BUILD

(
  cd "$WS"
  USE_BAZEL_VERSION=8.3.1 bazel --output_user_root="$OUT/bazel-user-root" build //:hello_j2cl --color=no --curses=no
)

echo "J2CL transpilation build completed for //:hello_j2cl"
