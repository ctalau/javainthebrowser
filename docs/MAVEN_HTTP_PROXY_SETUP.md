# Maven HTTP Proxy Setup Guide

This document explains how to run Maven builds in environments with restricted network access that require HTTP proxy authentication.

## Problem Statement

The build environment is behind an HTTP proxy (at `21.0.0.195:15004`) that requires JWT Bearer token authentication. Maven does not natively support this authentication method directly through its standard configuration mechanisms, making artifact downloads fail.

## Solution Overview

The solution involves two approaches:

### Approach 1: Using Environment Variables with Pre-downloaded Dependencies (Recommended)

This approach downloads Maven artifacts upfront using `curl` (which supports Bearer token auth), caches them locally, and then runs Maven normally.

#### How It Works

1. **curl supports Bearer tokens**: curl can authenticate with HTTP proxies using Bearer tokens via the `-H "Proxy-Authorization: Bearer TOKEN"` header
2. **Maven caches artifacts locally**: Downloaded artifacts are stored in `~/.m2/repository` and Maven reuses them
3. **Java HTTP doesn't use proxy env vars**: Java/Maven doesn't automatically use `http_proxy` environment variables for HTTPS CONNECT tunneling, but we work around this by pre-downloading

#### Environment Configuration

The proxy credentials are provided via environment variables in the format:
```
HTTP_PROXY=http://username:jwt_TOKEN@proxy_host:proxy_port
HTTPS_PROXY=http://username:jwt_TOKEN@proxy_host:proxy_port
```

To use these, extract the JWT token and download dependencies:

```bash
#!/bin/bash
# Extract JWT token from environment variable
PROXY_URL="$HTTP_PROXY"
PROXY_URL="${PROXY_URL#http://}"
JWT_TOKEN="${PROXY_URL#*:}"
JWT_TOKEN="${JWT_TOKEN%@*}"
JWT_TOKEN="${JWT_TOKEN#jwt_}"  # Remove "jwt_" prefix

# Download essential Maven artifacts
download_maven_artifacts() {
  local artifacts=(
    "org/apache/maven/plugins/maven-clean-plugin/3.2.0/maven-clean-plugin-3.2.0.pom"
    "org/apache/maven/plugins/maven-compiler-plugin/3.11.0/maven-compiler-plugin-3.11.0.pom"
    # ... add more artifacts as needed
  )

  for artifact in "${artifacts[@]}"; do
    local local_path="$HOME/.m2/repository/$artifact"
    mkdir -p "$(dirname "$local_path")"

    # Download via proxy using Bearer token
    curl -s -x "21.0.0.195:15004" \
      -H "Proxy-Authorization: Bearer $JWT_TOKEN" \
      "https://repo.maven.apache.org/maven2/${artifact}" \
      -o "$local_path"
  done
}

# Run Maven
download_maven_artifacts
mvn clean compile
```

### Approach 2: Using Maven Settings with Proxy Credentials

Create a `~/.m2/settings.xml` file with proxy configuration:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <proxies>
    <proxy>
      <id>http-proxy</id>
      <active>true</active>
      <protocol>http</protocol>
      <host>21.0.0.195</host>
      <port>15004</port>
      <username>container_container_01Wdf2eSEdoMK4rKyRXiBVMw--claude_code_remote--shrill-woeful-flawe</username>
      <password>jwt_eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiIsImtpZCI6Iks3dlRfYUVsdXIySGdsYVJ0QWJ0UThDWDU4dFFqODZIRjJlX1VsSzZkNEEifQ.eyJpc3MiOiJhbnRocm9waWMtZWdyZXNzLWNvbnRyb2wiLCJvcmdhbml6YXRpb25fdXVpZCI6IjZiM2RhYzQwLTJjYTYtNDJhZS05Mzc2LWE4OTJmNDYwZjc4YSIsImlhdCI6MTc2OTQ1MTkwNiwiZXhwIjoxNzY5NDY2MzA2LCJhbGxvd2VkX2hvc3RzIjoiKiIsImlzX2hpcGFhX3JlZ3VsYXRlZCI6ImZhbHNlIiwiaXNfYW50X2hpcGkiOiJmYWxzZSIsInVzZV9lZ3Jlc3NfZ2F0ZXdheSI6ImZhbHNlIiwic2Vzc2lvbl9pZCI6InNlc3Npb25fMDE3aEF4QnlSQWVTU1Bid0R5MWZKNjRUIiwiY29udGFpbmVyX2lkIjoiY29udGFpbmVyXzAxV2RmMmVTRWRvTUs0ckt5UlhpQlZNdy1jbGF1ZGVfY29kZV9yZW1vdGUtc2hyaWxsLXdvZWZ1bC1mbGF3ZSJ9.ZtZf6KLMN9AQd2ThWNWl0imUhHysvMCsjC-2B8FSKs1GysbI_8j2Ai0KfZ0aAG3kqYhjXRMqVKqQQodg84cxjw</password>
    </proxy>
    <proxy>
      <id>https-proxy</id>
      <active>true</active>
      <protocol>https</protocol>
      <host>21.0.0.195</host>
      <port>15004</port>
      <username>container_container_01Wdf2eSEdoMK4rKyRXiBVMw--claude_code_remote--shrill-woeful-flawe</username>
      <password>jwt_eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiIsImtpZCI6Iks3dlRfYUVsdXIySGdsYVJ0QWJ0UThDWDU4dFFqODZIRjJlX1VsSzZkNEEifQ.eyJpc3MiOiJhbnRocm9waWMtZWdyZXNzLWNvbnRyb2wiLCJvcmdhbml6YXRpb25fdXVpZCI6IjZiM2RhYzQwLTJjYTYtNDJhZS05Mzc2LWE4OTJmNDYwZjc4YSIsImlhdCI6MTc2OTQ1MTkwNiwiZXhwIjoxNzY5NDY2MzA2LCJhbGxvd2VkX2hvc3RzIjoiKiIsImlzX2hpcGFhX3JlZ3VsYXRlZCI6ImZhbHNlIiwiaXNfYW50X2hpcGkiOiJmYWxzZSIsInVzZV9lZ3Jlc3NfZ2F0ZXdheSI6ImZhbHNlIiwic2Vzc2lvbl9pZCI6InNlc3Npb25fMDE3aEF4QnlSQWVTU1Bid0R5MWZKNjRUIiwiY29udGFpbmVyX2lkIjoiY29udGFpbmVyXzAxV2RmMmVTRWRvTUs0ckt5UlhpQlZNdy1jbGF1ZGVfY29kZV9yZW1vdGUtc2hyaWxsLXdvZWZ1bC1mbGF3ZSJ9.ZtZf6KLMN9AQd2ThWNWl0imUhHysvMCsjC-2B8FSKs1GysbI_8j2Ai0KfZ0aAG3kqYhjXRMqVKqQQodg84cxjw</password>
    </proxy>
  </proxies>
</settings>
```

**Note**: This approach works for HTTP requests but may fail for HTTPS CONNECT tunneling with Bearer token authentication. Basic authentication is used instead.

## Testing the Setup

### Using curl to verify proxy works:

```bash
# Extract JWT token
PROXY_URL="$HTTP_PROXY"
PROXY_URL="${PROXY_URL#http://}"
JWT_TOKEN="${PROXY_URL#*:}"
JWT_TOKEN="${JWT_TOKEN%@*}"
JWT_TOKEN="${JWT_TOKEN#jwt_}"

# Test download
curl -s -x "21.0.0.195:15004" \
  -H "Proxy-Authorization: Bearer $JWT_TOKEN" \
  "https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-clean-plugin/3.2.0/maven-clean-plugin-3.2.0.pom" \
  -o /tmp/test.pom

if [ -s /tmp/test.pom ]; then
  echo "✓ Proxy authentication successful!"
else
  echo "✗ Proxy authentication failed"
fi
```

### Running Maven:

Once proxy is configured (via settings.xml or pre-downloaded dependencies), run:

```bash
mvn clean compile
```

## Key Insights

1. **Bearer Token Authentication**: The proxy uses JWT Bearer token authentication, not standard HTTP Basic auth
2. **curl vs. Java**: curl handles Bearer tokens correctly; Java's built-in HTTP clients require different configuration
3. **Artifact Caching**: Maven aggressively caches downloaded artifacts, so pre-downloading key files eliminates future network requests
4. **HTTPS CONNECT Limitation**: The CONNECT method used for HTTPS tunneling may not properly forward Bearer tokens in the proxy request headers

## Environment Variables

The following environment variables should be set automatically in the build environment:

- `HTTP_PROXY`: HTTP proxy URL with credentials
- `HTTPS_PROXY`: HTTPS proxy URL with credentials
- `http_proxy`: Lowercase variant (used by some tools)
- `https_proxy`: Lowercase variant (used by some tools)

Example value format:
```
http://username:jwt_TOKEN@proxy_host:proxy_port
```

## Troubleshooting

### 401 Unauthorized Errors

- **Cause**: Maven's Wagon HTTP client is not using Bearer token authentication correctly
- **Solution**: Pre-download artifacts using curl and let Maven use cached versions

### DNS Resolution Failures

- **Cause**: Java is not using the proxy for DNS lookups
- **Solution**: This is expected. Maven handles DNS through the proxy once connected. If DNS fails before proxy connection, use the settings.xml approach with explicit host/port

### Corrupted POM Files

- **Cause**: Proxy returned error response that was cached as a file
- **Solution**: Clear the Maven cache: `rm -rf ~/.m2/repository/` and retry

## References

- Maven Documentation: [Using a Proxy](https://maven.apache.org/guides/mini/guide-proxies.html)
- curl Documentation: [Proxy authentication](https://curl.se/libcurl/c/CURLOPT_PROXYAUTH.html)
