package javac;

import static org.junit.Assert.*;
import gwtjava.io.fs.FileSystem;
import javac.com.sun.tools.javac.Main;

import org.junit.Test;

public class CompilerTest {
    @Test
    public void test() {
        assertCompiles("Smth", "class Smth { String f() { return \"\"; }}");
        assertError("Error", "class Error { void f() { return \"\"; }}");
    }

    static FileSystem fs = FileSystem.instance();

    private static void assertCompiles(String name, String content) {
        Main.compile(Main.getClassName(content) + ".java", content);
        assertTrue(fs.exists(fs.cwd() + name + ".class"));
    }

    private static void assertError(String name, String content) {
        Main.compile(Main.getClassName(content) + ".java", content);
        assertFalse(fs.exists(fs.cwd() + name + ".class"));
    }
}
