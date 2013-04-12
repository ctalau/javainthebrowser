package gwtjava.io;

import gwtjava.io.fs.FileSystem;

public class FileInputStream extends ByteArrayInputStream {
    private static FileSystem fs = FileSystem.instance();

    public FileInputStream(File file) throws FileNotFoundException {
        super(fs.readFile(file.getAbsolutePath()));
    }
}
