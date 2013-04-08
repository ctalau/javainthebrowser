package gwtjava.io;


public class FileInputStream extends InputStream {

    java.io.FileInputStream jfis;
    java.io.File jfile;
    public FileInputStream(File file) throws FileNotFoundException {
        jfile = file.jfile;
        try {
            jfis = new java.io.FileInputStream(file.jfile);
        } catch (java.io.FileNotFoundException e) {
            throw new FileNotFoundException(e.getMessage());
        }
    }

    int count = 0;
    @Override
    public int read() throws IOException {
        try {
            count++;
            return jfis.read();
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public int available() {
        return (int) jfile.length() - count;
    }

}
