#!/usr/bin/env node
/**
 * This script extracts the GWT javac module from the local Maven build output
 * and bundles it as a self-contained JavaScript module.
 */

import { readFileSync, writeFileSync, readdirSync, existsSync } from 'fs';
import { dirname, join } from 'path';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const packageDir = join(__dirname, '..');
const projectRoot = join(packageDir, '..', '..');

// Path to the GWT output from Maven build
const GWT_OUTPUT_DIR = join(projectRoot, 'target', 'war', 'javac');

function findCacheFile() {
  if (!existsSync(GWT_OUTPUT_DIR)) {
    throw new Error(
      `GWT output directory not found: ${GWT_OUTPUT_DIR}\n` +
      'Please run "mvn -DskipTests package" from the project root first.'
    );
  }

  const files = readdirSync(GWT_OUTPUT_DIR);

  // Find .cache.html files (GWT compiled output)
  // The strong name is a hash like "517F3358019201A43E384D8211401F14"
  const cacheFiles = files.filter(f => f.endsWith('.cache.html'));

  if (cacheFiles.length === 0) {
    throw new Error(
      `No .cache.html files found in ${GWT_OUTPUT_DIR}\n` +
      'Please ensure the Maven build completed successfully.'
    );
  }

  // If there are multiple cache files, prefer the webkit/safari one for jsdom compatibility
  // GWT generates different permutations for different browsers
  // For now, just use the first one found (there's typically only one in production builds)
  const cacheFile = cacheFiles[0];
  const strongName = cacheFile.replace('.cache.html', '');

  console.log(`Found GWT cache file: ${cacheFile}`);
  return { path: join(GWT_OUTPUT_DIR, cacheFile), strongName };
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

function createBundledModule(headScript, bodyScript, strongName) {
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
      const $strongName = ${JSON.stringify(strongName)};
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

function main() {
  try {
    const { path: cacheFilePath, strongName } = findCacheFile();

    console.log(`Reading GWT cache from: ${cacheFilePath}`);
    const html = readFileSync(cacheFilePath, 'utf-8');

    const { headScript, bodyScript } = extractJavaScript(html);
    console.log(`Extracted ${bodyScript.length} bytes of JavaScript.`);

    const bundledModule = createBundledModule(headScript, bodyScript, strongName);

    const outputPath = join(packageDir, 'src', 'gwt-bundle.js');
    writeFileSync(outputPath, bundledModule);
    console.log(`Written bundled module to ${outputPath}`);

  } catch (error) {
    console.error('Error:', error.message);
    process.exit(1);
  }
}

main();
