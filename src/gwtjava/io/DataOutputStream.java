package gwtjava.io;

import gwtjava.io.OutputStream;

public class DataOutputStream extends OutputStream {

    private OutputStream os;
    public DataOutputStream(OutputStream os) {
        this.os = os;
    }

    @Override
    public void write(int b) throws IOException {
        os.write(b);
    }

    public void writeInt(int v) throws IOException {
        os.write(v >> 24);
        os.write(v >> 16);
        os.write(v >> 8);
        os.write(v);
    }

    public void writeFloat(float x) throws IOException {
        writeInt(Float.floatToIntBits(x));
    }

    public void writeLong(long v) throws IOException {
        writeInt((int) (v >> 32L));
        writeInt((int) v);
    }

    public void writeDouble(double x) throws IOException {
        writeLong(Double.doubleToRawLongBits(x));
    }

    @Override
    public void close() throws IOException {
        os.close();
    }

}
