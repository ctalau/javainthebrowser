package gwtjava.io;


public class FileOutputStream extends ByteArrayOutputStream {

    private File file;
    public FileOutputStream(File file) throws FileNotFoundException {
        this.file = file;
    }

    @Override
    public void close() throws IOException {
        try {
            java.io.FileOutputStream jfos = new java.io.FileOutputStream(file.jfile);
            jfos.write(this.buf, 0, this.count);
            jfos.close();
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
    }
}
