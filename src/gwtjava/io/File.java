package gwtjava.io;

import gwtjava.net.URI;
import gwtjava.net.URISyntaxException;

public class File {
    public static char separatorChar = '/';
    public static final String separator = "/";
    public static final String pathSeparator = ":";
    public static final char pathSeparatorChar = ':';

    private String absolutePath;

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
            this.absolutePath = FileSystem.canonical(FileSystem.cwd() + separator + name);
        } else {
            this.absolutePath = FileSystem.canonical(parent.getAbsolutePath() + separator + name);
        }
    }

    public File[] listFiles() {
        return FileSystem.listFiles(absolutePath);
    }

    public boolean isFile() {
        return FileSystem.isFile(absolutePath);
    }

    public boolean isDirectory() {
        return FileSystem.isDirectory(absolutePath);
    }

    public boolean exists() {
        return FileSystem.exists(absolutePath);
    }

    public long length() {
        return FileSystem.length(absolutePath);
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
