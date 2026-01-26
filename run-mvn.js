#!/usr/bin/env node

/**
 * run-mvn.js - Maven wrapper with proxy support
 *
 * This script:
 * 1. Starts a local proxy server (maven-proxy.js) in the background
 * 2. Configures Maven via ~/.m2/settings.xml to use the proxy
 * 3. Runs Maven with provided CLI arguments
 * 4. Stops the proxy server when done
 *
 * Usage: ./run-mvn.js <maven-arguments>
 * Example: ./run-mvn.js clean package -DskipTests
 */

const { spawn } = require('child_process');
const fs = require('fs');
const path = require('path');
const os = require('os');

const PROXY_PORT = 8080;
const PROXY_URL = `http://localhost:${PROXY_PORT}`;

/**
 * Generate Maven settings.xml with proxy configuration
 */
function generateSettingsXml() {
  return `<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <mirrors>
    <mirror>
      <id>maven-proxy</id>
      <name>Maven Central Proxy</name>
      <url>${PROXY_URL}</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
</settings>
`;
}

/**
 * Write settings.xml to ~/.m2/settings.xml
 */
function writeSettingsXml() {
  const m2Dir = path.join(os.homedir(), '.m2');
  const settingsPath = path.join(m2Dir, 'settings.xml');

  if (!fs.existsSync(m2Dir)) {
    fs.mkdirSync(m2Dir, { recursive: true });
  }

  if (fs.existsSync(settingsPath)) {
    const backupPath = `${settingsPath}.backup`;
    if (!fs.existsSync(backupPath)) {
      fs.copyFileSync(settingsPath, backupPath);
    }
  }

  fs.writeFileSync(settingsPath, generateSettingsXml());
  console.log(`Maven settings configured at: ${settingsPath}\n`);
}

/**
 * Start the proxy server in the background
 */
function startProxyServer() {
  return new Promise((resolve, reject) => {
    const proxyScript = path.join(__dirname, 'maven-proxy.js');

    if (!fs.existsSync(proxyScript)) {
      reject(new Error(`Proxy script not found: ${proxyScript}`));
      return;
    }

    const proxyProcess = spawn('node', [proxyScript], {
      detached: false,
      stdio: ['ignore', 'pipe', 'pipe']
    });

    let started = false;
    let output = '';

    const timeout = setTimeout(() => {
      if (!started) {
        proxyProcess.kill();
        reject(new Error('Proxy server failed to start within 5 seconds'));
      }
    }, 5000);

    proxyProcess.stdout.on('data', (data) => {
      output += data.toString();
      const lines = output.split('\n');
      for (const line of lines) {
        if (line.includes('Maven proxy server listening')) {
          if (!started) {
            started = true;
            clearTimeout(timeout);
            console.log('Proxy server started\n');
            resolve(proxyProcess);
          }
        }
      }
    });

    proxyProcess.stderr.on('data', (data) => {
      console.error(data.toString());
    });

    proxyProcess.on('error', (err) => {
      if (!started) {
        clearTimeout(timeout);
        reject(err);
      }
    });

    proxyProcess.on('exit', (code) => {
      if (!started) {
        clearTimeout(timeout);
        reject(new Error(`Proxy server exited with code ${code} before starting`));
      }
    });
  });
}

/**
 * Run Maven with the provided arguments
 */
function runMaven(args) {
  return new Promise((resolve, reject) => {
    console.log(`Running: mvn ${args.join(' ')}\n`);

    const mvnProcess = spawn('mvn', args, {
      stdio: 'inherit',
      env: process.env
    });

    mvnProcess.on('exit', (code) => {
      resolve(code);
    });

    mvnProcess.on('error', (err) => {
      reject(err);
    });
  });
}

/**
 * Main execution
 */
async function main() {
  const mvnArgs = process.argv.slice(2);

  if (mvnArgs.length === 0) {
    console.error('Usage: ./run-mvn.js <maven-arguments>');
    console.error('Example: ./run-mvn.js clean package -DskipTests');
    process.exit(1);
  }

  let proxyProcess = null;

  try {
    writeSettingsXml();
    proxyProcess = await startProxyServer();
    const exitCode = await runMaven(mvnArgs);

    if (exitCode === 0) {
      console.log('\n✓ Maven build completed successfully!');
    } else {
      console.log(`\n✗ Maven exited with code ${exitCode}`);
    }

    process.exit(exitCode);
  } catch (err) {
    console.error(`\nError: ${err.message}`);
    process.exit(1);
  } finally {
    if (proxyProcess && !proxyProcess.killed) {
      proxyProcess.kill('SIGTERM');
      setTimeout(() => {
        if (!proxyProcess.killed) {
          proxyProcess.kill('SIGKILL');
        }
      }, 1000);
    }
  }
}

process.on('SIGINT', () => process.exit(130));
process.on('SIGTERM', () => process.exit(143));

main();
