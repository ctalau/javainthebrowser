/**
 * @javainthebrowser/javac - Java compiler (javac) compiled to JavaScript via GWT
 *
 * This package provides a self-contained Java compiler that runs in JavaScript.
 * It can be used in browsers or Node.js (with jsdom).
 */

import { initializeJavac, type GwtEnvironment, type GwtJavacApi as ImportedGwtJavacApi } from './gwt-bundle.js';

/**
 * Options for compiling Java source code.
 */
export interface CompileOptions {
  /**
   * Override the file name used for error messages.
   * If not specified, the file name is derived from the class name.
   */
  fileName?: string;
}

/**
 * Result of compiling Java source code.
 */
export interface CompileResult {
  /** Whether compilation was successful */
  success: boolean;
  /** The name of the compiled class */
  className: string;
  /** Base64-encoded .class file bytes (null if compilation failed) */
  classFileBase64: string | null;
}

/**
 * The Java compiler interface.
 */
export interface JavacCompiler {
  /**
   * Compile Java source code to bytecode.
   * @param source - The Java source code to compile
   * @param options - Optional compilation options
   * @returns The compilation result
   */
  compile(source: string, options?: CompileOptions): CompileResult;

  /**
   * Extract the class name from Java source code without compiling.
   * @param source - The Java source code
   * @returns The class name
   */
  getClassName(source: string): string;
}

/**
 * Internal GWT API interface (not exported to users).
 */
interface GwtJavacApi {
  compileSource(source: string, fileNameOverride?: string | null): CompileResult;
  getClassName(source: string): string;
}

/**
 * Options for creating a Java compiler instance.
 */
export interface CreateJavacOptions {
  /**
   * Custom window object to use. If not provided:
   * - In browser: uses the global window
   * - In Node.js: creates a jsdom window
   */
  window?: Window;

  /**
   * Custom document object to use. If not provided:
   * - In browser: uses the global document
   * - In Node.js: creates a jsdom document
   */
  document?: Document;
}

/**
 * Create a wrapper around the GWT API with a clean TypeScript interface.
 */
function createCompiler(api: GwtJavacApi): JavacCompiler {
  return {
    compile(source: string, options: CompileOptions = {}): CompileResult {
      if (!source) {
        throw new Error('compile() requires source code');
      }
      return api.compileSource(source, options.fileName || null);
    },

    getClassName(source: string): string {
      if (!source) {
        throw new Error('getClassName() requires source code');
      }
      return api.getClassName(source);
    },
  };
}

/**
 * Create a simulated browser environment for Node.js using jsdom.
 */
async function createNodeEnvironment(): Promise<{ window: Window; document: Document }> {
  // Dynamic import of jsdom to avoid bundling issues in browsers
  const { JSDOM } = await import('jsdom');

  const dom = new JSDOM('<!DOCTYPE html><html><head></head><body></body></html>', {
    url: 'http://localhost/',
    runScripts: 'dangerously',
    pretendToBeVisual: true,
  });

  // Stub window.alert to prevent jsdom "not implemented" errors
  (dom.window as typeof dom.window & { alert: (msg: string) => void }).alert = () => {};

  return {
    window: dom.window as unknown as Window,
    document: dom.window.document,
  };
}

/**
 * Detect if we're running in a browser environment.
 */
function isBrowser(): boolean {
  return typeof window !== 'undefined' && typeof document !== 'undefined';
}

// Cached compiler instance for singleton pattern
let cachedCompilerPromise: Promise<JavacCompiler> | null = null;

/**
 * Create a new Java compiler instance.
 *
 * This function initializes the GWT-compiled javac module and returns
 * a compiler instance that can compile Java source code to bytecode.
 *
 * In browser environments, it uses the global window and document.
 * In Node.js environments, it creates a jsdom environment automatically
 * (requires jsdom as a peer dependency).
 *
 * @param options - Optional configuration
 * @returns A promise that resolves to a JavacCompiler instance
 *
 * @example
 * ```typescript
 * import { createJavac } from '@javainthebrowser/javac';
 *
 * const javac = await createJavac();
 * const result = javac.compile('public class Hello { }');
 * console.log(result.success); // true
 * console.log(result.classFileBase64); // "yv66vgAA..."
 * ```
 */
export async function createJavac(options: CreateJavacOptions = {}): Promise<JavacCompiler> {
  let env: GwtEnvironment;

  if (options.window && options.document) {
    // Use provided environment
    env = { window: options.window as GwtEnvironment['window'], document: options.document };
  } else if (isBrowser()) {
    // Use global browser environment
    env = { window, document };
  } else {
    // Create jsdom environment for Node.js
    env = await createNodeEnvironment() as GwtEnvironment;
  }

  const api = await initializeJavac(env);
  return createCompiler(api as GwtJavacApi);
}

/**
 * Get a shared Java compiler instance (singleton).
 *
 * This is useful when you want to reuse the same compiler instance
 * across multiple calls, avoiding re-initialization overhead.
 *
 * @returns A promise that resolves to a JavacCompiler instance
 *
 * @example
 * ```typescript
 * import { getJavac } from '@javainthebrowser/javac';
 *
 * // First call initializes the compiler
 * const javac1 = await getJavac();
 *
 * // Subsequent calls return the same instance
 * const javac2 = await getJavac();
 * console.log(javac1 === javac2); // true
 * ```
 */
export async function getJavac(): Promise<JavacCompiler> {
  if (!cachedCompilerPromise) {
    cachedCompilerPromise = createJavac();
  }
  return cachedCompilerPromise;
}

/**
 * Reset the cached compiler instance.
 * This is mainly useful for testing purposes.
 */
export function resetJavac(): void {
  cachedCompilerPromise = null;
}
