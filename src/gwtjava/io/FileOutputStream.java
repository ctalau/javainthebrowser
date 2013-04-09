package gwtjava.io;


public class FileOutputStream extends OutputStream {

    java.io.FileOutputStream jfos;
    public FileOutputStream(File file) throws FileNotFoundException {
        try {
            jfos = new java.io.FileOutputStream(file.jfile);
        } catch (java.io.FileNotFoundException e) {
            throw new FileNotFoundException(e.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        try {
            jfos.close();
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public void write(int b) throws IOException {
        try {
            jfos.write(b);
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
    }

}
