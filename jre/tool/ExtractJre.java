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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ExtractJre {

    /**
     * @param args Optional: [contentsFile] [outputFile]
     *             If not provided, uses defaults: jre/jre-contents and src/gwtjava/io/fs/FileSystemContent.java
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String contentsFile = args.length > 0 ? args[0] : "jre/jre-contents";
        String outputFile = args.length > 1 ? args[1] : "src/gwtjava/io/fs/FileSystemContent.java";

        List<String> contents = getJreContents(contentsFile);
        PrintStream fout = new PrintStream(outputFile);
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
        int skipped = 0;
        for (String classPath : classNames) {
            try {
                String bytecode = emitClass(classPath);
                String statement = generatePutStatement(classPath, bytecode);
                out.println(statement);
            } catch (IOException e) {
                System.err.println("WARNING: Skipping class " + classPath + ": " + e.getMessage());
                skipped++;
            }
        }
        out.println("  }");
        out.println();
        out.println("  private static String join(String... parts) {");
        out.println("    StringBuilder sb = new StringBuilder();");
        out.println("    for (String part : parts) {");
        out.println("      sb.append(part);");
        out.println("    }");
        out.println("    return sb.toString();");
        out.println("  }");
        out.println("}");
        if (skipped > 0) {
            System.err.println("Skipped " + skipped + " classes that could not be found.");
        }
    }

    private static String generatePutStatement(String classPath, String bytecode) {
        // If the bytecode is short enough, use inline string literal
        if (bytecode.length() <= 30000) {
            return "    files.put(\"" + classPath + ".class\", \"" + bytecode + "\");";
        }
        // Otherwise, use join() method with multiple string chunks
        StringBuilder sb = new StringBuilder();
        sb.append("    files.put(\"").append(classPath).append(".class\", join(");

        int chunkSize = 30000;
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < bytecode.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, bytecode.length());
            chunks.add("\"" + bytecode.substring(i, end) + "\"");
        }

        for (int i = 0; i < chunks.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(chunks.get(i));
        }

        sb.append("));");
        return sb.toString();
    }

    private static Set<String> overriden = new HashSet<String>();
    static {
        overriden.add("sun/misc/Unsafe");
    }

    private static String emitClass(String className) throws IOException {
        InputStream is = null;

        if (overriden.contains(className)) {
            is = new FileInputStream("test-classes/" + className + ".class");
        } else {
            is = findClassInputStream(className);
        }

        if (is == null) {
            throw new IOException("Cannot find class: " + className);
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

    private static InputStream findClassInputStream(String className) throws IOException {
        String classFileName = className + ".class";

        // First, try project's custom classes in jre/ directory
        File jreClassFile = new File("jre/" + classFileName);
        if (jreClassFile.exists()) {
            return new FileInputStream(jreClassFile);
        }

        // Also try the source file path and compile if needed
        String sourceFileName = className.replace('/', File.separatorChar) + ".java";
        File sourceFile = new File("jre/" + sourceFileName);
        if (sourceFile.exists()) {
            // Source exists but not compiled class yet - try to compile it
            // For now, just try the standard compilation approach
            // This would require running javac, so we'll skip for now and let the user handle it
        }

        String javaHome = System.getProperty("java.home");

        // Try Java 9+ modules (if they exist)
        File modulesDir = new File(javaHome, "jmods");
        if (modulesDir.exists()) {
            File[] moduleFiles = modulesDir.listFiles((dir, name) -> name.endsWith(".jmod"));
            if (moduleFiles != null) {
                for (File moduleFile : moduleFiles) {
                    InputStream is = findInModule(moduleFile, classFileName);
                    if (is != null) {
                        return is;
                    }
                }
            }
        }

        // Try rt.jar (Java 8 and earlier)
        File rtJar = new File(javaHome, "lib/rt.jar");
        if (rtJar.exists()) {
            InputStream is = findInZip(rtJar, classFileName);
            if (is != null) {
                return is;
            }
        }

        // Try jdk.internal.bootloader path for newer JDKs
        File libDir = new File(javaHome, "lib");
        if (libDir.exists()) {
            File[] jarFiles = libDir.listFiles((dir, name) -> name.endsWith(".jar"));
            if (jarFiles != null) {
                for (File jarFile : jarFiles) {
                    InputStream is = findInZip(jarFile, classFileName);
                    if (is != null) {
                        return is;
                    }
                }
            }
        }

        return null;
    }

    private static InputStream findInZip(File zipFile, String entryName) throws IOException {
        ZipFile zf = new ZipFile(zipFile);
        try {
            ZipEntry entry = zf.getEntry(entryName);
            if (entry != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                InputStream is = zf.getInputStream(entry);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                is.close();
                zf.close();
                return new java.io.ByteArrayInputStream(baos.toByteArray());
            }
        } finally {
            zf.close();
        }
        return null;
    }

    private static InputStream findInModule(File moduleFile, String classFileName) throws IOException {
        // JMOD files are ZIP archives with classes in "classes/" directory
        ZipFile zf = new ZipFile(moduleFile);
        try {
            ZipEntry entry = zf.getEntry("classes/" + classFileName);
            if (entry != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                InputStream is = zf.getInputStream(entry);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                is.close();
                zf.close();
                return new java.io.ByteArrayInputStream(baos.toByteArray());
            }
        } finally {
            zf.close();
        }
        return null;
    }

    private static String hexEncode(byte [] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
