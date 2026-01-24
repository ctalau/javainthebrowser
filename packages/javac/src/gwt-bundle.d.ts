/**
 * Type declarations for the GWT-compiled javac bundle.
 */

export interface GwtEnvironment {
  window: Window | {
    __javaInTheBrowserJavacReady?: (api: unknown) => void;
    [key: string]: unknown;
  };
  document: Document | object;
}

export interface GwtJavacApi {
  compileSource(source: string, fileNameOverride?: string | null): {
    success: boolean;
    className: string;
    classFileBase64: string | null;
  };
  getClassName(source: string): string;
}

/**
 * Initialize the GWT javac module in the given environment.
 * @param env - Environment with window-like and document-like objects
 * @returns Promise that resolves to the javac API object
 */
export function initializeJavac(env: GwtEnvironment): Promise<GwtJavacApi>;
