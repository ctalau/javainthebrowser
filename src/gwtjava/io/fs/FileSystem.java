package gwtjava.io.fs;

import gwtjava.io.File;
import gwtjava.io.IOException;

import java.io.FileNotFoundException;

public abstract class FileSystem {

    public abstract void reset() throws FileNotFoundException,
            java.io.IOException;

    public abstract void addFile(String name, String content);

    public abstract byte[] readFile(String path);

    public abstract void writeFile(String path, byte[] content, int off, int len)
            throws IOException;

    public abstract String cwd();

    public abstract File[] listFiles(String path);

    public abstract boolean exists(String name);

    public abstract boolean isFile(String path);

    public abstract boolean isDirectory(String path);

    public abstract long length(String path);


    private static FileSystem instance = new JSFileSystem();
    public static FileSystem instance() {
        return instance;
    }
}