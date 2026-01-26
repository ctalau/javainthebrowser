#!/usr/bin/env node

/**
 * Wrapper script to run Maven with the local proxy server
 *
 * This script:
 * 1. Starts the maven-proxy.js server in the background
 * 2. Waits for it to be ready
 * 3. Runs Maven with the provided arguments
 * 4. Stops the proxy server when done
 */

const { spawn } = require('child_process');
const http = require('http');
const path = require('path');

const PROXY_PORT = 8080;
const PROXY_SCRIPT = path.join(__dirname, 'maven-proxy.js');

let proxyProcess = null;

/**
 * Check if proxy server is ready
 */
function checkProxyReady() {
  return new Promise((resolve) => {
    const req = http.get(`http://localhost:${PROXY_PORT}/`, () => {
      resolve(true);
    });
    req.on('error', () => {
      resolve(false);
    });
    req.end();
  });
}

/**
 * Wait for proxy to be ready
 */
async function waitForProxy(maxAttempts = 30, intervalMs = 100) {
  for (let i = 0; i < maxAttempts; i++) {
    if (await checkProxyReady()) {
      return true;
    }
    await new Promise(resolve => setTimeout(resolve, intervalMs));
  }
  return false;
}

/**
 * Start the Maven proxy server
 */
async function startProxy() {
  console.log('Starting Maven proxy server...');

  proxyProcess = spawn('node', [PROXY_SCRIPT], {
    stdio: ['ignore', 'pipe', 'pipe'],
    detached: false
  });

  proxyProcess.stdout.on('data', (data) => {
    console.log(`[PROXY] ${data.toString().trim()}`);
  });

  proxyProcess.stderr.on('data', (data) => {
    console.error(`[PROXY ERROR] ${data.toString().trim()}`);
  });

  proxyProcess.on('exit', (code) => {
    if (code !== null && code !== 0) {
      console.error(`Proxy server exited with code ${code}`);
    }
  });

  // Wait for proxy to be ready
  console.log('Waiting for proxy to be ready...');
  const ready = await waitForProxy();
  if (!ready) {
    throw new Error('Proxy server failed to start');
  }
  console.log('Proxy server is ready!\n');
}

/**
 * Stop the proxy server
 */
function stopProxy() {
  if (proxyProcess) {
    console.log('\nStopping Maven proxy server...');
    proxyProcess.kill('SIGTERM');
    proxyProcess = null;
  }
}

/**
 * Run Maven with the provided arguments
 */
function runMaven(args) {
  return new Promise((resolve, reject) => {
    console.log(`Running: mvn ${args.join(' ')}\n`);

    const mvn = spawn('mvn', args, {
      stdio: 'inherit',
      env: process.env
    });

    mvn.on('exit', (code) => {
      if (code === 0) {
        resolve();
      } else {
        reject(new Error(`Maven exited with code ${code}`));
      }
    });

    mvn.on('error', (err) => {
      reject(err);
    });
  });
}

/**
 * Main function
 */
async function main() {
  const mavenArgs = process.argv.slice(2);

  if (mavenArgs.length === 0) {
    console.error('Usage: run-mvn.js <maven-arguments>');
    console.error('Example: run-mvn.js clean install');
    process.exit(1);
  }

  try {
    // Start proxy
    await startProxy();

    // Run Maven
    await runMaven(mavenArgs);

    console.log('\n✓ Maven build completed successfully');
    process.exit(0);
  } catch (err) {
    console.error(`\n✗ Error: ${err.message}`);
    process.exit(1);
  } finally {
    // Always stop proxy
    stopProxy();
  }
}

// Handle Ctrl+C
process.on('SIGINT', () => {
  console.log('\nReceived SIGINT, cleaning up...');
  stopProxy();
  process.exit(130);
});

// Handle termination
process.on('SIGTERM', () => {
  console.log('\nReceived SIGTERM, cleaning up...');
  stopProxy();
  process.exit(143);
});

// Run main function
main();
