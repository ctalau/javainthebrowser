# Maven Proxy Setup for Restricted Environments

## Overview

This project includes a Maven proxy solution for building in restricted environments where:
- Direct access to Maven Central is blocked
- All HTTPS traffic must go through an authenticated proxy
- The proxy requires JWT bearer token authentication

**For detailed architecture documentation**, see [ARCHITECTURE.md](ARCHITECTURE.md).

## Running Maven Commands

Instead of running `mvn` directly, use the `run-mvn.js` wrapper script:

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
1. Starts a local proxy server (`maven-proxy.js`) on port 8080
2. Waits for the proxy to be ready
3. Runs your Maven command
4. Automatically stops the proxy when Maven finishes

The proxy server:
- Accepts HTTP requests from Maven on localhost:8080
- Forwards them to Maven Central via HTTPS with HTTP/2
- Authenticates with the upstream proxy using credentials from `HTTPS_PROXY` environment variable
- Uses HTTP CONNECT tunneling and ALPN protocol negotiation

## Configuration

Maven is configured via `~/.m2/settings.xml` to use the local proxy:

```xml
<mirror>
  <id>local-maven-proxy</id>
  <url>http://localhost:8080</url>
  <mirrorOf>central</mirrorOf>
</mirror>
```

## Manual Proxy Usage

If you need to run the proxy manually (for debugging or multiple Maven commands):

```bash
# Start the proxy
node maven-proxy.js &

# Run Maven commands
mvn clean install
mvn package

# Stop the proxy
pkill -f maven-proxy.js
```

## Requirements

- Node.js (for running the proxy server)
- Maven 3.x
- HTTPS_PROXY environment variable must be set with authentication credentials

## Troubleshooting

**Proxy won't start - port 8080 in use**
```bash
# Kill any existing proxy processes
pkill -f maven-proxy.js
```

**Maven can't download dependencies**
- Check that `~/.m2/settings.xml` has the mirror configuration
- Verify HTTPS_PROXY environment variable is set: `echo $HTTPS_PROXY`
- Check proxy logs for connection errors

**Build fails after downloading dependencies**
- The proxy is working correctly if you see "Downloaded from local-maven-proxy"
- Build failures after dependency download are application issues, not proxy issues
