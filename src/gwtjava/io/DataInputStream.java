package gwtjava.io;

public class DataInputStream extends InputStream {

    private InputStream is;

    public DataInputStream(InputStream is) {
        this.is = is;
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    public int readInt() throws IOException {
        int a = is.read();
        int b = is.read();
        int c = is.read();
        int d = readUnsignedByte();
        return (a << 24) | (b << 16) | (c << 8) | d;
    }

    public int readUnsignedByte() throws IOException {
        int i = read();
        if (i == -1) {
            throw new IOException("End of Stream");
        }
        return i;
    }

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public long readLong() throws IOException {
        long a = readInt();
        long b = readInt() & 0x0ffffffff;
        return (a << 32) | b;
    }

    @Override
    public int read() throws IOException {
        return is.read();
    }

}
