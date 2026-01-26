#!/usr/bin/env node
/**
 * Generates FileSystemContent.java from JRE classes.
 *
 * This script:
 * 1. Compiles custom override classes (ConsolePrintStream, Unsafe)
 * 2. Compiles ExtractJre.java
 * 3. Runs ExtractJre to generate FileSystemContent.java
 *
 * Requires JAVA_HOME or JAVA8_HOME to be set to a Java 8 installation.
 */

import { execSync } from 'child_process';
import { existsSync, mkdirSync, readFileSync, writeFileSync, rmSync } from 'fs';
import { dirname, join, resolve } from 'path';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const STDLIB_ROOT = resolve(__dirname, '..');
const PROJECT_ROOT = resolve(STDLIB_ROOT, '../..');
const BUILD_DIR = join(STDLIB_ROOT, 'build');
const JRE_CLASSES_DIR = join(STDLIB_ROOT, 'jre-classes');
const BUNDLED_CLASSES_DIR = join(STDLIB_ROOT, 'bundled-classes');
const TOOLS_DIR = join(STDLIB_ROOT, 'tools');
const OUTPUT_FILE = join(PROJECT_ROOT, 'packages/javac/java/gwtjava/io/fs/FileSystemContent.java');

// Get Java home - prefer JAVA8_HOME if set, otherwise JAVA_HOME
function getJavaHome() {
  const java8Home = process.env.JAVA8_HOME;
  const javaHome = process.env.JAVA_HOME;

  if (java8Home && existsSync(java8Home)) {
    return java8Home;
  }
  if (javaHome && existsSync(javaHome)) {
    return javaHome;
  }

  throw new Error('JAVA_HOME or JAVA8_HOME must be set to a valid Java installation');
}

function getJavac(javaHome) {
  const javac = join(javaHome, 'bin', 'javac');
  if (!existsSync(javac)) {
    throw new Error(`javac not found at ${javac}`);
  }
  return javac;
}

function getJava(javaHome) {
  const java = join(javaHome, 'bin', 'java');
  if (!existsSync(java)) {
    throw new Error(`java not found at ${java}`);
  }
  return java;
}

function run(cmd, options = {}) {
  console.log(`> ${cmd}`);
  try {
    execSync(cmd, { stdio: 'inherit', ...options });
  } catch (error) {
    console.error(`Command failed: ${cmd}`);
    process.exit(1);
  }
}

function main() {
  console.log('=== Generating FileSystemContent.java ===\n');

  const javaHome = getJavaHome();
  const javac = getJavac(javaHome);
  const java = getJava(javaHome);

  console.log(`Using Java from: ${javaHome}\n`);

  // Create build directory
  if (existsSync(BUILD_DIR)) {
    rmSync(BUILD_DIR, { recursive: true });
  }
  mkdirSync(BUILD_DIR, { recursive: true });
  mkdirSync(join(BUILD_DIR, 'override-classes'), { recursive: true });
  mkdirSync(join(BUILD_DIR, 'tool-classes'), { recursive: true });

  // Step 1: Compile custom override classes
  console.log('Step 1: Compiling custom override classes...');

  const overrideClasses = [
    join(JRE_CLASSES_DIR, 'java/io/ConsolePrintStream.java'),
    join(JRE_CLASSES_DIR, 'sun/misc/Unsafe.java'),
  ];

  for (const src of overrideClasses) {
    if (existsSync(src)) {
      run(`"${javac}" -source 1.8 -target 1.8 -d "${join(BUILD_DIR, 'override-classes')}" "${src}"`);
    }
  }

  console.log('');

  // Step 2: Compile ExtractJre.java
  console.log('Step 2: Compiling ExtractJre.java...');
  run(`"${javac}" -source 1.8 -target 1.8 -d "${join(BUILD_DIR, 'tool-classes')}" "${join(TOOLS_DIR, 'ExtractJre.java')}"`);
  console.log('');

  // Step 3: Run ExtractJre
  console.log('Step 3: Running ExtractJre to generate FileSystemContent.java...');

  // Create the classpath with override classes
  const classpath = join(BUILD_DIR, 'tool-classes');
  const overrideClasspath = join(BUILD_DIR, 'override-classes');

  // Run ExtractJre with the correct paths
  // Args: <jre-contents> <override-classes-dir> <bundled-classes-dir> <output-file>
  run(
    `"${java}" -cp "${classpath}" tool.ExtractJre "${join(JRE_CLASSES_DIR, 'jre-contents')}" "${overrideClasspath}" "${BUNDLED_CLASSES_DIR}" "${OUTPUT_FILE}"`,
    { cwd: STDLIB_ROOT }
  );

  console.log('');
  console.log(`=== FileSystemContent.java generated at: ${OUTPUT_FILE} ===`);
}

main();
