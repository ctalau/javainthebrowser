package gwtjava.io;

public abstract class OutputStream implements Closeable {
    public abstract void write(int b) throws IOException ;

    public void write(byte[] elems, int i, int length) throws IOException {
        // TODO Auto-generated method stub

    }

}
