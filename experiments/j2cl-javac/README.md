# J2CL Javac Experiment

Compiling the OpenJDK javac (Java compiler) to JavaScript using [J2CL](https://github.com/google/j2cl) (Java to Closure compiler), producing a Java compiler that runs entirely in JavaScript.

## What This Is

This experiment takes the existing GWT-adapted javac sources from the parent project and transpiles them using J2CL instead of GWT. The result is a 14MB JavaScript file (`javac.js`) that can compile Java source code to `.class` bytecode files in a browser or Node.js environment.

## How It Works

1. **Source**: Uses the adapted OpenJDK javac sources from `packages/javac/java/` (originally modified for GWT compatibility)
2. **Transpiler**: J2CL (via the KIE j2cl-maven-plugin 0.23.1) transpiles Java source to Closure-style JavaScript
3. **JRE Emulation**: J2CL provides its own JRE emulation for `java.lang.*`, `java.util.*`, etc. Custom `gwtjava.*` classes provide additional types not in J2CL's emulation (File I/O, reflection stubs, etc.)
4. **Entry Point**: `JavacEntryPoint` is exported as a `@JsType` named `JavacCompiler` to JavaScript

## Changes from GWT Version

- Replaced GWT `EntryPoint` + JSNI with J2CL `@JsType`/`@JsMethod` annotations
- Removed GWT module descriptors (`.gwt.xml` files)
- Removed `finalize()` override in `ZipFileIndex.java` (not in J2CL's `Object`)
- Added missing `gwtjava.lang.NoSuchMethodException` class
- Replaced `EnumSet.allOf()` calls with `HashSet` + `Arrays.asList()` (not in J2CL's `EnumSet`)
- Added explicit import for `gwtjava.lang.NoSuchMethodException` in `SClass.java` and `AnnotationProxyMaker.java`

## Building

```bash
node ../../run-mvn.js clean package
```

Output: `target/javac-j2cl-1.0-SNAPSHOT/javac.js` (~14 MB in BUNDLE mode)

## Testing

```bash
node test-hello-world.js
```

This loads the compiled javac.js and compiles a Hello World Java program, producing a valid `.class` file (verified by the `CA FE BA BE` magic number).

## JavaScript API

After loading `javac.js`, access the compiler via Closure modules:

```javascript
var JavacCompiler = goog.module.get('JavacCompiler');

// Compile and get result as JSON string
var result = JavacCompiler.compileSource(javaSource, null);
// Returns: {"success":true,"className":"HelloWorld","classFileBase64":"..."}

// Or use individual methods:
var success = JavacCompiler.compile("HelloWorld.java", javaSource);
var className = JavacCompiler.getClassName(javaSource);
var base64Class = JavacCompiler.readClassFileBase64("HelloWorld");
```

## Project Structure

```
experiments/j2cl-javac/
  pom.xml                    # Maven build with J2CL plugin
  test-hello-world.js        # Node.js test script
  src/main/java/
    javac/client/
      JavacEntryPoint.java   # J2CL entry point (@JsType)
    javac/com/sun/tools/     # Adapted OpenJDK javac sources
    javac/javax/             # javax.tools API
    gwtjava/                 # Custom JRE classes for browser
```
