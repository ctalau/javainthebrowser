# Python Proxy with Bearer Token Authentication

## Overview

An attempt to create a Python-based HTTP proxy that intercepts requests and adds Bearer token authentication headers, allowing Maven to work transparently without understanding Bearer tokens.

## Implementation Attempts

### Attempt 1: Raw Socket Proxy (`proxy-with-auth.py`)
- Uses raw sockets for HTTP/HTTPS tunneling
- Implements bidirectional data forwarding for CONNECT tunneling
- **Status**: Complex, hangs on HTTPS CONNECT requests

### Attempt 2: Simplified Socket Proxy (`proxy-auth-simple.py`)
- Simplified version with select() for multiplexing
- Attempts to handle both HTTP and HTTPS
- **Status**: Socket timeouts on HTTPS CONNECT

### Attempt 3: HTTP Server Proxy (`proxy-http-server.py`)
- Uses Python's `http.server.BaseHTTPRequestHandler`
- Implements proper HTTP request/response handling
- Adds Bearer token to Proxy-Authorization header
- **Status**: Hangs on HTTPS CONNECT tunneling

## Why HTTPS CONNECT Tunneling is Difficult

When Maven connects to a repository via HTTPS through a proxy:

1. **CONNECT Request**: Client sends `CONNECT host:port HTTP/1.1` to proxy
2. **Proxy Authentication**: Must include `Proxy-Authorization: Bearer TOKEN` in CONNECT request
3. **Tunnel Establishment**: If successful, proxy forwards all subsequent data as raw bytes
4. **TLS Negotiation**: Client and server establish TLS through the tunnel

The challenge: Properly implementing the bidirectional forwarding layer without blocking, hanging, or losing data.

## Lessons Learned

### ✓ What Works
- curl's built-in HTTP proxy support with environment variables
- curl correctly handles Bearer token authentication
- curl can tunnel HTTPS through the proxy successfully
- Direct environment variable usage is simpler than custom proxies

### ✗ What Doesn't Work Well
- Simple Python socket proxies (complex buffering/synchronization)
- Python http.server for full HTTPS tunneling (missing features)
- Java's HttpURLConnection with custom authenticators (not configured easily)

### ✓ What's Recommended Instead
1. **Use curl for downloads** - It handles all authentication correctly
2. **Cache artifacts locally** - Maven's repository caching is very effective
3. **Run Maven offline** - `mvn -o` uses cached artifacts
4. **Simplicity wins** - The 3-step curl + Maven approach is more maintainable

## Alternative Python Proxy Solutions

If you want to build a production Python proxy, consider:

### Option 1: Use `mitmproxy`
```bash
mitmproxy --mode upstream:http://21.0.0.195:15004/ --listen-port 18888
# Then add custom script to inject Bearer token
```

### Option 2: Use `tinyproxy` with custom authentication
```
# Tinyproxy with custom authentication module
# Still requires custom development
```

### Option 3: Use `requests` library with custom proxy adapter
```python
import requests
from requests.adapters import HTTPAdapter, HTTPSAdapter
from requests.auth import HTTPProxyAuth

session = requests.Session()
proxy_auth = HTTPProxyAuth('user', 'jwt_token')
# Configure session with proxy and auth
```

## Conclusion

The Python proxy approach, while theoretically elegant, is complex to implement correctly. The **curl + Maven offline mode** approach is:
- ✓ Simpler (3 steps)
- ✓ More reliable (uses proven tools)
- ✓ Easier to debug (clear error messages)
- ✓ Faster (no tunnel overhead)
- ✓ More maintainable (no custom proxy code)

**Recommendation**: Use the documented curl + Maven offline approach instead of implementing a custom Python proxy.

## Code References

If you need to implement a proxy in Python, reference implementations are available in:
- `/home/user/javainthebrowser/scripts/proxy-with-auth.py` - Raw sockets approach
- `/home/user/javainthebrowser/scripts/proxy-auth-simple.py` - Simplified sockets
- `/home/user/javainthebrowser/scripts/proxy-http-server.py` - HTTP server approach

Each includes lessons learned and comments about the challenges encountered.
