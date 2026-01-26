#!/bin/bash
#
# Build script that handles HTTP proxy authentication for Maven
# Automatically downloads dependencies and runs Maven offline
#

set -e

PROXY_HOST="${PROXY_HOST:-21.0.0.195}"
PROXY_PORT="${PROXY_PORT:-15004}"
MAVEN_REPO="${MAVEN_REPO:-$HOME/.m2/repository}"
MAVEN_CENTRAL="https://repo.maven.apache.org/maven2"

echo "============================================"
echo "Maven Build with HTTP Proxy Authentication"
echo "============================================"
echo ""
echo "Proxy: $PROXY_HOST:$PROXY_PORT"
echo "Repository: $MAVEN_REPO"
echo ""

# Step 1: Download Maven dependencies using curl  
# curl automatically uses HTTP_PROXY/HTTPS_PROXY environment variables

echo "Step 1: Downloading dependencies with curl..."
echo "  curl will use HTTP_PROXY/HTTPS_PROXY from environment"
echo ""

# Run Maven with -X debug flag to see what it's trying to download
# Then manually download those with curl
mkdir -p "$MAVEN_REPO"

# Core Maven plugin artifacts
DEPS=(
  "org/apache/maven/plugins/maven-clean-plugin/3.2.0/maven-clean-plugin-3.2.0.pom"
  "org/apache/maven/plugins/maven-clean-plugin/3.2.0/maven-clean-plugin-3.2.0.jar"
  "org/apache/maven/plugins/maven-resources-plugin/3.3.1/maven-resources-plugin-3.3.1.pom"
  "org/apache/maven/plugins/maven-resources-plugin/3.3.1/maven-resources-plugin-3.3.1.jar"
  "org/apache/maven/plugins/maven-compiler-plugin/3.11.0/maven-compiler-plugin-3.11.0.pom"
  "org/apache/maven/plugins/maven-compiler-plugin/3.11.0/maven-compiler-plugin-3.11.0.jar"
  "org/apache/maven/plugins/maven-surefire-plugin/3.2.5/maven-surefire-plugin-3.2.5.pom"
  "org/apache/maven/plugins/maven-surefire-plugin/3.2.5/maven-surefire-plugin-3.2.5.jar"
  "org/apache/maven/plugins/maven-war-plugin/3.4.0/maven-war-plugin-3.4.0.pom"
  "org/apache/maven/plugins/maven-war-plugin/3.4.0/maven-war-plugin-3.4.0.jar"
  "org/codehaus/mojo/build-helper-maven-plugin/3.5.0/build-helper-maven-plugin-3.5.0.pom"
  "org/codehaus/mojo/build-helper-maven-plugin/3.5.0/build-helper-maven-plugin-3.5.0.jar"
  "org/codehaus/mojo/gwt-maven-plugin/2.5.1/gwt-maven-plugin-2.5.1.pom"
  "org/codehaus/mojo/gwt-maven-plugin/2.5.1/gwt-maven-plugin-2.5.1.jar"
  "com/google/gwt/gwt-user/2.5.1/gwt-user-2.5.1.pom"
  "com/google/gwt/gwt-user/2.5.1/gwt-user-2.5.1.jar"
  "com/google/gwt/gwt-dev/2.5.1/gwt-dev-2.5.1.pom"
  "com/google/gwt/gwt-dev/2.5.1/gwt-dev-2.5.1.jar"
  "junit/junit/4.13.2/junit-4.13.2.pom"
  "junit/junit/4.13.2/junit-4.13.2.jar"
)

SUCCESS=0
FAILED=0

for dep in "${DEPS[@]}"; do
  local_path="$MAVEN_REPO/$dep"
  mkdir -p "$(dirname "$local_path")"
  
  if curl -s "$MAVEN_CENTRAL/$dep" -o "$local_path" && [ -s "$local_path" ]; then
    ((SUCCESS++))
  else
    ((FAILED++))
    rm -f "$local_path"
  fi
done

echo "  Downloaded: $SUCCESS artifacts"
[ $FAILED -gt 0 ] && echo "  Failed: $FAILED artifacts"
echo ""

# Step 2: Run Maven
echo "Step 2: Running Maven with cached dependencies..."
echo ""

mvn clean compile

echo ""
echo "============================================"
echo "Build Complete!"
echo "============================================"
