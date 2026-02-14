package gwtjava.io;

public class StringWriter extends Writer {

    StringBuffer buf;
    public StringWriter() {
        this.buf = new StringBuffer();
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        buf.append(cbuf, off, len);
    }

    @Override
    public String toString() {
        return buf.toString();
    }
}
