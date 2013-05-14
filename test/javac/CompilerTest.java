package javac;

import static org.junit.Assert.*;
import gwtjava.io.fs.FileSystem;
import javac.com.sun.tools.javac.Javac;

import org.junit.Test;

public class CompilerTest {
    @Test
    public void test() {
        assertCompiles("Smth", "class Smth { String f() { return \"\"; }}");
        assertError("Error", "class Error { void f() { return \"\"; }}");
    }

    @Test
    public void testInspect() {
        assertCompiles("HelloWorld",
                "public class HelloWorld {                                  " +
                "  public static void main(String[] args) {                 " +
                "    for (int i = 0; i < 2; i++) {                          " +
                "      System.out.println(\"Hello, World!\" + i);           " +
                "    }                                                      " +
                "  }                                                        " +
                "}                                                          ");
        printContent(fs.readFile(fs.cwd() + "HelloWorld.class"));
    }

    static FileSystem fs = FileSystem.instance();

    private static void printContent(byte[] content) {
        StringBuilder sb = new StringBuilder();
        for (byte b: content) {
            if (Character.isLetterOrDigit(b)) {
                sb.append((char)b);
            } else {
                sb.append(" ");
            }
            if (sb.length() == 40) {
                System.out.println(sb.toString());
                sb = new StringBuilder();
            }
        }
    }

    private static void assertCompiles(String name, String content) {
        Javac.compile(Javac.getClassName(content) + ".java", content);
        String className = fs.cwd() + name + ".class";
        assertTrue(fs.exists(className));
    }

    private static void assertError(String name, String content) {
        Javac.compile(Javac.getClassName(content) + ".java", content);
        assertFalse(fs.exists(fs.cwd() + name + ".class"));
    }
}
