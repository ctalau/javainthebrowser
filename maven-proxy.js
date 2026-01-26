#!/usr/bin/env node

const http = require('http');
const https = require('https');
const url = require('url');

const MAVEN_CENTRAL_URL = 'https://repo.maven.apache.org/maven2';
const LISTEN_PORT = 8080;

/**
 * Extract bearer token from HTTPS_PROXY environment variable.
 * Expected format: https://user:jwt_<token>@host:port
 * Returns the token without the jwt_ prefix.
 */
function extractBearerToken() {
  const proxyUrl = process.env.HTTPS_PROXY;
  if (!proxyUrl) {
    console.log('Warning: HTTPS_PROXY not set, proxy forwarding disabled');
    return null;
  }

  const parsed = new url.URL(proxyUrl);
  const password = parsed.password;

  if (!password) {
    console.log('Warning: No password in HTTPS_PROXY');
    return null;
  }

  if (password.startsWith('jwt_')) {
    const token = password.slice(4); // Remove 'jwt_' prefix
    console.log('Extracted bearer token from HTTPS_PROXY');
    return token;
  }

  console.log('Warning: Password does not start with jwt_ prefix');
  return null;
}

/**
 * Parse HTTPS_PROXY to get proxy host and port
 */
function getProxyInfo() {
  const proxyUrl = process.env.HTTPS_PROXY;
  if (!proxyUrl) {
    return null;
  }

  const parsed = new url.URL(proxyUrl);
  return {
    host: parsed.hostname,
    port: parsed.port || 443
  };
}

const bearerToken = extractBearerToken();
const proxyInfo = getProxyInfo();

/**
 * Forward request through HTTPS_PROXY if configured
 */
function forwardThroughProxy(requestUrl, callback) {
  if (!proxyInfo || !bearerToken) {
    // If no proxy configured, connect directly
    console.log(`Making direct request to ${requestUrl}`);
    return https.get(requestUrl, callback);
  }

  console.log(`Forwarding request through proxy ${proxyInfo.host}:${proxyInfo.port}`);

  const parsedUrl = new url.URL(requestUrl);

  const options = {
    host: proxyInfo.host,
    port: proxyInfo.port,
    method: 'GET',
    headers: {
      'Host': parsedUrl.hostname,
      'Proxy-Authorization': `Bearer ${bearerToken}`,
      'User-Agent': 'Maven-Proxy/1.0'
    }
  };

  return https.request(requestUrl, options, callback);
}

/**
 * Create HTTP server that proxies to Maven Central
 */
const server = http.createServer((req, res) => {
  const mavenUrl = `${MAVEN_CENTRAL_URL}${req.url}`;

  console.log(`${req.method} ${req.url}`);

  forwardThroughProxy(mavenUrl, (proxyRes) => {
    console.log(`Response status: ${proxyRes.statusCode}`);

    // Copy response headers
    Object.keys(proxyRes.headers).forEach(key => {
      res.setHeader(key, proxyRes.headers[key]);
    });

    res.writeHead(proxyRes.statusCode);
    proxyRes.pipe(res);
  }).on('error', (err) => {
    console.error(`Error: ${err.message}`);
    res.writeHead(502, { 'Content-Type': 'text/plain' });
    res.end(`Bad Gateway: ${err.message}`);
  }).end();
});

server.listen(LISTEN_PORT, () => {
  console.log(`Maven proxy server listening on http://localhost:${LISTEN_PORT}`);
  console.log(`Proxying to: ${MAVEN_CENTRAL_URL}`);
  if (proxyInfo && bearerToken) {
    console.log(`Using upstream proxy: ${proxyInfo.host}:${proxyInfo.port}`);
    console.log('Using JWT bearer authentication');
  } else {
    console.log('No upstream proxy configured (direct mode)');
  }
});
