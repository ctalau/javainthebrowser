package gwtjava.io;

public class RandomAccessFile extends File implements Closeable {

    public RandomAccessFile(File parent, String name) {
        super(parent, name);
    }

    public RandomAccessFile(String name) {
        super(name);
    }

    public void close() throws IOException {
        // TODO Auto-generated method stub

    }

    public void skipBytes(int i) {
        // TODO Auto-generated method stub

    }

    public void readFully(byte[] cbuf, int i, int csize) {
        // TODO Auto-generated method stub

    }

    public int read(byte[] buffer, int offset, int i) {
        // TODO Auto-generated method stub
        return 0;
    }

    public void seek(long l) {
        // TODO Auto-generated method stub

    }

    public void readFully(byte[] header) {
        // TODO Auto-generated method stub

    }

    public long getFilePointer() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int read() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int readInt() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void read(byte[] zfieNameBytes) {
        // TODO Auto-generated method stub

    }

    public byte readByte() {
        // TODO Auto-generated method stub
        return 0;
    }

    public long readLong() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void writeLong(long zipFileLastModified) {
        // TODO Auto-generated method stub

    }

    public void writeInt(int size) {
        // TODO Auto-generated method stub

    }

    public void write(byte[] dirNameBytes) {
        // TODO Auto-generated method stub

    }

    public void writeByte(byte b) {
        // TODO Auto-generated method stub

    }

}
