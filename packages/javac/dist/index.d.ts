export interface CompileOptions {
  fileName?: string;
}

export interface CompileResult {
  success: boolean;
  className: string;
  classFileBase64: string | null;
}

export interface JavacCompiler {
  compile(source: string, options?: CompileOptions): CompileResult;
  getClassName(source: string): string;
}

export interface LoadJavacOptions {
  scriptUrl?: string;
  timeoutMs?: number;
}

export function loadJavac(options?: LoadJavacOptions): Promise<JavacCompiler>;
export function createCompilerFromApi(api: {
  compileSource: (source: string, fileNameOverride?: string | null) => CompileResult;
  getClassName: (source: string) => string;
}): JavacCompiler;
