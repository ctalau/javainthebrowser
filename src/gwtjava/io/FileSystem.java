package gwtjava.io;

import gwtjava.lang.System;

import java.io.FileNotFoundException;
import gwtjava.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

public class FileSystem {

    private static final Map<String, byte[]> files = new HashMap<String, byte[]>();

    static {
        try {
            reset();
            addFile(new java.io.File("Basic.java"));
            addFile(new java.io.File("Error.java"));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public static void reset() throws FileNotFoundException,
            java.io.IOException {
        String[] paths = System.getProperty("java.class.path").split(
                File.pathSeparator);
        for (String path : paths) {
            addFiles(new java.io.File(path));
        }
    }

    /** Add files in a subtree to the list of files. */
    private static void addFiles(java.io.File file)
            throws FileNotFoundException, java.io.IOException {
        if (file.isFile()) {
            addFile(file);
        } else {
            for (java.io.File child : file.listFiles()) {
                addFiles(child);
            }
        }
    }

    private static void addFile(java.io.File file)
            throws FileNotFoundException, java.io.IOException {
        byte[] content = new byte[(int) file.length()];
        new java.io.RandomAccessFile(file, "r").readFully(content);
        files.put(canonical(file.getAbsolutePath()), content);
    }

    static byte[] readFile(String path) {
        return files.get(path);
    }

    static void writeFile(String path, byte[] content, int off, int len)
            throws IOException {
        try {
            java.io.FileOutputStream jfos = new java.io.FileOutputStream(path);
            jfos.write(content, off, len);
            jfos.close();
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    static File[] listFiles(String path) {
        HashSet<File> children = new HashSet<File>();
        for (String fileName : files.keySet()) {
            if (fileName.startsWith(path)) {
                String relPath = fileName.substring(path.length() + 1);
                int index = relPath.indexOf(File.separatorChar);
                if (index > 0) {
                    children.add(new File(path + File.separator
                            + relPath.substring(0, index)));
                } else {
                    children.add(new File(fileName));
                }
            }
        }
        return children.toArray(new File[0]);
    }

    static boolean exists(String name) {
        for (String fileName : files.keySet()) {
            if (fileName.startsWith(name)) {
                return true;
            }
        }
        return false;
    }

    static boolean isFile(String path) {
        return files.containsKey(path);
    }

    static boolean isDirectory(String path) {
        return exists(path) && !isFile(path);
    }

    static long length(String path) {
        return files.get(path).length;
    }

    static String canonical(String path) {
        String[] parts = path.split(File.separator);
        Stack<String> stack = new Stack<String>();
        for (String part : parts) {
            if (part.equals(".") || part.length() == 0) {
                continue;
            } else if (part.equals("..")) {
                stack.pop();
            } else {
                stack.push(part);
            }
        }

        StringBuffer ret = new StringBuffer();
        for (String part : stack) {
            ret.append(File.separator);
            ret.append(part);
        }
        return ret.toString();
    }

    static String cwd() {
        return new java.io.File(".").getAbsolutePath();
    }
}
