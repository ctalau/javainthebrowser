#!/usr/bin/env node
/**
 * This script downloads the pre-built GWT javac module from GitHub Pages
 * and bundles it as a self-contained JavaScript module.
 */

import { readFileSync, writeFileSync, mkdirSync, existsSync } from 'fs';
import { dirname, join } from 'path';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const packageDir = join(__dirname, '..');

// GWT cache file for webkit/safari (most compatible with jsdom)
const GWT_CACHE_URL = 'https://ctalau.github.io/javainthebrowser/javac/517F3358019201A43E384D8211401F14.cache.html';

async function fetchGwtCache() {
  console.log('Fetching GWT cache file from GitHub Pages...');

  // Try Node.js fetch first
  try {
    const response = await fetch(GWT_CACHE_URL);
    if (response.ok) {
      return await response.text();
    }
  } catch {
    // Fall through to curl
  }

  // Try curl as fallback (works better in some environments)
  const { execSync } = await import('child_process');
  try {
    const result = execSync(`curl -s "${GWT_CACHE_URL}"`, { encoding: 'utf-8', maxBuffer: 10 * 1024 * 1024 });
    if (result && result.includes('<html>')) {
      return result;
    }
  } catch {
    // Fall through
  }

  throw new Error('Failed to fetch GWT cache file');
}

function extractJavaScript(html) {
  // The GWT cache.html file has JavaScript split across multiple <script> tags
  // Format: <body><script><!--\n...code...\n--></script>\n<script><!--\n...more code...\n--></script>...

  // Extract the head script (initialization)
  const headMatch = html.match(/<head>.*?<script>(.*?)<\/script>/s);
  const headScript = headMatch ? headMatch[1] : '';

  // Extract all body script blocks and concatenate them
  // Each block is wrapped in HTML comments: <!--\n...code...\n-->
  const bodySection = html.match(/<body>([\s\S]*?)<\/body>/);
  if (!bodySection) {
    throw new Error('Failed to find body section in GWT cache file');
  }

  // Find all script content within the body
  const scriptBlocks = [];
  const scriptRegex = /<script><!--\n([\s\S]*?)\n--><\/script>/g;
  let match;
  while ((match = scriptRegex.exec(bodySection[1])) !== null) {
    scriptBlocks.push(match[1]);
  }

  if (scriptBlocks.length === 0) {
    throw new Error('Failed to extract JavaScript from GWT cache file');
  }

  // Concatenate all script blocks with newlines
  const bodyScript = scriptBlocks.join('\n');
  console.log(`Found ${scriptBlocks.length} script blocks.`);

  return { headScript, bodyScript };
}

function createBundledModule(headScript, bodyScript) {
  // Append a call to gwtOnLoad to trigger module initialization
  const gwtCodeWithInit = bodyScript + '\n\n// Trigger GWT module initialization\ngwtOnLoad(null, "javac", "", 0);';

  return `// Auto-generated from GWT-compiled javac module
// Do not edit directly - run 'npm run bundle-gwt' to regenerate

/**
 * Initialize the GWT javac module in the given environment.
 * @param {object} env - Environment with window-like and document-like objects
 * @returns {Promise<object>} - The javac API object
 */
export function initializeJavac(env) {
  return new Promise((resolve, reject) => {
    const timeout = setTimeout(() => {
      reject(new Error('Timed out waiting for javac module to initialize'));
    }, 30000);

    // Set up the callback that GWT will call when ready
    env.window.__javaInTheBrowserJavacReady = (api) => {
      clearTimeout(timeout);
      resolve(api);
    };

    try {
      // Create a function scope for the GWT code
      const gwtCode = createGwtCode();

      // Set up GWT globals that the GWT code expects
      const $wnd = env.window;
      const $doc = env.document;
      let $moduleName = 'javac';
      let $moduleBase = '';
      const $strongName = '517F3358019201A43E384D8211401F14';
      const $stats = null;
      const $sessionId = null;

      // Execute the GWT code in a function scope with the required globals
      const fn = new Function(
        '$wnd', '$doc', '$moduleName', '$moduleBase', '$strongName', '$stats', '$sessionId',
        gwtCode
      );
      fn($wnd, $doc, $moduleName, $moduleBase, $strongName, $stats, $sessionId);
    } catch (error) {
      clearTimeout(timeout);
      reject(error);
    }
  });
}

function createGwtCode() {
  // Return the GWT-compiled JavaScript as a string (with gwtOnLoad call appended)
  return ${JSON.stringify(gwtCodeWithInit)};
}
`;
}

async function main() {
  try {
    // Check if we can fetch (for offline development, allow using cached version)
    let html;
    const cacheFile = join(packageDir, '.gwt-cache.html');

    try {
      html = await fetchGwtCache();
      // Save cache for offline development
      writeFileSync(cacheFile, html);
      console.log('Downloaded and cached GWT module.');
    } catch (error) {
      if (existsSync(cacheFile)) {
        console.log('Network error, using cached GWT module...');
        html = readFileSync(cacheFile, 'utf-8');
      } else {
        throw error;
      }
    }

    const { headScript, bodyScript } = extractJavaScript(html);
    console.log(`Extracted ${bodyScript.length} bytes of JavaScript.`);

    const bundledModule = createBundledModule(headScript, bodyScript);

    const outputPath = join(packageDir, 'src', 'gwt-bundle.js');
    writeFileSync(outputPath, bundledModule);
    console.log(`Written bundled module to ${outputPath}`);

  } catch (error) {
    console.error('Error:', error.message);
    process.exit(1);
  }
}

main();
