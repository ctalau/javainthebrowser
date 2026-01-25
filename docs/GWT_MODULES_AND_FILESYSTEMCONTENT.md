# GWT Modules and FileSystemContent Architecture

This document describes the four GWT modules that make up Java in the Browser and how FileSystemContent is generated and used across the build and runtime lifecycle.

## Table of Contents

1. [Overview](#overview)
2. [Module Architecture](#module-architecture)
3. [FileSystemContent Generation](#filesystemcontent-generation)
4. [FileSystemContent Usage](#filesystemcontent-usage)
5. [Module Details](#module-details)
6. [Compilation and Runtime Flow](#compilation-and-runtime-flow)

---

## Overview

Java in the Browser consists of 4 main GWT modules that work together to provide a complete Java development environment in the browser:

1. **gwtjava** - Java Standard Library (foundation)
2. **javac** - Java Compiler
3. **jvm** - JVM Execution Engine
4. **Jib** - Demo Application

These modules are built in a dependency hierarchy where `gwtjava` is the foundation providing standard library classes, while `javac` and `jvm` both depend on `gwtjava`, and the demo app (`Jib`) pulls everything together.

### Module Dependency Diagram

```
Jib.gwt.xml (Demo App - Top Level)
├── javac.javac (Java Compiler)
│   └── gwtjava.gwtjava (Java Std Lib)
└── jvm.jvm (JVM Execution Engine)
    └── gwtjava.gwtjava (Java Std Lib)

com.google.gwt.user.User (GWT Core - inherited by all)
```

---

## Module Architecture

### Module 1: gwtjava - Java Standard Library

**Location:** `/packages/javac/java/gwtjava/`

**Module File:** `gwtjava.gwt.xml`

**Purpose:** Foundation library providing Java standard library classes compiled to GWT/JavaScript

**Key Configuration:**
```xml
<module rename-to='gwtjava'>
  <inherits name='com.google.gwt.user.User'/>
  <inherits name='com.google.gwt.user.theme.clean.Clean'/>
  <source path='io'/>
  <source path='lang'/>
  <source path='net'/>
  <source path='nio'/>
  <source path='security'/>
  <source path='statics'/>
  <source path='util'/>
</module>
```

**Contents (104 Java files):**

- **io/** - File I/O classes with custom FileSystem integration
  - `File`, `FileInputStream`, `FileOutputStream`, `FileReader`, `FileWriter`
  - `BufferedReader`, `BufferedWriter`, `DataInputStream`, `DataOutputStream`
  - `PrintStream`, `PrintWriter`, `InputStream`, `OutputStream`
  - **fs/** - Custom FileSystem implementation
    - `FileSystem.java` - Abstract base class
    - `JSFileSystem.java` - Browser implementation using FileSystemContent
    - `FileSystemContent.java` - **Generated class** with embedded JRE bytecode

- **lang/** - Language fundamentals
  - Core classes: `Object`, `String`, `Class`, `System`, `Thread`
  - Reflection support, annotations, type system

- **util/** - Collections and utilities
  - Collections: `HashMap`, `HashSet`, `ArrayList`, `Arrays`, `Collections`
  - Concurrent utilities: `AtomicInteger`, `ConcurrentHashMap`
  - Other: `Date`, `Calendar`, `UUID`, `logging`, `regex`, `jar`, `zip`

- **nio/** - Non-blocking I/O classes
- **net/** - Networking classes
- **security/** - Security classes

**Entry Points:** None (library module)

**Key Feature - FileSystemContent Integration:**

The `gwtjava` module contains the critical `io/fs/` package which implements the virtual file system used by the entire application:

- **FileSystemContent.java** - Generated class containing all embedded JRE library files
- **JSFileSystem.java** - Uses FileSystemContent to initialize the file system:
  ```java
  public void reset() {
      files.clear();
      for (Map.Entry<String, String> entry : FileSystemContent.files.entrySet()) {
          files.put("/jre/" + entry.getKey(), hexDecode(entry.getValue()));
      }
  }
  ```

---

### Module 2: javac - Java Compiler

**Location:** `/packages/javac/java/javac/`

**Module File:** `javac.gwt.xml`

**Purpose:** OpenJDK javac compiler compiled to GWT/JavaScript for browser-based Java compilation

**Key Configuration:**
```xml
<module rename-to='javac'>
  <inherits name='com.google.gwt.user.User'/>
  <inherits name='gwtjava.gwtjava' />
  <inherits name='com.google.gwt.user.theme.clean.Clean'/>
  <entry-point class='javac.client.JavacEntryPoint'/>
  <source path='client'/>
  <source path='com'/>
  <source path='javax'/>
</module>
```

**Contents (290 Java files):**

- **client/JavacEntryPoint.java** - Exported JavaScript API:
  - `compile(fileName, source)` - Compiles Java source code to bytecode
  - `getClassName(source)` - Extracts class name from source
  - `readClassFileBase64(className)` - Returns compiled .class file as Base64

- **com/sun/tools/javac/** - Full OpenJDK javac compiler
  - `api/` - Compiler API classes
  - `code/` - Symbol and type definitions
  - `comp/` - Compilation phases (Enter, Attr, Flow, Resolve, etc.)
  - `file/` - File manager and JavaFileObject implementations
  - `jvm/` - JVM bytecode generation
  - `main/` - Main compiler entry points
  - `model/` - Program element model
  - `parser/` - Java source parser
  - `processing/` - Annotation processing
  - `tree/` - Abstract syntax tree (AST)
  - `util/` - Compiler utilities

**Entry Points:**
- `javac.client.JavacEntryPoint` - Exports JavaScript compilation API

**Dependencies:**
- GWT Core (`com.google.gwt.user.User`)
- Java Standard Library (`gwtjava.gwtjava`)
- GWT Theme (`com.google.gwt.user.theme.clean.Clean`)

**FileSystemContent Integration:**

The javac module depends on gwtjava and uses the FileSystem indirectly. When javac writes compiled `.class` files, they are written to the file system which is backed by FileSystemContent's embedded JRE files. This ensures that all compilation happens in an environment where standard library classes are available.

---

### Module 3: jvm - JVM Execution Engine

**Location:** `/src/jvm/`

**Module File:** `jvm.gwt.xml`

**Purpose:** JVM implementation for executing compiled Java bytecode in the browser

**Key Configuration:**
```xml
<module rename-to='jvm'>
  <inherits name='com.google.gwt.user.User'/>
  <inherits name='gwtjava.gwtjava' />
  <inherits name='com.google.gwt.user.theme.clean.Clean'/>
  <source path='classparser'/>
  <source path='execution'/>
  <source path='util'/>
  <source path='main'/>
</module>
```

**Contents (25 Java files):**

- **classparser/** - Java class file format parser
  - `JClass.java` - Represents a parsed Java class
  - `JConstantPool.java` - Constant pool parsing
  - `JMember.java` - Class members (fields, methods)
  - `JType.java` - Type system
  - `JAttribute.java` - Class file attributes
  - `jconstants/` - Constant pool entry types

- **execution/** - Bytecode execution engine
  - `ExecutionEngine.java` - Main VM execution loop with opcode dispatch
  - `JClassLoader.java` - Class loading and resolution
  - `OPCodes.java` - JVM bytecode opcode implementation
  - `Stack.java` - JVM operand stack
  - `Natives.java` - Native method implementations
  - `objrepr/` - Object representation (ArrayRepr, ObjectRepr, ObjectFactory)

- **main/** - Public API
  - `JVM.java` - Entry point:
    - `setClassLoader(JClassLoader)` - Install custom class loader
    - `run(String path)` - Execute a class by name

- **util/** - JVM utilities

**Entry Points:** None (library module with static API)

**Dependencies:**
- GWT Core (`com.google.gwt.user.User`)
- Java Standard Library (`gwtjava.gwtjava`)
- GWT Theme (`com.google.gwt.user.theme.clean.Clean`)

**FileSystemContent Integration:**

The jvm module is the heaviest user of FileSystemContent at runtime:

1. `JClassLoader.loadClass()` uses `FileSystem.instance().readFile(path)` to load class bytecode
2. Classes are resolved from the file system which contains:
   - Embedded JRE standard library classes (from FileSystemContent)
   - Compiled user code (written by javac)
3. When a class references standard library classes (e.g., `String`, `ArrayList`), they are loaded from FileSystemContent

---

### Module 4: Jib - Demo Application

**Location:** `/src/jib/`

**Module File:** `Jib.gwt.xml`

**Purpose:** Complete demonstration application integrating javac and jvm in a browser UI

**Key Configuration:**
```xml
<module rename-to='jib'>
  <inherits name='com.google.gwt.user.User'/>
  <inherits name='javac.javac' />
  <inherits name='jvm.jvm' />
  <inherits name='com.google.gwt.user.theme.clean.Clean'/>
  <entry-point class='jib.client.Jib'/>
  <source path='client'/>
  <set-property name="user.agent" value="gecko1_8"/>
</module>
```

**Contents (3 Java files):**

- **client/Jib.java** - Main entry point that:
  1. Creates browser UI with editor and output area
  2. Integrates System.out/err redirection to UI
  3. On user action:
     - Extracts Java source and class name
     - Compiles via javac module
     - Executes via jvm module with custom class loader

- **client/JibClassLoader.java** - Custom class loader for loading user code

- **client/TextAreaPrintStream.java** - Redirects System.out to UI

**Entry Point:**
- `jib.client.Jib` - Web application entry point

**Dependencies:**
- GWT Core (`com.google.gwt.user.User`)
- Java Compiler (`javac.javac`)
- JVM Engine (`jvm.jvm`)
- GWT Theme (`com.google.gwt.user.theme.clean.Clean`)
- Transitively: Java Standard Library (`gwtjava.gwtjava`)

**FileSystemContent Integration:**

The demo app uses FileSystemContent transparently through the compilation and execution pipeline:
1. User submits Java code
2. javac compiles it (using FileSystem backed by FileSystemContent)
3. jvm loads and executes (loading from same FileSystemContent-initialized filesystem)

---

## FileSystemContent Generation

FileSystemContent is a **generated Java class** that contains the embedded Java Runtime Environment bytecode. It is created at **build time** and used at **runtime**.

### Generation Process

**Tool:** `jre/tool/ExtractJre.java`

**Input:**
- Manifest file: `jre/jre-contents` (list of 90 JRE classes to include)
- System JRE: Java 8 standard library from the build machine

**Process:**

1. **Read Manifest:**
   ```
   java/lang/Object.class
   java/lang/String.class
   java/lang/Class.class
   ... (90 total classes)
   ```

2. **Load Bytecode:**
   - For standard classes: Load from system JRE via `ClassLoader.getSystemClassLoader()`
   - For custom overrides: Load from `jre/` directory
     - `jre/java/io/ConsolePrintStream.java` - Browser console output
     - `jre/sun/misc/Unsafe.java` - Low-level memory operations stub

3. **Hex-Encode:**
   - Convert each .class file to hex string
   - Example: `java/lang/String.class` → `"cafebabe0000003200..."`

4. **Generate FileSystemContent.java:**
   ```java
   public class FileSystemContent {
       public static final HashMap<String, String> files = new HashMap<>();
       static {
           files.put("java/lang/Object.class", "cafebabe00000034...");
           files.put("java/lang/String.class", "cafebabe00000034...");
           // ... 90 classes total
       }
   }
   ```

**Output Location:**
`/packages/javac/java/gwtjava/io/fs/FileSystemContent.java`

**Size:**
- Source file: ~828KB (hex-encoded bytecode strings)
- Compiled: Embedded in `javac.nocache.js` and `jib.nocache.js`

**When Generated:**
- Maven build time (`mvn clean package`)
- Part of the build process before GWT compilation
- Must be regenerated if `jre/jre-contents` changes

---

## FileSystemContent Usage

FileSystemContent is used in two distinct phases:

### 1. Compile-Time Usage (Build Phase)

During Maven build and GWT compilation:

1. **FileSystemContent Generation:** `ExtractJre.java` tool runs
2. **GWT Compilation:** FileSystemContent.java is compiled into GWT modules
3. **Bytecode Embedding:** Hex-encoded strings become part of the compiled JavaScript modules
4. **Module Assembly:** `gwtjava`, `javac`, and `jvm` modules all include FileSystemContent

### 2. Runtime Usage (Browser Execution Phase)

When the application runs in the browser:

```
User Opens App
  ↓
Browser Loads GWT Compiled JavaScript
  ↓
JSFileSystem.reset() Called at Startup
  ↓
Iterates FileSystemContent.files
  ↓
Hex-Decodes Each Bytecode String
  ↓
Populates In-Memory File System (/jre/*)
  ↓
Application Ready
```

**Runtime File System Layout:**
```
In-Memory File System:
/jre/java/lang/Object.class         (from FileSystemContent)
/jre/java/lang/String.class         (from FileSystemContent)
/jre/java/util/HashMap.class        (from FileSystemContent)
... (88 more JRE classes)
/class/UserClassName.class          (from javac compilation output)
```

---

## FileSystemContent Usage Across Modules

### Module Usage Table

| Module | How FileSystemContent is Used |
|--------|-------------------------------|
| **gwtjava** | Defines and initializes FileSystemContent; JSFileSystem.reset() loads all bytecode strings from FileSystemContent into the in-memory virtual file system under `/jre/` directory |
| **javac** | Inherits from gwtjava and uses the FileSystem (populated by FileSystemContent) to ensure standard library classes are available during compilation |
| **jvm** | Inherits from gwtjava and uses JClassLoader to load standard library classes from the FileSystem (initialized from FileSystemContent) at runtime |
| **Jib** | Transparently uses FileSystemContent through javac (compilation phase) and jvm (execution phase) |

### Runtime Class Loading Flow

```
User Code References: new ArrayList()
  ↓
JVM.run(className) → ExecutionEngine.execute()
  ↓
ArrayList bytecode needed
  ↓
JClassLoader.loadClass("java.util.ArrayList")
  ↓
FileSystem.instance().readFile("/jre/java/util/ArrayList.class")
  ↓
JSFileSystem.files.get("/jre/java/util/ArrayList.class")
  ↓ (Hex string was loaded from FileSystemContent at startup)
hexDecode("cafebabe...") → byte[] bytecode
  ↓
JClass jc = new JClass(bytecode)
  ↓
Class loaded and ready to execute
```

---

## Compilation and Runtime Flow

### Build-Time Flow

```
1. Maven Build Starts (mvn clean package)
   ↓
2. Extract JRE Phase
   - jre/tool/ExtractJre.java runs
   - Reads jre/jre-contents manifest
   - Loads 90 classes from system JRE + custom overrides
   - Hex-encodes each bytecode file
   - Generates packages/javac/java/gwtjava/io/fs/FileSystemContent.java
   ↓
3. Compile Sources
   - javac compiles all Java source files
   - Including the generated FileSystemContent.java
   ↓
4. GWT Compilation
   - GWT compiles gwtjava module → gwtjava.js
   - GWT compiles javac module → javac.nocache.js
   - GWT compiles jib module → jib.nocache.js
   - FileSystemContent.files embedded in JavaScript
   ↓
5. Build Artifacts Created
   - gwtjava.nocache.js (library, not typically deployed)
   - javac.nocache.js (standalone compiler)
   - jib.nocache.js (demo application)
   - All contain embedded FileSystemContent bytecode
```

### Runtime Flow

```
1. Browser Loads index.html
   ↓
2. GWT Bootstrap Runs
   - Loads jib.nocache.js
   - Loads dependencies (javac.nocache.js, gwtjava.nocache.js)
   ↓
3. JSFileSystem Initialization
   - JSFileSystem.reset() executes
   - Reads FileSystemContent.files (static initializer)
   - Decodes hex strings to byte arrays
   - Populates in-memory filesystem at /jre/*
   ↓
4. Jib.onModuleLoad() Executes
   - Creates UI (editor, run button)
   - Sets up custom PrintStream for output
   ↓
5. User Enters Java Code and Clicks Run
   ↓
6. Compilation Phase
   - JavaScript calls Javac.compile(source)
   - Javac module reads FileSystem (FileSystemContent populated)
   - References String, System, etc. from FileSystemContent
   - Outputs compiled .class bytecode
   - Writes to /class/UserClassName.class
   ↓
7. Execution Phase
   - JavaScript calls JVM.run(className)
   - JVM loads UserClassName from /class/
   - User code references ArrayList, System.out, etc.
   - JClassLoader loads from /jre/* (FileSystemContent)
   - ExecutionEngine interprets bytecode
   - System.out redirected to TextAreaPrintStream
   - Output appears in UI
```

---

## Summary

**FileSystemContent** is the critical bridge between the Java Runtime Environment and the browser:

1. **Generated at Build Time:** Extracts 90 JRE classes from system JRE, hex-encodes them, and generates a Java class
2. **Compiled into JavaScript:** Becomes static data in GWT modules
3. **Decoded at Runtime:** Browser-based JSFileSystem decodes hex strings into byte arrays
4. **Provides Foundation:** All compilation and execution uses classes from FileSystemContent
5. **Enables Transparency:** Users can write normal Java code that references `String`, `ArrayList`, `System`, etc. without knowing they're coming from FileSystemContent

The 4 GWT modules (`gwtjava`, `javac`, `jvm`, `Jib`) form a complete Java development environment where FileSystemContent is the invisible foundation making it all work.
