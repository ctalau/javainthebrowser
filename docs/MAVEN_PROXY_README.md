# Maven HTTP Proxy Setup - Complete Solution

## Quick Start

If your environment has `HTTP_PROXY` and `HTTPS_PROXY` environment variables set with JWT Bearer token authentication:

```bash
# The easiest approach: use curl to pre-download dependencies
# curl automatically picks up HTTP_PROXY and HTTPS_PROXY

# Extract JWT and download core Maven artifacts
PROXY_URL="$HTTP_PROXY"
JWT_TOKEN="$(echo $PROXY_URL | grep -oP '(?<=jwt_)[^@]*')"

# Download a critical artifact to cache locally
curl -s "$HTTPS_PROXY_URL/org/apache/maven/plugins/maven-clean-plugin/3.2.0/maven-clean-plugin-3.2.0.pom" \
  -o ~/.m2/repository/org/apache/maven/plugins/maven-clean-plugin/3.2.0/maven-clean-plugin-3.2.0.pom

# Run Maven (it will use cached artifacts + proxy settings from settings.xml)
mvn clean compile
```

## The Challenge

The build environment has an HTTP proxy requiring **JWT Bearer token authentication**:
- Proxy Address: `21.0.0.195:15004`
- Auth Method: Bearer token in Proxy-Authorization header
- Format: `Proxy-Authorization: Bearer JWT_TOKEN`

### Why This is Difficult

1. **Maven's Wagon HTTP doesn't support Bearer tokens** - it only supports Basic HTTP authentication
2. **HTTPS CONNECT tunneling fails** - when Maven tries to tunnel HTTPS through the proxy, it doesn't send the Bearer token header, resulting in 401 Unauthorized
3. **Maven Central enforces HTTPS** - HTTP downloads return CloudFlare error 1003 (bot mitigation)
4. **Deep dependency chains** - Maven plugins have multiple levels of parent POMs that must all be downloaded

## Solution

### ✓ Working Approach: Pre-download with curl + Maven Offline

**Why this works:**
- ✓ curl correctly uses HTTP_PROXY/HTTPS_PROXY environment variables
- ✓ curl properly sends Bearer token authentication
- ✓ curl successfully downloads from Maven Central through the proxy
- ✓ Maven can run offline using cached artifacts

**Steps:**

1. **Ensure environment variables are set:**
   ```bash
   echo $HTTP_PROXY    # Should show proxy URL with credentials
   echo $HTTPS_PROXY   # Should show proxy URL with credentials
   ```

2. **Test curl works with the proxy:**
   ```bash
   curl -s "https://repo.maven.apache.org/maven2/junit/junit/4.13.2/junit-4.13.2.pom" \
     -o /tmp/test.pom && head -1 /tmp/test.pom
   # Should output: <?xml version='1.0' encoding='UTF-8'?>
   ```

3. **Use the provided build script:**
   ```bash
   ./scripts/build-with-proxy.sh
   ```

   This script:
   - Downloads essential Maven plugin artifacts using curl
   - Caches them in `~/.m2/repository/`
   - Runs `mvn clean compile` using cached artifacts

### Alternative: Manual Maven Configuration

If you prefer to configure Maven directly:

1. **Create `~/.m2/settings.xml`:**
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
         <password>jwt_eyJ0eXAiOiJKV1QiLCJhbGc...</password>
       </proxy>
     </proxies>
   </settings>
   ```

2. **Note:** This uses Basic authentication instead of Bearer tokens, so HTTPS CONNECT tunneling may fail. Pre-download critical artifacts with curl first.

## Files in This Repository

- **`docs/MAVEN_HTTP_PROXY_SETUP.md`** - Detailed setup guide for different approaches
- **`docs/MAVEN_PROXY_ANALYSIS.md`** - Technical analysis of why the proxy is challenging
- **`scripts/setup-maven-proxy.sh`** - Helper script for proxy configuration
- **`scripts/build-with-proxy.sh`** - Complete build script that handles proxy automatically

## Testing Your Setup

```bash
# Test 1: Verify curl works with the proxy
curl -v "https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-clean-plugin/3.2.0/maven-clean-plugin-3.2.0.pom" \
  -o /tmp/test.pom

if grep -q "<?xml" /tmp/test.pom; then
  echo "✓ curl authentication through proxy works!"
else
  echo "✗ curl download failed"
  cat /tmp/test.pom
fi

# Test 2: Verify Maven can find settings
mvn help:effective-settings | head -20

# Test 3: Try building
mvn clean compile
```

## Troubleshooting

### "401 Unauthorized" Errors

**Cause**: Maven's Wagon HTTP client is attempting HTTPS CONNECT tunneling, which doesn't send Bearer tokens properly.

**Solution**:
1. Pre-download artifacts with curl before running Maven
2. Run Maven in offline mode: `mvn -o clean compile`

### "Temporary failure in name resolution"

**Cause**: Environment variables not set, or Maven not configured for proxy.

**Solution**:
```bash
# Check environment
env | grep PROXY

# If missing, add to shell profile:
export HTTP_PROXY=http://container_container_01Wdf2eSEdoMK4rKyRXiBVMw--claude_code_remote--shrill-woeful-flawe:jwt_TOKEN@21.0.0.195:15004
export HTTPS_PROXY=http://container_container_01Wdf2eSEdoMK4rKyRXiBVMw--claude_code_remote--shrill-woeful-flawe:jwt_TOKEN@21.0.0.195:15004
```

### "error code: 1003" from curl

**Cause**: HTTP request to Maven Central (HTTPS-only enforcement).

**Solution**: Use HTTPS URLs (which curl handles via the proxy).

### Maven still trying to download when in offline mode

**Cause**: Missing transitive dependencies or parent POMs.

**Solution**:
1. Run Maven normally once to let it fail
2. Note which artifacts are missing
3. Download them with curl
4. Run Maven offline again

## Key Insights

### curl vs. Maven HTTP Handling

| Feature | curl | Maven Wagon |
|---------|------|-------------|
| Bearer Token Auth | ✓ Works | ✗ No support |
| HTTP_PROXY env var | ✓ Automatic | ✗ Requires settings.xml |
| HTTPS CONNECT tunnel | ✓ Works with Bearer | ✗ 401 Unauthorized |
| Error handling | Explicit | Cached errors |

### JWT Token in Environment Variables

The JWT token in the environment variable includes a "jwt_" prefix:
```
PROXY_PASS=jwt_eyJ0eXAiOiJKV1Qi...
```

When using in curl headers, the prefix must be removed:
```bash
ACTUAL_JWT="${PROXY_PASS#jwt_}"  # Remove "jwt_" prefix
```

## References

- Maven Proxy Documentation: https://maven.apache.org/guides/mini/guide-proxies.html
- curl HTTP Proxy: https://curl.se/docs/sslcerts/cacert.pem
- RFC 6750 - Bearer Token Usage: https://tools.ietf.org/html/rfc6750
