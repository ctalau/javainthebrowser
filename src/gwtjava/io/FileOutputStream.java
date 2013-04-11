package gwtjava.io;


public class FileOutputStream extends ByteArrayOutputStream {

    private File file;
    public FileOutputStream(File file) throws FileNotFoundException {
        this.file = file;
    }

    @Override
    public void close() throws IOException {
        FileSystem.writeFile(file.getAbsolutePath(), buf, 0, count);
    }
}
