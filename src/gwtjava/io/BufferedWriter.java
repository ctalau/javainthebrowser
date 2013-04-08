package gwtjava.io;

public class BufferedWriter extends Writer {

    private Writer writer;
    public BufferedWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return writer.toString();
    }

}
