import { describe, it, expect, beforeAll } from 'vitest';
import { createJavac, getJavac, resetJavac, type JavacCompiler } from './index.js';

describe('javac', () => {
  let javac: JavacCompiler;

  beforeAll(async () => {
    // Reset any cached instance
    resetJavac();
    // Create a new compiler instance
    javac = await createJavac();
  }, 60000); // Allow 60s for GWT module initialization

  describe('getClassName', () => {
    it('should extract class name from simple class', () => {
      const source = 'public class Hello { }';
      expect(javac.getClassName(source)).toBe('Hello');
    });

    it('should extract class name from class with package', () => {
      const source = `
        package com.example;
        public class MyClass { }
      `;
      expect(javac.getClassName(source)).toBe('MyClass');
    });

    it('should extract class name from class with main method', () => {
      const source = `
        public class HelloWorld {
          public static void main(String[] args) {
            System.out.println("Hello, World!");
          }
        }
      `;
      expect(javac.getClassName(source)).toBe('HelloWorld');
    });
  });

  describe('compile', () => {
    it('should compile a simple Hello World class', () => {
      const source = `
        public class HelloWorld {
          public static void main(String[] args) {
            System.out.println("Hello, World!");
          }
        }
      `;

      const result = javac.compile(source);

      expect(result.success).toBe(true);
      expect(result.className).toBe('HelloWorld');
      expect(result.classFileBase64).toBeTruthy();

      // Verify it's valid base64
      expect(() => {
        const bytes = Buffer.from(result.classFileBase64!, 'base64');
        // Java class files start with magic number 0xCAFEBABE
        expect(bytes[0]).toBe(0xCA);
        expect(bytes[1]).toBe(0xFE);
        expect(bytes[2]).toBe(0xBA);
        expect(bytes[3]).toBe(0xBE);
      }).not.toThrow();
    });

    it('should compile a class with fields and methods', () => {
      const source = `
        public class Person {
          private String name;
          private int age;

          public Person(String name, int age) {
            this.name = name;
            this.age = age;
          }

          public String getName() {
            return name;
          }

          public int getAge() {
            return age;
          }

          public String toString() {
            return name + " (" + age + ")";
          }
        }
      `;

      const result = javac.compile(source);

      expect(result.success).toBe(true);
      expect(result.className).toBe('Person');
      expect(result.classFileBase64).toBeTruthy();
    });

    it('should return failure for invalid Java code', () => {
      const source = `
        public class Invalid {
          // Missing closing brace for method
          public void broken() {
            int x = 1;
      `;

      const result = javac.compile(source);

      expect(result.success).toBe(false);
      expect(result.className).toBe('Invalid');
      expect(result.classFileBase64).toBeNull();
    });

    it('should return failure for syntax error', () => {
      const source = `
        public class SyntaxError {
          public void test() {
            int x = "not an integer";
          }
        }
      `;

      const result = javac.compile(source);

      expect(result.success).toBe(false);
    });

    it('should accept explicit fileName that matches class name', () => {
      // Note: In Java, the filename must match the public class name
      const source = 'public class Test { }';
      const result = javac.compile(source, { fileName: 'Test.java' });

      expect(result.success).toBe(true);
      expect(result.className).toBe('Test');
    });

    it('should compile class with static initializer', () => {
      const source = `
        public class StaticInit {
          private static int count;

          static {
            count = 42;
          }

          public static int getCount() {
            return count;
          }
        }
      `;

      const result = javac.compile(source);

      expect(result.success).toBe(true);
      expect(result.className).toBe('StaticInit');
    });

    it('should compile class with inner class', () => {
      const source = `
        public class Outer {
          private int value = 10;

          public class Inner {
            public int getValue() {
              return value;
            }
          }
        }
      `;

      const result = javac.compile(source);

      expect(result.success).toBe(true);
      expect(result.className).toBe('Outer');
    });

    it('should compile abstract class', () => {
      const source = `
        public abstract class Greeter {
          public abstract String greet(String name);
        }
      `;

      const result = javac.compile(source);

      expect(result.success).toBe(true);
      expect(result.className).toBe('Greeter');
    });

    it('should compile class with constants', () => {
      const source = `
        public class Color {
          public static final String RED = "red";
          public static final String GREEN = "green";
          public static final String BLUE = "blue";

          public static String display(String color) {
            return color.toLowerCase();
          }
        }
      `;

      const result = javac.compile(source);

      expect(result.success).toBe(true);
      expect(result.className).toBe('Color');
    });
  });

  describe('getJavac singleton', () => {
    it('should return the same instance', async () => {
      resetJavac();
      const instance1 = await getJavac();
      const instance2 = await getJavac();
      expect(instance1).toBe(instance2);
    });
  });

  describe('error handling', () => {
    it('should throw when compiling empty source', () => {
      expect(() => javac.compile('')).toThrow('compile() requires source code');
    });

    it('should throw when getting class name from empty source', () => {
      expect(() => javac.getClassName('')).toThrow('getClassName() requires source code');
    });
  });
});
