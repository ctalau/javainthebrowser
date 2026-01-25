# Development Guide

## Building the Project

This project uses a hybrid build system combining Maven/GWT for the Java compiler backend and Vite/React for the modern frontend.

### Prerequisites

- **Java 8 or higher** - for Maven and GWT compilation
- **Maven 3.x** - for building Java code
- **Node.js 18+** - for the React frontend
- **npm** - comes with Node.js

### Full Build Process

1. **Build the GWT modules** (Java compiler + JVM):
   ```bash
   mvn clean package
   ```
   This compiles the Java code and generates:
   - `target/war/javac/javac.nocache.js` - The Java compiler (GWT-compiled)
   - `target/war/jib/jib.nocache.js` - The JVM implementation (GWT-compiled)

2. **Install npm dependencies**:
   ```bash
   npm install
   ```

3. **Build the React frontend**:
   ```bash
   npm run build
   ```
   This creates the modern UI in `target/war/`

### Development Workflow

**Option 1: Full Stack Development**
```bash
# Build everything
mvn clean package && npm install && npm run build

# Serve the built files (use any static server)
npx serve target/war
```

**Option 2: Frontend Development Only**

If you already have the GWT files built, you can work on just the React UI:

```bash
# Build GWT once (or copy from a previous build)
mvn clean package

# Develop React app with hot reload
npm run dev
```

The dev server runs on `http://localhost:3000` with Vite's hot module replacement.

### Project Structure

```
javainthebrowser/
├── src/                          # React frontend source
│   ├── App.tsx                   # Main React component
│   ├── App.css                   # Retro-futuristic styling
│   ├── index.css                 # Global styles
│   └── main.tsx                  # React entry point
├── war/                          # Static assets & old UI
│   ├── javac-api.js              # Bridge to GWT compiler
│   └── lib/                      # CodeMirror (old version)
├── packages/javac/               # Javac npm package
├── src/ (Java)                   # Java source for GWT
│   ├── jib/                      # JVM implementation
│   └── jvm/                      # Bytecode execution
├── index.html                    # Vite HTML template
├── vite.config.ts                # Vite configuration
├── package.json                  # npm dependencies
└── pom.xml                       # Maven build config
```

### Troubleshooting

**"Failed to load javac" error:**
- Make sure you've run `mvn clean package` successfully
- Check that `target/war/javac/javac.nocache.js` exists
- The React app needs the GWT-compiled files to work

**Maven build fails:**
- Check your internet connection (Maven downloads dependencies)
- Ensure Java 8+ is installed: `java -version`
- Try clearing Maven cache: `rm -rf ~/.m2/repository`

**React build fails:**
- Delete `node_modules` and `package-lock.json`, then run `npm install` again
- Make sure you're using Node.js 18 or higher: `node --version`

**Vite dev server shows blank page:**
- Make sure the GWT files are built first
- Check browser console for errors
- Ensure `javac-api.js` is accessible

### Architecture

The application uses a unique architecture:

1. **GWT Backend**: Java code (compiler + JVM) is compiled to JavaScript using Google Web Toolkit
2. **React Frontend**: Modern UI built with React, TypeScript, and Vite
3. **Bridge**: `javac-api.js` provides a JavaScript API to the GWT-compiled Java compiler
4. **Runtime**: Everything runs in the browser - no server needed!

When you click "Run Code":
1. React app sends Java source to the GWT-compiled javac
2. javac compiles it to JVM bytecode
3. GWT-compiled JVM executes the bytecode
4. Output is displayed in the React UI

### Deployment

For Vercel deployment, the build command in `vercel.json` runs:
```bash
mvn clean package && npm run build
```

This ensures both GWT modules and the React frontend are built together.

### Modern UI Features

The new React UI includes:
- **Retro-futuristic terminal aesthetic** with neon effects
- **CodeMirror 6** for modern code editing
- **Framer Motion** for smooth animations
- **Mobile-responsive design** with touch-friendly controls
- **Debug logging panel** with detailed execution traces
- **TypeScript** for type safety

All UI dependencies are managed via npm (no committed libraries).
