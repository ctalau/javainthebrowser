package gwtjava.io;

public abstract class Writer implements Closeable, Flushable {
    public void write(String str) throws IOException {
        write(str.toCharArray());
    }

    public void write(char[] cbuf) throws IOException {
        write(cbuf, 0, cbuf.length);
    }

    public abstract void write(char[] cbuf, int off, int len) throws IOException;

    public String getContent() {
        return null;
    }
}
