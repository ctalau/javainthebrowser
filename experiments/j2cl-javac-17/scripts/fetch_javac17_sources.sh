#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
EXPERIMENT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
UPSTREAM_DIR="$EXPERIMENT_DIR/upstream/openjdk-jdk-17"

REPO_URL="${JAVAC17_REPO_URL:-https://github.com/openjdk/jdk.git}"
REPO_REF="${JAVAC17_REPO_REF:-jdk-17-ga}"

if [[ -d "$UPSTREAM_DIR/.git" ]]; then
  echo "Repository already present at $UPSTREAM_DIR"
  echo "Refreshing to ref $REPO_REF"
  git -C "$UPSTREAM_DIR" fetch --tags origin
else
  mkdir -p "$(dirname "$UPSTREAM_DIR")"
  git clone --filter=blob:none --sparse "$REPO_URL" "$UPSTREAM_DIR"
fi

git -C "$UPSTREAM_DIR" checkout "$REPO_REF"

git -C "$UPSTREAM_DIR" sparse-checkout set \
  src/jdk.compiler/share/classes \
  src/java.compiler/share/classes \
  src/java.base/share/classes/jdk/internal/javac \
  test/langtools/tools/javac

echo "Fetched OpenJDK sources:"
echo "  repo: $REPO_URL"
echo "  ref:  $REPO_REF"
echo "  dir:  $UPSTREAM_DIR"
