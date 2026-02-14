package gwtjava.io;

public class ByteArrayInputStream extends InputStream {
    private byte [] buf;
    private int pos, lim;

    public ByteArrayInputStream(byte[] buf) {
        this.buf = buf;
        this.pos = 0;
        this.lim = buf.length;
    }

    public ByteArrayInputStream(byte[] buf, int pos, int len) {
        this.buf = buf;
        this.pos = pos;
        this.lim = pos + len;
    }

    public int read() {
        return pos < lim ? (buf[pos++] & 0xff) : -1;
    }

    @Override
    public void close() {
    }

    @Override
    public int available() {
        return lim - pos;
    }

}
