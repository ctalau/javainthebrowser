#!/usr/bin/env node

/**
 * Test script: Load the J2CL-compiled javac and compile a Hello World program.
 *
 * The J2CL BUNDLE output uses Closure Library's module system (goog.loadModule)
 * plus $jscomp runtime helpers. We set up the necessary globals and run in
 * a VM context to simulate a browser-like environment.
 */

const fs = require('fs');
const path = require('path');
const vm = require('vm');

const javacJsPath = path.join(__dirname, 'target/javac-j2cl-1.0-SNAPSHOT/javac.js');

if (!fs.existsSync(javacJsPath)) {
    console.error('javac.js not found. Run: node ../../run-mvn.js clean package');
    process.exit(1);
}

const fileSizeMB = (fs.statSync(javacJsPath).size / 1024 / 1024).toFixed(1);
console.log(`Loading javac.js (${fileSizeMB} MB)...`);

// Create a sandbox with all needed globals
const sandbox = {};

// Copy all globals from the current environment
for (const key of Object.getOwnPropertyNames(globalThis)) {
    try {
        sandbox[key] = globalThis[key];
    } catch (e) {
        // skip non-copyable globals
    }
}

// Self-references
sandbox.self = sandbox;
sandbox.window = sandbox;
sandbox.globalThis = sandbox;
sandbox.global = sandbox;

// $jscomp runtime - minimal polyfill for Closure Compiler's module system
sandbox.$jscomp = {
    modules: {},
    registerAndLoadModule: function(fn) {
        const exports = {};
        const module = { id: '', exports: exports };
        fn(function(id) {
            return sandbox.$jscomp.modules[id] || {};
        }, exports, module);
        // Store module by its id if available
        if (module.id) {
            sandbox.$jscomp.modules[module.id] = module.exports;
        }
    },
    getCurrentModulePath: function() {
        return '';
    }
};

const code = fs.readFileSync(javacJsPath, 'utf8');

try {
    const context = vm.createContext(sandbox);
    vm.runInContext(code, context, { filename: 'javac.js', timeout: 120000 });
    console.log('javac.js loaded successfully!');

    // Access the JavacCompiler via goog.module.get
    const CompilerClass = vm.runInContext(`
        (function() {
            return goog.module.get('JavacCompiler');
        })()
    `, context);

    if (!CompilerClass) {
        console.error('Could not find JavacCompiler module');
        process.exit(1);
    }

    console.log('JavacCompiler class found:', typeof CompilerClass);

    // Test: compile a Hello World program
    const helloWorld = `public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello from J2CL-compiled javac!");
    }
}`;

    console.log('\n--- Compiling Hello World ---');
    console.log('Source:');
    console.log(helloWorld);
    console.log('');

    const result = vm.runInContext(`
        (function() {
            var JavacCompiler = goog.module.get('JavacCompiler');
            try {
                return JavacCompiler.compileSource(${JSON.stringify(helloWorld)}, null);
            } catch(e) {
                return 'ERROR: ' + e.toString() + '\\n' + (e.stack || '').split('\\n').slice(0, 5).join('\\n');
            }
        })()
    `, context, { timeout: 120000 });

    console.log('Compilation result:', result);

    if (result && typeof result === 'string' && result.includes('"success":true')) {
        console.log('\n*** SUCCESS: Hello World compiled successfully with J2CL-transpiled javac! ***');
        // Parse and show the class file info
        try {
            const parsed = JSON.parse(result);
            console.log('Class name:', parsed.className);
            console.log('Class file size:', parsed.classFileBase64 ? Math.round(parsed.classFileBase64.length * 3 / 4) : 0, 'bytes');
        } catch (e) {
            // ignore parse errors
        }
    } else if (result && typeof result === 'string' && result.startsWith('ERROR:')) {
        console.log('\nRuntime error during compilation:');
        console.log(result);
    } else {
        console.log('\nCompilation returned:', result);
    }
} catch (err) {
    console.error('Error:', err.message);
    if (err.stack) {
        console.error(err.stack.split('\n').slice(0, 15).join('\n'));
    }
    process.exit(1);
}
