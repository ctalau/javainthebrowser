# @javainthebrowser/javac

`@javainthebrowser/javac` exposes the JavaInTheBrowser `javac` port as an ES module API.
It loads the GWT-compiled compiler and returns a Java-centric compiler interface (no globals).

## Installation

```sh
npm install @javainthebrowser/javac
```

> **Note**: The package expects the GWT-compiled `javac` output to be available at
> `javac/javac.nocache.js` (configurable via `scriptUrl`). Build it from this repository and
> serve the generated `target/war/javac/` directory alongside your app.

## Usage

### 1) Build the GWT module (once per version)

```sh
mvn -DskipTests package
```

The compiled output is placed in `target/war/javac/` (copy it to wherever you serve static
assets from).

### 2) Load the compiler

```js
import { loadJavac } from "@javainthebrowser/javac";

const javac = await loadJavac({ scriptUrl: "/assets/javac/javac.nocache.js" });
const result = javac.compile("public class Hello {}", { fileName: "Hello.java" });
console.log(result.className, result.classFileBase64);
```

## API

### `loadJavac({ scriptUrl, timeoutMs }) -> Promise<JavacCompiler>`

Loads the GWT module script and resolves with a `JavacCompiler` once it initializes.

### `JavacCompiler`

- `compile(source, { fileName })` → `{ success, className, classFileBase64 }`
- `getClassName(source)` → string

`classFileBase64` is the compiled `.class` output for the primary class in the provided source.

## High-level architecture

1. **GWT-compiled compiler** – The Java sources under `src/javac` are compiled by GWT into a
   JavaScript module (`javac.nocache.js` + generated artifacts).
2. **In-memory file system** – The ported `javac` writes source files and class output into a
   browser-backed file system (`gwtjava.io.fs.FileSystem`).
3. **Bridge callback** – `JavacEntryPoint` hands a small API object to a loader callback
   (`__javaInTheBrowserJavacReady`) so consumers can avoid globals.
4. **ES module wrapper** – This package loads the GWT script, waits for the callback, and
   presents a compiler-centric API.

## Development

Build and serve `target/war/` (see the root README) and ensure `javac.nocache.js` is reachable
from the page that loads the API.
