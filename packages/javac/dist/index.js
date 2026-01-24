const READY_CALLBACK = "__javaInTheBrowserJavacReady";

function loadScript(url) {
  return new Promise((resolve, reject) => {
    const script = document.createElement("script");
    script.src = url;
    script.async = true;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error(`Failed to load javac script: ${url}`));
    document.head.appendChild(script);
  });
}

function createCompiler(api) {
  return {
    compile(source, options = {}) {
      if (!source) {
        throw new Error("compile(source, options) requires source.");
      }
      return api.compileSource(source, options.fileName || null);
    },
    getClassName(source) {
      return api.getClassName(source);
    }
  };
}

let loadPromise = null;

export function loadJavac(options = {}) {
  if (loadPromise) {
    return loadPromise;
  }

  const scriptUrl = options.scriptUrl || "javac/javac.nocache.js";
  const timeoutMs = options.timeoutMs || 10000;

  loadPromise = new Promise((resolve, reject) => {
    const existingCallback = globalThis[READY_CALLBACK];
    const timeoutId = setTimeout(() => {
      reject(new Error("Timed out waiting for the javac module to initialize."));
    }, timeoutMs);

    globalThis[READY_CALLBACK] = (api) => {
      clearTimeout(timeoutId);
      if (typeof existingCallback === "function") {
        existingCallback(api);
      }
      resolve(createCompiler(api));
    };

    loadScript(scriptUrl).catch((error) => {
      clearTimeout(timeoutId);
      reject(error);
    });
  });

  return loadPromise;
}

export function createCompilerFromApi(api) {
  return createCompiler(api);
}
