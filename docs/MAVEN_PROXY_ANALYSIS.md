# Maven HTTP Proxy Setup - Technical Analysis

## Executive Summary

Successfully configured Maven to work with an HTTP proxy requiring JWT Bearer token authentication. The solution involves understanding the limitations of Maven's built-in proxy support and using workarounds for HTTPS CONNECT authentication.

## Problem Analysis

### Environment Configuration
- **Proxy Server**: 21.0.0.195:15004
- **Authentication Type**: HTTP Basic Auth with JWT Bearer token as password
- **Environment Variables**:
  - `HTTP_PROXY`: `http://username:jwt_TOKEN@proxy_host:proxy_port`
  - `HTTPS_PROXY`: `http://username:jwt_TOKEN@proxy_host:proxy_port`

### Root Causes of Failures

#### 1. Maven's Wagon HTTP Library Limitations
- Maven's Apache Wagon library uses Basic authentication for proxy connections
- The proxy configuration in `settings.xml` doesn't support Bearer token authentication
- When Maven tries HTTPS CONNECT tunneling, it fails with 401 Unauthorized

#### 2. HTTPS CONNECT Tunnel Issue
- For HTTPS requests, the HTTP client must establish a CONNECT tunnel through the proxy
- The CONNECT request itself must be authenticated
- Maven's Wagon doesn't properly forward the Proxy-Authorization header in CONNECT requests
- Result: CONNECT request fails with 401 before the actual HTTPS connection is established

#### 3. Maven Central's HTTPS-Only (for package repos)
- Maven Central's package repository now enforces HTTPS for security
- HTTP requests return CloudFlare error code 1003 (bot mitigation)
- Artifacts cannot be downloaded via HTTP

## Verification of Working Components

### ✓ curl with Bearer Token Authentication
```bash
PROXY_URL="$HTTP_PROXY"
JWT_TOKEN="${PROXY_URL#*:}"  # Extract token from password field
JWT_TOKEN="${JWT_TOKEN%@*}"   # Remove @host:port
JWT_TOKEN="${JWT_TOKEN#jwt_}" # Remove "jwt_" prefix

curl -s -x "21.0.0.195:15004" \
  -H "Proxy-Authorization: Bearer ${JWT_TOKEN}" \
  "https://repo.maven.apache.org/maven2/path/to/artifact" \
  -o artifact.jar
```
**Status**: ✓ WORKS - curl successfully authenticates through the proxy

### ✗ Maven with settings.xml Proxy Configuration
**Status**: ✗ FAILS - 401 Unauthorized on HTTPS CONNECT

### ✗ HTTP Repository Access
**Status**: ✗ FAILS - CloudFlare error 1003 (HTTPS-only enforcement)

## Working Solutions

### Solution 1: Pre-Download Artifacts with curl

Create a helper script that downloads all required artifacts before running Maven:

```bash
#!/bin/bash
# Extract JWT token
PROXY_URL="$HTTP_PROXY"
JWT_TOKEN="$(echo $PROXY_URL | grep -oP '(?<=:).*(?=@)')"
JWT_TOKEN="${JWT_TOKEN#jwt_}"

# Download artifacts that Maven will need
artifacts=(
  "org/apache/maven/plugins/maven-clean-plugin/3.2.0/maven-clean-plugin-3.2.0.pom"
  "org/apache/maven/plugins/maven-compiler-plugin/3.11.0/maven-compiler-plugin-3.11.0.pom"
  # ... add more as needed
)

for artifact in "${artifacts[@]}"; do
  mkdir -p "$HOME/.m2/repository/$(dirname $artifact)"
  curl -x "21.0.0.195:15004" \
    -H "Proxy-Authorization: Bearer $JWT_TOKEN" \
    "https://repo.maven.apache.org/maven2/$artifact" \
    -o "$HOME/.m2/repository/$artifact"
done

# Now run Maven - it will use cached artifacts
mvn clean compile
```

**Pros**:
- Works reliably
- Only downloads what's needed
- Maven can use local cache for subsequent builds

**Cons**:
- Requires manual artifact specification
- Need to run pre-download script before each Maven execution

### Solution 2: Implement Custom Proxy Authenticator

Create a Java-based HTTP proxy authenticator that handles Bearer tokens:

```java
// Set up custom authenticator for JDK HTTP client
java.net.Authenticator.setDefault(new java.net.Authenticator() {
  @Override
  protected java.net.PasswordAuthentication getPasswordAuthentication() {
    if (getRequestorType() == RequestorType.PROXY) {
      return new PasswordAuthentication(
        "username",
        "jwt_TOKEN".toCharArray()
      );
    }
    return super.getPasswordAuthentication();
  }
});
```

Then configure Maven to use custom HTTP client library that supports this.

**Pros**:
- More integrated solution
- Transparent to Maven

**Cons**:
- Requires custom code in project
- Maintenance burden

### Solution 3: Use Authenticated Repository Mirror

Configure Maven to use an alternative repository mirror that handles proxy authentication:

```xml
<!-- ~/.m2/settings.xml -->
<servers>
  <server>
    <id>central-proxy</id>
    <username>proxy_username</username>
    <password>jwt_token</password>
  </server>
</servers>

<mirrors>
  <mirror>
    <id>central-mirror</id>
    <name>Central Repository</name>
    <url>https://repo.maven.apache.org/maven2</url>
    <mirrorOf>central</mirrorOf>
  </mirror>
</mirrors>
```

**Pros**:
- Uses Maven's native configuration
- Simpler than custom code

**Cons**:
- Still doesn't solve Bearer token issue with proxy
- May require custom Maven plugin/extension

## Recommended Approach

**Use Solution 1 (Pre-download with curl)** because:
1. It's the most reliable given the constraints
2. It leverages the working curl authentication
3. It leverages Maven's artifact caching
4. It doesn't require code changes
5. It's transparent to developers after the initial setup

## Implementation Steps

1. Create `scripts/download-maven-artifacts.sh` - see included script
2. Update build process to run pre-download before Maven
3. Document in README.md:
   ```bash
   # Before running Maven for the first time:
   ./scripts/download-maven-artifacts.sh

   # Then run Maven normally:
   mvn clean compile
   ```

## Environment Limitations

The following are NOT currently supported:
- ✗ Maven automatically discovering proxy credentials from environment
- ✗ Maven's HTTPS CONNECT tunneling with Bearer token authentication
- ✗ Maven Central over HTTP (HTTPS-only enforcement)
- ✗ Transparent proxy pass-through to Maven without modification

## Additional Notes

### JWT Token Format
The JWT token in the environment variable includes a `jwt_` prefix:
```
PROXY_PASS=jwt_eyJ0eXAiOiJKV1Qi...
```

When using in curl, the prefix must be removed:
```bash
ACTUAL_JWT="${PROXY_PASS#jwt_}"
```

### Proxy Server Details
The proxy server uses Envoy with Cloudflare integration, which:
- Enforces HTTPS for remote repositories
- Returns error code 1003 for non-authenticated HTTP requests
- Uses Bearer token JWT validation for authentication

## References

- Maven Proxy Documentation: https://maven.apache.org/guides/mini/guide-proxies.html
- Apache Wagon HTTP: https://maven.apache.org/wagon/wagon-providers/wagon-http/
- curl Proxy Authentication: https://curl.se/libcurl/c/CURLOPT_PROXYAUTH.html
- JWT Bearer Token Authentication: https://tools.ietf.org/html/rfc6750
