import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * JVM smoke harness for the j2cl-javac-17 experiment.
 *
 * <p>This verifies the local JVM can execute javac and emit bytecode while using the fetched
 * OpenJDK 17 source tree as the experiment input baseline.
 */
public final class Javac17JvmSmokeTest {
  private Javac17JvmSmokeTest() {}

  public static void main(String[] args) throws Exception {
    Path experimentDir = args.length > 0 ? Path.of(args[0]) : Path.of("experiments/j2cl-javac-17");
    Path upstreamMain =
        experimentDir.resolve("upstream/openjdk-jdk-17/src/jdk.compiler/share/classes/com/sun/tools/javac/main/Main.java");
    if (!Files.isRegularFile(upstreamMain)) {
      throw new IllegalStateException(
          "Missing fetched OpenJDK javac source: "
              + upstreamMain
              + "\nRun scripts/fetch_javac17_sources.sh first.");
    }

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      throw new IllegalStateException("No system Java compiler found; run with a JDK, not a JRE.");
    }

    Path tempRoot = Files.createTempDirectory("javac17-jvm-smoke-");
    Path sourceDir = Files.createDirectories(tempRoot.resolve("src"));
    Path classDir = Files.createDirectories(tempRoot.resolve("classes"));
    Path helloSource = sourceDir.resolve("HelloWorld.java");

    Files.writeString(
        helloSource,
        "public class HelloWorld { public static void main(String[] args) { System.out.println(\"ok\"); } }\n",
        StandardCharsets.UTF_8);

    int exitCode;
    try (StandardJavaFileManager fileManager =
        compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
      List<String> options = Arrays.asList("-d", classDir.toString(), "--release", "17");
      exitCode =
          compiler.getTask(
                  null,
                  fileManager,
                  null,
                  options,
                  null,
                  fileManager.getJavaFileObjects(helloSource.toFile()))
              .call()
              ? 0
              : 1;
    }

    if (exitCode != 0) {
      throw new IllegalStateException("javac smoke compilation failed with exit code " + exitCode);
    }

    Path classFile = classDir.resolve("HelloWorld.class");
    if (!Files.isRegularFile(classFile)) {
      throw new IllegalStateException("Expected class file was not generated: " + classFile);
    }

    byte[] bytecode = Files.readAllBytes(classFile);
    if (bytecode.length == 0) {
      throw new IllegalStateException("Generated class file is empty: " + classFile);
    }

    int magic =
        ((bytecode[0] & 0xFF) << 24)
            | ((bytecode[1] & 0xFF) << 16)
            | ((bytecode[2] & 0xFF) << 8)
            | (bytecode[3] & 0xFF);
    if (magic != 0xCAFEBABE) {
      throw new IllegalStateException(
          "Unexpected class file header 0x"
              + Integer.toHexString(magic)
              + " for "
              + classFile);
    }

    System.out.println("JVM smoke test passed.");
    System.out.println("- Verified fetched javac source exists: " + upstreamMain);
    System.out.println("- Generated class file: " + classFile + " (" + bytecode.length + " bytes)");

    deleteRecursively(tempRoot);
  }

  private static void deleteRecursively(Path path) throws IOException {
    if (!Files.exists(path)) {
      return;
    }
    Files.walk(path).sorted((a, b) -> b.getNameCount() - a.getNameCount()).forEach(
        p -> {
          try {
            Files.deleteIfExists(p);
          } catch (IOException e) {
            throw new RuntimeException("Failed to delete " + p, e);
          }
        });
  }
}
