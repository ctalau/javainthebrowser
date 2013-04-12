package gwtjava.io;

import gwtjava.io.fs.FileSystem;


public class FileOutputStream extends ByteArrayOutputStream {

    private static FileSystem fs = FileSystem.instance();

    private File file;
    public FileOutputStream(File file) throws FileNotFoundException {
        this.file = file;
    }

    @Override
    public void close() throws IOException {
        fs.writeFile(file.getAbsolutePath(), buf, 0, count);
    }
}
