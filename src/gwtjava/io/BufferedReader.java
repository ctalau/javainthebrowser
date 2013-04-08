package gwtjava.io;

public class BufferedReader extends Reader {

    private Reader delegate;
    public BufferedReader(Reader inputStreamReader) {
        this.delegate = inputStreamReader;
    }

    public String readLine() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void close() throws IOException {
        delegate.close();
    }

}
