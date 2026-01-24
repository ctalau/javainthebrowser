package gwtjava.io;

import gwtjava.io.IOException;
import gwtjava.io.OutputStream;

public class ByteArrayOutputStream extends OutputStream {
    protected int count;
    protected byte[] buf;

    public ByteArrayOutputStream() {
        this(16);
    }

    public ByteArrayOutputStream(int initialSize) {
        buf = new byte[initialSize];
    }

    @Override
    public void write(int b) {
        if (buf.length == count) {
            byte[] newBuf = new byte[buf.length * 3 / 2];
            System.arraycopy(buf, 0, newBuf, 0, count);
            buf = newBuf;
        }

        buf[count++] = (byte) b;
    }

    public byte[] toByteArray() {
        byte[] result = new byte[count];
        System.arraycopy(buf, 0, result, 0, count);
        return result;
    }

    @Override
    public void close() throws IOException {

    }

}
