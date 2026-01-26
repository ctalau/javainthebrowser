#!/usr/bin/env node

/**
 * Test script to replicate curl's proxy behavior for HTTPS requests
 * This demonstrates how to use HTTP CONNECT tunneling with proxy authentication
 */

const http = require('http');
const https = require('https');
const { URL } = require('url');

// Parse proxy from environment variable
const proxyUrl = process.env.HTTPS_PROXY || process.env.https_proxy;
if (!proxyUrl) {
  console.error('Error: HTTPS_PROXY environment variable not set');
  process.exit(1);
}

const proxy = new URL(proxyUrl);
const targetUrl = process.argv[2] || 'https://repo.maven.apache.org/maven2/';
const target = new URL(targetUrl);

console.log('Proxy Configuration:');
console.log('  Host:', proxy.hostname);
console.log('  Port:', proxy.port);
console.log('  Username:', proxy.username);
console.log('  Password prefix:', proxy.password.substring(0, 10) + '...');
console.log();
console.log('Target URL:', targetUrl);
console.log('  Host:', target.hostname);
console.log('  Port:', target.port || 443);
console.log();

// Create the Proxy-Authorization header (Basic auth)
const auth = Buffer.from(`${proxy.username}:${proxy.password}`).toString('base64');
const proxyAuthHeader = `Basic ${auth}`;

console.log('Establishing CONNECT tunnel...');

// Step 1: Send CONNECT request to proxy
const connectOptions = {
  host: proxy.hostname,
  port: proxy.port,
  method: 'CONNECT',
  path: `${target.hostname}:${target.port || 443}`,
  headers: {
    'Host': `${target.hostname}:${target.port || 443}`,
    'Proxy-Authorization': proxyAuthHeader,
    'Proxy-Connection': 'Keep-Alive',
    'User-Agent': 'Node.js-Test/1.0'
  }
};

const connectReq = http.request(connectOptions);

connectReq.on('connect', (res, socket, head) => {
  console.log('CONNECT Response:', res.statusCode, res.statusMessage);
  console.log('Response Headers:', res.headers);
  console.log();

  if (res.statusCode !== 200) {
    console.error('Failed to establish tunnel');
    socket.destroy();
    process.exit(1);
  }

  console.log('Tunnel established successfully!');
  console.log('Performing TLS handshake...');
  console.log();

  // Step 2: Perform TLS handshake through the tunnel
  const tlsOptions = {
    socket: socket,
    servername: target.hostname,
  };

  const secureSocket = https.request({
    ...tlsOptions,
    host: target.hostname,
    port: target.port || 443,
    path: target.pathname + target.search,
    method: 'GET',
    headers: {
      'Host': target.hostname,
      'User-Agent': 'Node.js-Test/1.0',
      'Accept': '*/*'
    },
    createConnection: () => socket
  });

  secureSocket.on('response', (response) => {
    console.log('HTTPS Response:', response.statusCode, response.statusMessage);
    console.log('Response Headers:', JSON.stringify(response.headers, null, 2));
    console.log();
    console.log('Response Body (first 500 chars):');
    console.log('---');

    let data = '';
    response.on('data', (chunk) => {
      data += chunk.toString();
      if (data.length < 500) {
        process.stdout.write(chunk);
      }
    });

    response.on('end', () => {
      if (data.length >= 500) {
        process.stdout.write('...\n');
      }
      console.log();
      console.log('---');
      console.log('Success! Received', data.length, 'bytes');
      process.exit(0);
    });
  });

  secureSocket.on('error', (err) => {
    console.error('HTTPS Request Error:', err.message);
    socket.destroy();
    process.exit(1);
  });

  secureSocket.end();
});

connectReq.on('error', (err) => {
  console.error('CONNECT Request Error:', err.message);
  process.exit(1);
});

connectReq.end();
