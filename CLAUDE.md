# Maven Proxy Setup for Restricted Environments

## Running Maven Commands

Running Maven in Claude web sandbox requires that instead of running `mvn` directly, use the `run-mvn.js` wrapper script:

```bash
# Standard Maven commands
node run-mvn.js clean install
node run-mvn.js package
node run-mvn.js test
node run-mvn.js clean compile

# Any Maven goal or combination works
node run-mvn.js dependency:tree
node run-mvn.js clean package -DskipTests
```

## How It Works

The `run-mvn.js` script:
1. Ensures Maven settings.xml exists with proxy configuration
2. Starts an integrated local proxy server on port 8080
3. Waits for the proxy to be ready
4. Runs your Maven command
5. Automatically stops the proxy when Maven finishes

The integrated proxy server:
- Accepts HTTP requests from Maven on localhost:8080
- Forwards them to Maven Central via HTTPS with HTTP/2
- Authenticates with the upstream proxy using credentials from `HTTPS_PROXY` environment variable
- Uses HTTP CONNECT tunneling and ALPN protocol negotiation

**For detailed architecture documentation**, see the @fileoverview JSDoc comment at the top of `run-mvn.js`.

## Configuration

Maven is configured via `~/.m2/settings.xml` to use the local proxy:

```xml
<mirror>
  <id>local-maven-proxy</id>
  <url>http://localhost:8080</url>
  <mirrorOf>central</mirrorOf>
</mirror>
```

## Architecture

The script creates a three-layer proxy chain:

```
Maven -> Local Proxy -> Upstream Sandbox Proxy -> Maven Central
       (HTTP)        (HTTP CONNECT)         (HTTP/2 over TLS)
```

Each layer serves a specific purpose:
- **Layer 1**: Maven → Local Proxy (HTTP) - Maven doesn't support JWT auth
- **Layer 2**: Local Proxy → Upstream Proxy (HTTP CONNECT) - Creates authenticated tunnel
- **Layer 3**: Tunnel → Maven Central (HTTP/2 over TLS) - Required by Maven Central

## Requirements

- Node.js (for running the proxy server)
- Maven 3.x
- HTTPS_PROXY environment variable must be set with authentication credentials

## Troubleshooting

**Proxy won't start - port 8080 in use**
```bash
# Kill any processes using port 8080
lsof -ti:8080 | xargs kill -9
```

**Maven can't download dependencies**
- Check that `~/.m2/settings.xml` has the mirror configuration
- Verify HTTPS_PROXY environment variable is set: `echo $HTTPS_PROXY`
- Check proxy logs for connection errors

**Build fails after downloading dependencies**
- The proxy is working correctly if you see "Downloaded from local-maven-proxy"
- Build failures after dependency download are application issues, not proxy issues
