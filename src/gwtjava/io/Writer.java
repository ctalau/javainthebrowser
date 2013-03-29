package gwtjava.io;

public abstract class Writer implements Closeable, Flushable {
    public Writer append(char c) throws IOException {
        write(c);
        return this;
    }

    public Writer append(CharSequence csq) throws IOException {
        return append(csq, 0, csq.length());
    }

    public Writer append(CharSequence csq, int start, int end) throws IOException {
        if (start < 0 || end < 0 || end > csq.length() || start > end) {
            throw new IndexOutOfBoundsException();
        }
        write(csq.toString(), start, end-start);
        return this;
    }

    private static char [] cbuf = new char[1];
    public void write(int c) throws IOException {
        cbuf[0] = (char) c;
        write(cbuf, 0, 1);
    }

    public void write(String str, int off, int len) throws IOException {
        write(str.toCharArray(), off, len);
    }

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
