#!/bin/bash
#
# Setup script for Maven HTTP proxy configuration
# Downloads Maven artifacts through authenticated HTTP proxy
#

set -e

# Extract proxy credentials from environment variables
PROXY_URL="${HTTP_PROXY:-${http_proxy}}"

if [ -z "$PROXY_URL" ]; then
  echo "ERROR: HTTP_PROXY or http_proxy environment variable not set"
  exit 1
fi

# Parse proxy URL: http://username:jwt_TOKEN@host:port
PROXY_URL_CLEAN="${PROXY_URL#http://}"
PROXY_USER="${PROXY_URL_CLEAN%%:*}"
TEMP="${PROXY_URL_CLEAN#*:}"
PROXY_PASS="${TEMP%@*}"
TEMP="${PROXY_URL_CLEAN##*@}"
PROXY_HOST="${TEMP%:*}"
PROXY_PORT="${TEMP##*:}"

# Remove "jwt_" prefix from password if present
JWT_TOKEN="${PROXY_PASS#jwt_}"

echo "Maven HTTP Proxy Setup"
echo "====================="
echo "Proxy: $PROXY_HOST:$PROXY_PORT"
echo "User:  ${PROXY_USER:0:30}..."
echo "Auth:  Bearer (JWT)"
echo ""

# Create Maven settings directory
mkdir -p ~/.m2

# Create Maven settings.xml
cat > ~/.m2/settings.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              http://maven.apache.org/xsd/settings-1.0.0.xsd">
</settings>
EOF

echo "✓ Created ~/.m2/settings.xml"

# Function to download an artifact using curl
download_artifact() {
  local artifact=$1
  local local_path="$HOME/.m2/repository/$artifact"
  local url="https://repo.maven.apache.org/maven2/$artifact"

  mkdir -p "$(dirname "$local_path")"

  # Skip if already downloaded and non-empty
  if [ -f "$local_path" ] && [ -s "$local_path" ]; then
    return 0
  fi

  # Download via proxy with Bearer token
  if curl -s -x "${PROXY_HOST}:${PROXY_PORT}" \
    -H "Proxy-Authorization: Bearer ${JWT_TOKEN}" \
    "$url" -o "$local_path" 2>/dev/null && [ -s "$local_path" ]; then
    echo "  ✓ $artifact"
    return 0
  else
    echo "  ✗ $artifact (retrying...)"
    # Clean up bad file
    rm -f "$local_path"
    return 1
  fi
}

# List of essential Maven artifacts for this project
ARTIFACTS=(
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

echo ""
echo "Downloading Maven artifacts through proxy..."
FAILED_COUNT=0
for artifact in "${ARTIFACTS[@]}"; do
  if ! download_artifact "$artifact"; then
    ((FAILED_COUNT++))
  fi
done

echo ""
if [ $FAILED_COUNT -eq 0 ]; then
  echo "✓ All artifacts downloaded successfully!"
  echo ""
  echo "You can now run Maven:"
  echo "  mvn clean compile"
else
  echo "⚠  $FAILED_COUNT artifacts failed to download"
  echo "This may cause Maven to retry network access"
fi
