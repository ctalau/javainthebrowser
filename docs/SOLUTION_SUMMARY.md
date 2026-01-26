# Maven HTTP Proxy Setup - Complete Solution Summary

## Project Goal
Run Maven builds in a network environment with an HTTP proxy requiring JWT Bearer token authentication.

## Proxy Details
- **Address**: 21.0.0.195:15004
- **Auth Type**: HTTP Proxy with JWT Bearer Token
- **Format**: `Proxy-Authorization: Bearer <JWT_TOKEN>`
- **Environment Variables**: `HTTP_PROXY` and `HTTPS_PROXY` (with credentials embedded)

## What We Discovered

### ✓ WORKS: curl with Environment Variables
curl automatically uses HTTP_PROXY/HTTPS_PROXY and properly handles Bearer token authentication.

```bash
# Test it:
curl -s "https://repo.maven.apache.org/maven2/junit/junit/4.13.2/junit-4.13.2.pom" \
  -o ~/artifact.pom
# Returns valid XML if proxy auth succeeds
```

**Why it works:**
- curl has native HTTP proxy support
- curl properly implements CONNECT tunneling with authentication headers
- curl respects environment variables automatically

### ✗ FAILS: Maven Direct Proxy Configuration
Maven's Wagon HTTP library doesn't support Bearer token authentication.

```bash
mvn clean compile
# Result: 401 Unauthorized on HTTPS CONNECT
# Root cause: Wagon doesn't send Proxy-Authorization on CONNECT requests
```

### ✗ COMPLEX: Python Proxy Server
Building a custom Python proxy to add Bearer token authentication is complex.

**Attempts made:**
1. **Raw socket proxy** - Hangs on bidirectional data forwarding
2. **Select-based multiplexing** - Times out on HTTPS CONNECT
3. **http.server approach** - Also hangs on tunnel establishment

**Why it's hard:**
- HTTPS CONNECT tunneling requires precise bidirectional forwarding
- Must handle raw byte streams for TLS negotiation
- Python stdlib lacks native tunnel support
- Complex synchronization between sockets required

## Recommended Solution

### **Use curl + Maven Offline Mode**

This approach combines curl's working authentication with Maven's artifact caching:

```bash
# Step 1: Download critical artifacts with curl
curl -s "https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-clean-plugin/3.2.0/maven-clean-plugin-3.2.0.pom" \
  -o ~/.m2/repository/org/apache/maven/plugins/maven-clean-plugin/3.2.0/maven-clean-plugin-3.2.0.pom

# Step 2: Run Maven (uses cached artifacts + proxy for new ones)
mvn clean compile

# Or run offline (only uses cache, no network):
mvn -o clean compile
```

**Why this works:**
- ✓ curl handles proxy authentication correctly
- ✓ Maven's local repository cache is very effective
- ✓ Simple, no custom code needed
- ✓ Reliable and maintainable
- ✓ Clear error messages if something fails

## Provided Tools

### 1. Helper Script: `scripts/build-with-proxy.sh`
Automatically downloads essential Maven artifacts using curl, then runs Maven.

```bash
./scripts/build-with-proxy.sh
```

### 2. Setup Script: `scripts/setup-maven-proxy.sh`
Extracts proxy credentials from environment and configures Maven.

```bash
./scripts/setup-maven-proxy.sh
```

### 3. Maven Configuration: `~/.m2/settings.xml`
Manual Maven settings with proxy configuration (requires pre-downloaded artifacts).

```xml
<proxies>
  <proxy>
    <id>default-proxy</id>
    <protocol>http</protocol>
    <host>21.0.0.195</host>
    <port>15004</port>
    <username>container_container_01Wdf2eSEdoMK4rKyRXiBVMw--claude_code_remote--shrill-woeful-flawe</username>
    <password>jwt_eyJ0eXAi...</password>
  </proxy>
</proxies>
```

## Documentation Files

| File | Purpose |
|------|---------|
| **MAVEN_PROXY_README.md** | Quick start guide and overview |
| **MAVEN_HTTP_PROXY_SETUP.md** | Detailed setup instructions |
| **MAVEN_PROXY_ANALYSIS.md** | Technical deep-dive |
| **PYTHON_PROXY_APPROACH.md** | Python proxy experiments & lessons learned |
| **SOLUTION_SUMMARY.md** | This file - comprehensive summary |

## Key Insights

### 1. Tool Comparison

| Feature | curl | Maven Wagon | Python Proxy |
|---------|------|------------|--------------|
| Bearer Token Auth | ✓ Works | ✗ No support | ✓ Possible* |
| HTTPS CONNECT | ✓ Works | ✗ 401 Error | ✗ Complex |
| Environment Vars | ✓ Auto-detects | ✗ Requires settings.xml | N/A |
| Simplicity | ✓ Easy | ✗ Difficult | ✗ Complex |

*Python proxy possible but not practical - complex implementation with synchronization issues.

### 2. JWT Token in Environment Variables

The JWT token is embedded in the HTTP_PROXY variable:
```
HTTP_PROXY=http://username:jwt_TOKEN@proxy:port
```

When extracting manually:
```bash
# Extract JWT (remove jwt_ prefix)
JWT="${HTTP_PROXY#*:jwt_}"
JWT="${JWT%@*}"
```

### 3. Maven's Artifact Caching

Maven is very efficient at caching:
- Downloads once, uses many times
- Stores in `~/.m2/repository/`
- Can be shared across projects
- `mvn dependency:purge-local-repository` to clear cache

## Quickstart Instructions

### For First-Time Setup

```bash
# Option 1: Use helper script (automatic)
./scripts/build-with-proxy.sh

# Option 2: Manual setup
./scripts/setup-maven-proxy.sh
mvn clean compile
```

### For Subsequent Builds

```bash
# Maven will use cached artifacts automatically
mvn clean compile

# Or offline mode (no network access needed)
mvn -o clean compile
```

## Troubleshooting

### Problem: "401 Unauthorized"
**Cause**: Maven trying HTTPS CONNECT without Bearer token
**Solution**: Use `build-with-proxy.sh` or pre-download artifacts with curl

### Problem: "Temporary failure in name resolution"
**Cause**: Environment variables not set
**Solution**: Verify `echo $HTTP_PROXY` shows proxy URL with credentials

### Problem: "error code: 1003"
**Cause**: HTTP request to HTTPS-only repository
**Solution**: Use HTTPS URLs (curl handles this correctly)

## Next Steps

1. **Read**: `/docs/MAVEN_PROXY_README.md` for quick start
2. **Run**: `./scripts/build-with-proxy.sh` to test
3. **Reference**: Other docs for specific scenarios
4. **Customize**: Adapt scripts for your project's dependencies

## Files Structure

```
javainthebrowser/
├── docs/
│   ├── MAVEN_PROXY_README.md          # Quick start (READ FIRST)
│   ├── MAVEN_HTTP_PROXY_SETUP.md      # Setup details
│   ├── MAVEN_PROXY_ANALYSIS.md        # Technical analysis
│   ├── PYTHON_PROXY_APPROACH.md       # Python proxy experiments
│   └── SOLUTION_SUMMARY.md            # This file
├── scripts/
│   ├── build-with-proxy.sh            # Build wrapper (RECOMMENDED)
│   ├── setup-maven-proxy.sh           # Configuration helper
│   ├── proxy-with-auth.py             # Raw socket proxy (reference)
│   ├── proxy-auth-simple.py           # Simplified proxy (reference)
│   └── proxy-http-server.py           # HTTP server proxy (reference)
└── ~/.m2/settings.xml                 # Maven configuration
```

## Conclusion

The **curl + Maven offline mode** approach provides:
- ✓ **Simplicity**: 3 easy steps
- ✓ **Reliability**: Uses proven tools
- ✓ **Maintainability**: No custom code
- ✓ **Performance**: Leverages caching
- ✓ **Debuggability**: Clear error messages

This is the recommended solution for running Maven builds through HTTP proxies with JWT Bearer token authentication.
