package gwtjava.io;

public abstract class InputStream {

    public int available() {
        return 0;
    }

    public int read(byte[] buf, int start, int len) throws IOException {

        int end = start + len;
        for (int i = start; i < end; i++) {
            int r = read();
            if (r == -1) {
                return i == start ? -1 : i - start;
            }
            buf[i] = (byte) r;
        }
        return len;
    }

    public int read(byte[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    public void close() throws IOException {

    }

    public abstract int read() throws IOException;

}
