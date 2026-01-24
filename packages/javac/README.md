# @javainthebrowser/javac

A self-contained Java compiler (javac) compiled to JavaScript via GWT. Compiles Java source code to bytecode entirely in the browser or Node.js.

## Features

- **Self-contained**: No external dependencies or runtime file loading required
- **Works in browsers and Node.js**: Uses jsdom for Node.js environments
- **TypeScript support**: Full type definitions included
- **Simple API**: Just `compile()` and `getClassName()`

## Installation

```sh
npm install @javainthebrowser/javac
```

For Node.js usage, also install jsdom as a peer dependency:

```sh
npm install jsdom
```

## Usage

### Basic Example

```typescript
import { createJavac } from '@javainthebrowser/javac';

// Initialize the compiler (this is async)
const javac = await createJavac();

// Compile Java source code
const result = javac.compile(`
  public class HelloWorld {
    public static void main(String[] args) {
      System.out.println("Hello, World!");
    }
  }
`);

if (result.success) {
  console.log('Compiled class:', result.className);
  console.log('Bytecode (base64):', result.classFileBase64);
} else {
  console.log('Compilation failed');
}
```

### Singleton Pattern

Use `getJavac()` to reuse the same compiler instance:

```typescript
import { getJavac } from '@javainthebrowser/javac';

// First call initializes the compiler
const javac = await getJavac();

// Subsequent calls return the same instance
const javac2 = await getJavac();
console.log(javac === javac2); // true
```

### Extract Class Name

```typescript
const className = javac.getClassName('public class MyClass { }');
console.log(className); // "MyClass"
```

## API

### `createJavac(options?): Promise<JavacCompiler>`

Creates a new Java compiler instance.

**Options:**
- `window?: Window` - Custom window object (for advanced use)
- `document?: Document` - Custom document object (for advanced use)

### `getJavac(): Promise<JavacCompiler>`

Returns a shared compiler instance (singleton pattern).

### `resetJavac(): void`

Resets the cached compiler instance. Useful for testing.

### `JavacCompiler`

#### `compile(source: string, options?: CompileOptions): CompileResult`

Compiles Java source code to bytecode.

**Options:**
- `fileName?: string` - Override the file name (must match the class name for public classes)

**Returns:**
- `success: boolean` - Whether compilation succeeded
- `className: string` - The name of the compiled class
- `classFileBase64: string | null` - Base64-encoded .class file (null if compilation failed)

#### `getClassName(source: string): string`

Extracts the class name from Java source code without compiling.

## Limitations

- Only supports compiling a single class per call
- The `getClassName()` function only works with classes (not interfaces or enums)
- Based on Java 6/7 era javac - modern Java features are not supported

## How It Works

1. The Java compiler (OpenJDK javac) was ported to work with GWT
2. GWT compiles the Java code to JavaScript
3. This package bundles the compiled JavaScript with a clean TypeScript API
4. The compiler uses an in-memory file system for source and output files

## Development

To rebuild the GWT bundle from source:

```sh
# From the repository root - compile Java to JavaScript via GWT
mvn -DskipTests package

# From packages/javac
npm run bundle-gwt  # Extracts GWT output from local Maven build (target/war/javac/)
npm run build       # Runs bundle-gwt + TypeScript compilation
npm test            # Runs tests
```

Or run both Maven and npm build in one command:

```sh
# From packages/javac
npm run build:gwt && npm run build
```

## License

GPL-2.0-only (same as the original OpenJDK javac)
