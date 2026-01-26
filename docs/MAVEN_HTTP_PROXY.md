# Maven HTTP Proxy Configuration

This document describes how to configure Maven to use HTTP proxies for bypassing network restrictions during the build process.

## Table of Contents

1. [Overview](#overview)
2. [Environment Variables](#environment-variables)
3. [GitHub Actions Configuration](#github-actions-configuration)
4. [Local Development](#local-development)
5. [Proxy URL Format](#proxy-url-format)
6. [Troubleshooting](#troubleshooting)

---

## Overview

Maven downloads dependencies from Maven Central (`https://repo.maven.apache.org/maven2/`) during the build process. In environments with network restrictions (corporate firewalls, air-gapped networks, etc.), you may need to configure a proxy server to reach Maven Central.

The build system supports HTTP proxy configuration through standard environment variables:

- `HTTP_PROXY` - Proxy for HTTP connections
- `HTTPS_PROXY` - Proxy for HTTPS connections
- `NO_PROXY` - Hosts that should bypass the proxy

These environment variables are automatically parsed and converted to Maven system properties during the build.

---

## Environment Variables

### HTTP_PROXY

Sets the proxy for HTTP connections.

```bash
export HTTP_PROXY=http://proxy.example.com:8080
```

With authentication:

```bash
export HTTP_PROXY=http://username:password@proxy.example.com:8080
```

### HTTPS_PROXY

Sets the proxy for HTTPS connections. This is typically what you need for Maven Central since it uses HTTPS.

```bash
export HTTPS_PROXY=http://proxy.example.com:8080
```

With authentication:

```bash
export HTTPS_PROXY=http://username:password@proxy.example.com:8080
```

### NO_PROXY

Comma-separated list of hosts that should bypass the proxy.

```bash
export NO_PROXY=localhost,127.0.0.1,*.internal.company.com
```

---

## GitHub Actions Configuration

The CI/CD pipeline in `.github/workflows/vercel.yml` automatically reads proxy environment variables and passes them to Maven.

### Using Repository Secrets

To configure a proxy in GitHub Actions, add the following secrets to your repository:

1. Go to **Settings** > **Secrets and variables** > **Actions**
2. Add the following secrets:
   - `HTTP_PROXY` (optional)
   - `HTTPS_PROXY` (typically required)
   - `NO_PROXY` (optional)

Then modify the workflow to expose the secrets as environment variables:

```yaml
- name: Build with Maven
  env:
    HTTP_PROXY: ${{ secrets.HTTP_PROXY }}
    HTTPS_PROXY: ${{ secrets.HTTPS_PROXY }}
    NO_PROXY: ${{ secrets.NO_PROXY }}
  run: |
    # ... (proxy parsing and Maven execution)
```

### How It Works

The workflow parses the proxy environment variables and converts them to Maven system properties:

| Environment Variable | Maven System Properties |
|---------------------|------------------------|
| `HTTP_PROXY=http://host:port` | `-Dhttp.proxyHost=host -Dhttp.proxyPort=port` |
| `HTTP_PROXY=http://user:pass@host:port` | `-Dhttp.proxyHost=host -Dhttp.proxyPort=port -Dhttp.proxyUser=user -Dhttp.proxyPassword=pass` |
| `HTTPS_PROXY=http://host:port` | `-Dhttps.proxyHost=host -Dhttps.proxyPort=port` |
| `HTTPS_PROXY=http://user:pass@host:port` | `-Dhttps.proxyHost=host -Dhttps.proxyPort=port -Dhttps.proxyUser=user -Dhttps.proxyPassword=pass` |
| `NO_PROXY=host1,host2` | `-Dhttp.nonProxyHosts=host1\|host2` |

---

## Local Development

### Option 1: Environment Variables (Recommended)

Set the environment variables before running Maven:

```bash
export HTTPS_PROXY=http://proxy.example.com:8080
mvn clean package
```

Or inline with the command:

```bash
HTTPS_PROXY=http://proxy.example.com:8080 mvn clean package
```

### Option 2: Maven Settings File

Create or edit `~/.m2/settings.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              https://maven.apache.org/xsd/settings-1.0.0.xsd">

  <proxies>
    <proxy>
      <id>http-proxy</id>
      <active>true</active>
      <protocol>http</protocol>
      <host>proxy.example.com</host>
      <port>8080</port>
      <!-- Optional authentication -->
      <username>proxyuser</username>
      <password>proxypass</password>
      <!-- Optional non-proxy hosts -->
      <nonProxyHosts>localhost|127.0.0.1|*.internal.company.com</nonProxyHosts>
    </proxy>
    <proxy>
      <id>https-proxy</id>
      <active>true</active>
      <protocol>https</protocol>
      <host>proxy.example.com</host>
      <port>8080</port>
      <!-- Optional authentication -->
      <username>proxyuser</username>
      <password>proxypass</password>
      <nonProxyHosts>localhost|127.0.0.1|*.internal.company.com</nonProxyHosts>
    </proxy>
  </proxies>

</settings>
```

### Option 3: Command Line Properties

Pass proxy settings directly to Maven:

```bash
mvn clean package \
  -Dhttps.proxyHost=proxy.example.com \
  -Dhttps.proxyPort=8080
```

With authentication:

```bash
mvn clean package \
  -Dhttps.proxyHost=proxy.example.com \
  -Dhttps.proxyPort=8080 \
  -Dhttps.proxyUser=username \
  -Dhttps.proxyPassword=password
```

---

## Proxy URL Format

The environment variables accept URLs in the following formats:

### Basic Format

```
http://host:port
https://host:port
```

Examples:

```
http://proxy.company.com:8080
http://10.0.0.1:3128
```

### With Authentication

```
http://username:password@host:port
```

Examples:

```
http://john:secret123@proxy.company.com:8080
http://john:pass%40word@proxy.company.com:8080  # URL-encoded @ in password
```

### Protocol Notes

- Even for `HTTPS_PROXY`, the proxy URL itself typically uses `http://` (the proxy connection is HTTP, but it tunnels HTTPS traffic)
- The `https://` protocol in the proxy URL is less common but supported

---

## Troubleshooting

### Common Issues

**1. Connection Timeout**

```
[ERROR] Could not transfer artifact ... Connection timed out
```

- Verify proxy host and port are correct
- Check if proxy requires authentication
- Ensure proxy allows connections to `repo.maven.apache.org`

**2. Authentication Failed**

```
[ERROR] Could not transfer artifact ... status code 407 Proxy Authentication Required
```

- Add username and password to the proxy URL
- URL-encode special characters in password (e.g., `@` becomes `%40`)

**3. Certificate Errors**

```
[ERROR] PKIX path building failed: unable to find valid certification path
```

- Corporate proxies may perform SSL inspection
- Import the corporate CA certificate into Java's truststore:
  ```bash
  keytool -import -trustcacerts -file corporate-ca.crt \
    -alias corporate-ca -keystore $JAVA_HOME/lib/security/cacerts
  ```

**4. Proxy Not Used**

- Verify environment variables are exported (use `env | grep -i proxy`)
- Check for typos in variable names (case-sensitive on Linux)
- Ensure `NO_PROXY` doesn't include Maven Central hosts

### Debugging

Enable Maven debug output to see proxy configuration:

```bash
mvn -X clean package 2>&1 | grep -i proxy
```

Check current proxy environment:

```bash
echo "HTTP_PROXY: $HTTP_PROXY"
echo "HTTPS_PROXY: $HTTPS_PROXY"
echo "NO_PROXY: $NO_PROXY"
```

### Verifying Proxy Works

Test proxy connectivity before running Maven:

```bash
# Using curl
curl -x http://proxy.example.com:8080 https://repo.maven.apache.org/maven2/

# Using wget
https_proxy=http://proxy.example.com:8080 wget -q -O - https://repo.maven.apache.org/maven2/
```

---

## Summary

| Method | Use Case |
|--------|----------|
| Environment Variables | CI/CD, temporary settings, shell sessions |
| `~/.m2/settings.xml` | Local development, persistent settings |
| Command Line | One-off builds, testing different proxies |

For CI/CD pipelines (GitHub Actions), use environment variables through repository secrets. For local development, either environment variables or `~/.m2/settings.xml` work well depending on whether you need persistent or temporary proxy configuration.
