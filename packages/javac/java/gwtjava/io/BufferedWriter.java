package gwtjava.io;

public class BufferedWriter extends Writer {

    public BufferedWriter(Writer writer) {
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void flush() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "BufferedWriter";
    }

}
