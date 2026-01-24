package gwtjava.io;

public abstract class OutputStream implements Closeable {
    public abstract void write(int b) throws IOException;

    public void write(byte[] ba, int start, int len) throws IOException {
        int end = start + len;
        for (int i = start; i < end; i++) {
            write(ba[i]);
        }
    }
}
