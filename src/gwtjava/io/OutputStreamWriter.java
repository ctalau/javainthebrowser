package gwtjava.io;

public class OutputStreamWriter extends Writer {

    public OutputStreamWriter(OutputStream outputStream) {
    }

    public OutputStreamWriter(OutputStream outputStream,
            String encodingName) {
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flush() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        throw new UnsupportedOperationException();
    }

    public String getEncoding() {
        return "UTF8";
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException();
    }

}
