#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
EXPERIMENT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
OUT_DIR="$EXPERIMENT_DIR/out/jvm-smoke"
CLASSES_DIR="$OUT_DIR/classes"
SOURCE_TREE="${1:-upstream}"

mkdir -p "$CLASSES_DIR"

if [[ "$SOURCE_TREE" == "upstream" ]]; then
  SOURCE_ROOT_REL="upstream/openjdk-jdk-17"
  if [[ ! -f "$EXPERIMENT_DIR/$SOURCE_ROOT_REL/src/jdk.compiler/share/classes/com/sun/tools/javac/main/Main.java" ]]; then
    echo "Missing fetched OpenJDK sources. Running fetch script first..."
    "$SCRIPT_DIR/fetch_javac17_sources.sh"
  fi
elif [[ "$SOURCE_TREE" == "work" ]]; then
  SOURCE_ROOT_REL="work/openjdk-jdk-17-reduced"
  if [[ ! -f "$EXPERIMENT_DIR/$SOURCE_ROOT_REL/src/jdk.compiler/share/classes/com/sun/tools/javac/main/Main.java" ]]; then
    echo "Missing reduced source tree. Running reduction script first..."
    "$SCRIPT_DIR/prepare_reduced_sources.sh"
  fi
else
  echo "Usage: $0 [upstream|work]" >&2
  exit 2
fi

javac -d "$CLASSES_DIR" "$EXPERIMENT_DIR/src/Javac17JvmSmokeTest.java"

java -cp "$CLASSES_DIR" Javac17JvmSmokeTest "$EXPERIMENT_DIR" "$SOURCE_ROOT_REL"
