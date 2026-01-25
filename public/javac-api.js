const READY_CALLBACK = "__javaInTheBrowserJavacReady";

function debugLog(message, type = 'info') {
  if (window.debugLog) {
    window.debugLog[type](message);
  }
}

function loadScript(url) {
  debugLog(`Network request: Loading script from ${url}`, 'info');
  return new Promise((resolve, reject) => {
    const script = document.createElement("script");
    script.src = url;
    script.async = true;
    script.onload = () => {
      debugLog(`Network success: Script loaded from ${url}`, 'success');
      resolve();
    };
    script.onerror = () => {
      const error = new Error(`Failed to load javac script: ${url}`);
      debugLog(`Network error: ${error.message}`, 'error');
      reject(error);
    };
    document.head.appendChild(script);
  });
}

function createCompiler(api) {
  debugLog('Creating compiler wrapper with API', 'milestone');
  return {
    compile(source, options = {}) {
      if (!source) {
        throw new Error("compile(source, options) requires source.");
      }
      debugLog('Compiler API: Starting compilation', 'info');
      const result = api.compileSource(source, options.fileName || null);
      if (result && result.success) {
        debugLog(`Compiler API: Compilation successful for class ${result.className}`, 'success');
      } else {
        debugLog('Compiler API: Compilation failed', 'error');
      }
      return result;
    },
    getClassName(source) {
      const className = api.getClassName(source);
      debugLog(`Compiler API: Extracted class name: ${className}`, 'info');
      return className;
    }
  };
}

let loadPromise = null;

export function loadJavac(options = {}) {
  if (loadPromise) {
    debugLog('Javac module already loading, returning existing promise', 'info');
    return loadPromise;
  }

  const scriptUrl = options.scriptUrl || "javac/javac.nocache.js";
  const timeoutMs = options.timeoutMs || 10000;

  debugLog(`Initializing javac loader with script URL: ${scriptUrl}, timeout: ${timeoutMs}ms`, 'milestone');

  loadPromise = new Promise((resolve, reject) => {
    const existingCallback = globalThis[READY_CALLBACK];
    const timeoutId = setTimeout(() => {
      const error = new Error("Timed out waiting for the javac module to initialize.");
      debugLog(`Javac module loading timeout after ${timeoutMs}ms`, 'error');
      reject(error);
    }, timeoutMs);

    globalThis[READY_CALLBACK] = (api) => {
      clearTimeout(timeoutId);
      debugLog('Javac module callback invoked - module ready', 'milestone');
      if (typeof existingCallback === "function") {
        debugLog('Calling existing callback', 'info');
        existingCallback(api);
      }
      resolve(createCompiler(api));
    };

    loadScript(scriptUrl).catch((error) => {
      clearTimeout(timeoutId);
      debugLog(`Failed to load javac script: ${error.message}`, 'error');
      reject(error);
    });
  });

  return loadPromise;
}

export function createCompilerFromApi(api) {
  return createCompiler(api);
}
