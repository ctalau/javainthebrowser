#!/usr/bin/env bash
set -euo pipefail

ROOT=$(cd "$(dirname "$0")/../../.." && pwd)
EXP="$ROOT/experiments/j2cl-javac"
OUT="$EXP/out"
PATCHED_SRC="$OUT/patched-src"
CLASSES="$OUT/classes"

rm -rf "$OUT"
mkdir -p "$PATCHED_SRC/gwtjava/io/fs" "$CLASSES"

"$EXP/scripts/patch_filesystem_content.py" \
  "$ROOT/src/gwtjava/io/fs/FileSystemContent.java" \
  "$PATCHED_SRC/gwtjava/io/fs/FileSystemContent.java"

javac -source 8 -target 8 -d "$CLASSES" \
  "$PATCHED_SRC/gwtjava/io/fs/FileSystemContent.java" \
  $(find "$ROOT/src/javac" "$ROOT/src/gwtjava" -name '*.java' ! -path '*/FileSystemContent.java') \
  "$EXP/src/HelloWorldCompileSmokeTest.java"

java -cp "$CLASSES" HelloWorldCompileSmokeTest

"$EXP/scripts/run_j2cl_transpile.sh"
