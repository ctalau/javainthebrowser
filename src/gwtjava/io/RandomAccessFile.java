package gwtjava.io;

// XXX : use DataInputStream and DataOutputStream
public class RandomAccessFile extends File implements Closeable {

    private java.io.RandomAccessFile jfile;
    public RandomAccessFile(File file, String mode) throws FileNotFoundException {
        super(file.jfile);
        try {
            jfile = new java.io.RandomAccessFile (file.jfile, mode);
        } catch (java.io.FileNotFoundException e) {
            throw new FileNotFoundException(e.getMessage());
        }
    }

    public void close() throws IOException {
        try {
            jfile.close();
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public void skipBytes(int i) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();

    }

    public void readFully(byte[] cbuf, int off, int len) throws IOException {
        try {
            jfile.readFully(cbuf, off, len);
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public void readFully(byte[] header) throws IOException {
        readFully(header, 0, header.length);
    }

    public int read(byte[] buffer, int offset, int i) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public void seek(long l) throws IOException {
        try {
            jfile.seek(l);
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public long getFilePointer() throws IOException {
        try {
            return jfile.getFilePointer();
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public int read() throws IOException {
        try {
            return jfile.read();
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public int readInt() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public void read(byte[] zfieNameBytes) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public byte readByte() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public long readLong() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public void writeLong(long zipFileLastModified) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();

    }

    public void writeInt(int size) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();

    }

    public void write(byte[] dirNameBytes) {
        // TODO Auto-generated method stub

        throw new UnsupportedOperationException();
    }

    public void writeByte(byte b) {
        // TODO Auto-generated method stub

        throw new UnsupportedOperationException();
    }

}
