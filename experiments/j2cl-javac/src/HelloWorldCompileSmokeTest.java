import gwtjava.io.fs.FileSystem;
import javac.com.sun.tools.javac.Javac;

public final class HelloWorldCompileSmokeTest {
  public static void main(String[] args) {
    String source = "public class HelloWorld { public static void main(String[] args) { System.out.println(\"hello from javac-js experiment\"); }}";
    Javac.compile("HelloWorld.java", source);

    FileSystem fs = FileSystem.instance();
    String classFile = fs.cwd() + "HelloWorld.class";
    if (!fs.exists(classFile)) {
      throw new IllegalStateException("Compilation failed, expected class not found: " + classFile);
    }

    byte[] content = fs.readFile(classFile);
    if (content == null || content.length == 0) {
      throw new IllegalStateException("Compilation produced an empty class file");
    }

    System.out.println("Compiled HelloWorld.class bytes=" + content.length);
  }
}
