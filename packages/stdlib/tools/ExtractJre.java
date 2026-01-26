package tool;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Extracts JRE class bytecode and generates FileSystemContent.java.
 *
 * Usage: java tool.ExtractJre <jre-contents-file> <override-classes-dir> <output-file>
 *
 * - jre-contents-file: Path to the file listing classes to include
 * - override-classes-dir: Path to directory containing custom override .class files
 * - output-file: Path where FileSystemContent.java will be written
 */
public class ExtractJre {

    private static ClassLoader cl = ClassLoader.getSystemClassLoader();
    private static Set<String> overriden = new HashSet<String>();
    private static String overrideDir;

    static {
        // Classes that have custom implementations
        overriden.add("java/io/ConsolePrintStream");
        overriden.add("java/io/ConsolePrintStream$ConsoleOutputStream");
        overriden.add("sun/misc/Unsafe");
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("Usage: java tool.ExtractJre <jre-contents-file> <override-classes-dir> <output-file>");
            System.exit(1);
        }

        String contentsFile = args[0];
        overrideDir = args[1];
        String outputFile = args[2];

        List<String> contents = getJreContents(contentsFile);

        // Ensure output directory exists
        File outputDir = new File(outputFile).getParentFile();
        if (outputDir != null && !outputDir.exists()) {
            outputDir.mkdirs();
        }

        PrintStream fout = new PrintStream(outputFile);
        emitJavaSource(contents, fout);
        fout.close();

        System.out.println("Generated " + outputFile + " with " + contents.size() + " classes");
    }

    private static List<String> getJreContents(String contentsFile) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(contentsFile));
        List<String> ret = new ArrayList<String>();

        String className = null;
        while ((className = in.readLine()) != null) {
            String trimmed = className.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                ret.add(trimmed);
            }
        }
        in.close();

        return ret;
    }

    private static void emitJavaSource(List<String> classNames, PrintStream out)
            throws IOException {
        out.println("package gwtjava.io.fs;");
        out.println("import java.util.HashMap;");
        out.println("class FileSystemContent {");
        out.println("  public static HashMap<String, String> files = new HashMap<String, String>();");
        out.println("  static {");
        for (String classPath : classNames) {
            String bytecode = emitClass(classPath);
            out.println("    files.put(\"" + classPath + ".class\", \"" + bytecode + "\");");
        }
        out.println("  }");
        out.println("}");
    }

    private static String emitClass(String className) throws IOException {
        InputStream is = null;

        // Check if this class has a custom override
        if (overriden.contains(className)) {
            File overrideFile = new File(overrideDir, className + ".class");
            if (overrideFile.exists()) {
                is = new FileInputStream(overrideFile);
            } else {
                throw new IOException("Override class not found: " + overrideFile.getAbsolutePath());
            }
        } else {
            // Load from system classloader (JRE)
            is = cl.getResourceAsStream(className + ".class");
            if (is == null) {
                throw new IOException("Class not found in JRE: " + className);
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        try {
            while ((length = is.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
        } finally {
            is.close();
        }
        return hexEncode(baos.toByteArray());
    }

    private static String hexEncode(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
