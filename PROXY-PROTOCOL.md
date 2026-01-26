# Maven Proxy with Bearer Token Authentication - Solution

## Problem Summary

Maven requires access to Maven Central (`repo.maven.apache.org`) but runs in a sandboxed environment where:
- All HTTPS traffic must go through a proxy at `21.0.0.79:15004`
- The proxy requires authentication with a JWT token
- The HTTPS_PROXY environment variable is: `http://username:jwt_TOKEN@21.0.0.79:15004`

## Solution

The solution uses a **local Node.js proxy server** that:
1. Listens on `http://localhost:8080`
2. Forwards requests to Maven Central through the upstream proxy
3. Uses **HTTP CONNECT tunneling** with **Basic authentication**
4. Negotiates **HTTP/2** with Maven Central (required for proper operation)

## Protocol Flow

### Step 1: HTTP CONNECT Tunnel
```
Client (Maven) -> localhost:8080 -> Upstream Proxy (21.0.0.79:15004) -> Maven Central
```

The proxy sends:
```http
CONNECT repo.maven.apache.org:443 HTTP/1.1
Host: repo.maven.apache.org:443
Proxy-Authorization: Basic <base64(username:jwt_TOKEN)>
Proxy-Connection: Keep-Alive
```

Upstream proxy responds:
```http
HTTP/1.1 200 OK
date: Mon, 26 Jan 2026 22:22:19 GMT
server: envoy
```

### Step 2: TLS Handshake with ALPN
After tunnel establishment, the proxy:
1. Wraps the socket with TLS
2. Sets `servername` for SNI (Server Name Indication)
3. Negotiates protocol using ALPN: `['h2', 'http/1.1']`
4. Maven Central accepts `h2` (HTTP/2)

### Step 3: HTTP/2 Request
The proxy makes HTTP/2 requests through the TLS tunnel:
```http
:method: GET
:path: /maven2/org/apache/maven/...
user-agent: Maven-Proxy/1.0
```

## Key Technical Details

### Authentication
- **Method**: Basic authentication (NOT Bearer)
- **Header**: `Proxy-Authorization: Basic base64(username:password)`
- **Username**: Container ID from environment
- **Password**: Full JWT token WITH `jwt_` prefix

### HTTP/2 Requirement
Maven Central returns 301 redirects to an error page for HTTP/1.1 requests. **HTTP/2 is required** for successful requests.

### ALPN (Application-Layer Protocol Negotiation)
The TLS handshake must include ALPN with `['h2', 'http/1.1']` to negotiate HTTP/2.

## Files

### maven-proxy.js
Local proxy server that handles the protocol translation:
- Listens on port 8080
- Establishes HTTP CONNECT tunnels
- Performs TLS handshake with ALPN
- Makes HTTP/2 requests to Maven Central

### run-mvn.js
Wrapper script that:
- Starts maven-proxy.js in background
- Waits for proxy to be ready
- Runs Maven with provided arguments
- Stops proxy when done

### ~/.m2/settings.xml
Maven configuration that points to the local proxy:
```xml
<mirror>
  <id>local-maven-proxy</id>
  <url>http://localhost:8080</url>
  <mirrorOf>central</mirrorOf>
</mirror>
```

## Usage

```bash
# Run Maven through the proxy
node run-mvn.js clean install

# Or start proxy manually
node maven-proxy.js &
mvn clean install
```

## Testing Results

✓ curl with HTTPS_PROXY works directly
✓ Node.js can replicate curl's behavior
✓ maven-proxy.js serves requests via HTTP/2
✓ Maven successfully downloads dependencies
✓ run-mvn.js automates the entire process

## Why Previous Attempts Failed

1. **Attempt 1**: Used `Proxy-Authorization: Bearer` instead of `Basic` ❌
2. **Attempt 2**: Made direct HTTPS requests without CONNECT tunnel ❌
3. **Attempt 3**: Used HTTP/1.1 instead of HTTP/2 ❌
4. **Attempt 4**: Missing ALPN negotiation ❌
5. **Attempt 5**: Missing SNI (servername) ❌

## Final Solution

The working solution combines:
- HTTP CONNECT tunneling ✓
- Basic authentication with JWT ✓
- TLS with SNI ✓
- ALPN protocol negotiation ✓
- HTTP/2 support ✓
