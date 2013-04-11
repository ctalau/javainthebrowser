package gwtjava.io;

public class FileInputStream extends ByteArrayInputStream {
    public FileInputStream(File file) throws FileNotFoundException {
        super(FileSystem.readFile(file.getAbsolutePath()));
    }
}
