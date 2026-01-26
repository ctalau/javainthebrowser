#!/usr/bin/env node

const http = require('http');
const https = require('https');
const http2 = require('http2');
const tls = require('tls');
const url = require('url');

const MAVEN_CENTRAL_URL = 'https://repo.maven.apache.org/maven2';
const LISTEN_PORT = 8080;

/**
 * Parse HTTPS_PROXY to get proxy connection details
 * Expected format: http://username:jwt_<token>@host:port
 */
function getProxyInfo() {
  const proxyUrl = process.env.HTTPS_PROXY || process.env.https_proxy;
  if (!proxyUrl) {
    return null;
  }

  const parsed = new url.URL(proxyUrl);

  // Build Basic auth header from username and password
  const auth = parsed.username && parsed.password
    ? Buffer.from(`${parsed.username}:${parsed.password}`).toString('base64')
    : null;

  return {
    host: parsed.hostname,
    port: parsed.port || 15004,
    auth: auth,
    username: parsed.username,
    password: parsed.password
  };
}

const proxyInfo = getProxyInfo();

/**
 * Forward request through HTTPS_PROXY using HTTP CONNECT tunneling
 */
function forwardThroughProxy(requestUrl, callback, errorCallback) {
  const parsedUrl = new url.URL(requestUrl);

  if (!proxyInfo || !proxyInfo.auth) {
    // If no proxy configured, connect directly
    console.log(`Making direct request to ${requestUrl}`);
    const req = https.get(requestUrl, callback);
    if (errorCallback) {
      req.on('error', errorCallback);
    }
    return req;
  }

  console.log(`Forwarding request through proxy ${proxyInfo.host}:${proxyInfo.port}`);

  // Step 1: Establish HTTP CONNECT tunnel to target server
  const connectOptions = {
    host: proxyInfo.host,
    port: proxyInfo.port,
    method: 'CONNECT',
    path: `${parsedUrl.hostname}:${parsedUrl.port || 443}`,
    headers: {
      'Host': `${parsedUrl.hostname}:${parsedUrl.port || 443}`,
      'Proxy-Authorization': `Basic ${proxyInfo.auth}`,
      'Proxy-Connection': 'Keep-Alive',
      'User-Agent': 'Maven-Proxy/1.0'
    }
  };

  const connectReq = http.request(connectOptions);

  connectReq.on('connect', (res, socket, head) => {
    if (res.statusCode !== 200) {
      const error = new Error(`Failed to establish tunnel: ${res.statusCode} ${res.statusMessage}`);
      if (errorCallback) {
        errorCallback(error);
      }
      socket.destroy();
      return;
    }

    console.log(`Tunnel established to ${parsedUrl.hostname}:${parsedUrl.port || 443}`);

    // Step 2: Wrap socket with TLS and enable ALPN for HTTP/2
    const tlsSocket = tls.connect({
      socket: socket,
      servername: parsedUrl.hostname,
      ALPNProtocols: ['h2', 'http/1.1']
    });

    tlsSocket.on('secureConnect', () => {
      const protocol = tlsSocket.alpnProtocol;
      console.log(`TLS handshake complete, negotiated protocol: ${protocol || 'http/1.1'}`);

      const requestPath = parsedUrl.pathname + parsedUrl.search;
      console.log(`Making request: GET ${requestPath}`);

      // Use HTTP/2 if negotiated, otherwise fall back to HTTP/1.1
      if (protocol === 'h2') {
        // HTTP/2 request
        const client = http2.connect(`https://${parsedUrl.hostname}`, {
          createConnection: () => tlsSocket
        });

        const req = client.request({
          ':method': 'GET',
          ':path': requestPath,
          'user-agent': 'Maven-Proxy/1.0'
        });

        req.on('response', (headers) => {
          const statusCode = headers[':status'];
          console.log(`Response status: ${statusCode}`);

          // Convert HTTP/2 headers to HTTP/1.1 format for the client
          const responseHeaders = {};
          for (const [key, value] of Object.entries(headers)) {
            if (!key.startsWith(':')) {
              responseHeaders[key] = value;
            }
          }

          // Create a mock response object for callback
          const mockResponse = {
            statusCode: statusCode,
            headers: responseHeaders,
            pipe: (dest) => req.pipe(dest),
            on: (event, handler) => req.on(event, handler)
          };

          callback(mockResponse);
        });

        req.on('error', (err) => {
          console.error(`HTTP/2 request error: ${err.message}`);
          if (errorCallback) {
            errorCallback(err);
          }
          client.close();
        });

        req.end();
      } else {
        // HTTP/1.1 fallback
        const httpsOptions = {
          host: parsedUrl.hostname,
          port: parsedUrl.port || 443,
          path: requestPath,
          method: 'GET',
          headers: {
            'Host': parsedUrl.hostname,
            'User-Agent': 'Maven-Proxy/1.0',
            'Accept': '*/*'
          },
          socket: tlsSocket,
          createConnection: () => tlsSocket
        };

        const httpsReq = https.request(httpsOptions, callback);

        if (errorCallback) {
          httpsReq.on('error', errorCallback);
        }

        httpsReq.end();
      }
    });

    tlsSocket.on('error', (err) => {
      console.error(`TLS error: ${err.message}`);
      if (errorCallback) {
        errorCallback(err);
      }
    });
  });

  connectReq.on('error', (err) => {
    console.error(`CONNECT error: ${err.message}`);
    if (errorCallback) {
      errorCallback(err);
    }
  });

  connectReq.end();

  return connectReq;
}

/**
 * Create HTTP server that proxies to Maven Central
 */
const server = http.createServer((req, res) => {
  const mavenUrl = `${MAVEN_CENTRAL_URL}${req.url}`;

  console.log(`${req.method} ${req.url}`);

  forwardThroughProxy(
    mavenUrl,
    (proxyRes) => {
      console.log(`Response status: ${proxyRes.statusCode}`);

      // Copy response headers
      Object.keys(proxyRes.headers).forEach(key => {
        res.setHeader(key, proxyRes.headers[key]);
      });

      res.writeHead(proxyRes.statusCode);
      proxyRes.pipe(res);
    },
    (err) => {
      console.error(`Error: ${err.message}`);
      res.writeHead(502, { 'Content-Type': 'text/plain' });
      res.end(`Bad Gateway: ${err.message}`);
    }
  );
});

server.listen(LISTEN_PORT, () => {
  console.log(`Maven proxy server listening on http://localhost:${LISTEN_PORT}`);
  console.log(`Proxying to: ${MAVEN_CENTRAL_URL}`);
  if (proxyInfo && proxyInfo.auth) {
    console.log(`Using upstream proxy: ${proxyInfo.host}:${proxyInfo.port}`);
    console.log(`Authentication: Basic (username=${proxyInfo.username})`);
  } else {
    console.log('No upstream proxy configured (direct mode)');
  }
});
