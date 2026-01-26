/// <reference types="vite/client" />

declare module '/javac-api.js' {
  export interface CompilationResult {
    success: boolean
    className?: string
    classFile?: Uint8Array
    errors?: string[]
  }

  export interface JavacAPI {
    compile: (source: string, options?: { fileName?: string }) => CompilationResult
    getClassName: (source: string) => string
  }

  export interface LoadJavacOptions {
    scriptUrl?: string
    timeoutMs?: number
  }

  export function loadJavac(options?: LoadJavacOptions): Promise<JavacAPI>
  export function createCompilerFromApi(api: any): JavacAPI
}
