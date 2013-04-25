package tool;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExtractJre {

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        List<String> contents = getJreContents("jre/jre-contents");
        PrintStream fout = new PrintStream("src/gwtjava/io/fs/FileSystemContent.java");
        emitJavaSource(contents, fout);
        fout.close();
    }

    private static List<String> getJreContents(String contentsFile) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(contentsFile));
        List<String> ret = new ArrayList<String>();

        String className = null;
        while ((className = in.readLine()) != null) {
            ret.add(className);
        }

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

    private static ClassLoader cl = ClassLoader.getSystemClassLoader();
    private static Set<String> overriden = new HashSet<String>();
    static {
        overriden.add("sun/misc/Unsafe");
    }
    private static String emitClass(String className) throws IOException {
        InputStream is;

        is = cl.getResourceAsStream(className + ".class");
        if (overriden.contains(className)) {
            is = new FileInputStream("test-classes/" + className + ".class");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        try {
            while ((length = is.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            is.close();
        }
        return hexEncode(baos.toByteArray());
    }

    private static String hexEncode(byte [] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
