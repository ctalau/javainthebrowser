package gwtjava.io;

public class FilterWriter extends Writer {
    protected Writer out;
    public FilterWriter(Writer openWriter) {
        throw new UnsupportedOperationException();
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

    @Override
    public String toString() {
        throw new UnsupportedOperationException();
    }

}
