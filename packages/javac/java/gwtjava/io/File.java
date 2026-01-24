package gwtjava.io;

import gwtjava.io.fs.FileSystem;
import gwtjava.net.URI;
import gwtjava.net.URISyntaxException;

import java.util.Stack;

public class File {
    public static char separatorChar = '/';
    public static final String separator = "/";
    public static final String pathSeparator = ":";
    public static final char pathSeparatorChar = ':';

    private String absolutePath;
    private static FileSystem fs = FileSystem.instance();

    public File(String name) {
        this((File) null, name);
    }

    public File(String parent, String name) {
        this(new File(parent), name);
    }

    public File(File parent, String name) {
        if (name.startsWith(separator)) {
            this.absolutePath = name;
        } else if (parent == null) {
            this.absolutePath = canonical(fs.cwd() + separator + name);
        } else {
            this.absolutePath = canonical(parent.getAbsolutePath() + separator + name);
        }
    }

    public File[] listFiles() {
        return fs.listFiles(absolutePath);
    }

    public boolean isFile() {
        return fs.isFile(absolutePath);
    }

    public boolean isDirectory() {
        return fs.isDirectory(absolutePath);
    }

    public boolean exists() {
        return fs.exists(absolutePath);
    }

    public long length() {
        return fs.length(absolutePath);
    }

    public File getParentFile() {
        return new File(
                absolutePath.substring(0, absolutePath.lastIndexOf('/')));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof File) {
            return absolutePath.equals(((File) obj).absolutePath);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return absolutePath.hashCode();
    }

    public String getPath() {
        return absolutePath;
    }

    public URI toURI() {
        try {
            return new URI("file:" + absolutePath);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public String getName() {
        return absolutePath.substring(absolutePath.lastIndexOf('/') + 1);
    }

    public File getCanonicalFile() throws IOException {
        return new File(absolutePath);
    }

    @Override
    public String toString() {
        return absolutePath;
    }

    public static String canonical(String path) {
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

    public boolean isAbsolute() {
        throw new UnsupportedOperationException();
    }

    public String getCanonicalPath() throws IOException {
        throw new UnsupportedOperationException();
    }

    public boolean canWrite() {
        throw new UnsupportedOperationException();
    }

    public File getAbsoluteFile() {
        return new File(this.getAbsolutePath());
    }

    public boolean mkdirs() {
        throw new UnsupportedOperationException();
    }

    public boolean delete() {
        throw new UnsupportedOperationException();
    }

    public String getParent() {
        throw new UnsupportedOperationException();
    }

    public long lastModified() {
        throw new UnsupportedOperationException();
    }
}
