#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
EXPERIMENT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
OUT_DIR="$EXPERIMENT_DIR/out/jvm-smoke"
CLASSES_DIR="$OUT_DIR/classes"

mkdir -p "$CLASSES_DIR"

if [[ ! -f "$EXPERIMENT_DIR/upstream/openjdk-jdk-17/src/jdk.compiler/share/classes/com/sun/tools/javac/main/Main.java" ]]; then
  echo "Missing fetched OpenJDK sources. Running fetch script first..."
  "$SCRIPT_DIR/fetch_javac17_sources.sh"
fi

javac -d "$CLASSES_DIR" "$EXPERIMENT_DIR/src/Javac17JvmSmokeTest.java"

java -cp "$CLASSES_DIR" Javac17JvmSmokeTest "$EXPERIMENT_DIR"
