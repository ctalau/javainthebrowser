package gwtjava.io;

// XXX : use DataInputStream and DataOutputStream
public class RandomAccessFile implements Closeable {
    private java.io.RandomAccessFile jfile;
    public RandomAccessFile(File file, String mode) throws FileNotFoundException {
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

    public void skipBytes(int i) throws IOException {
        try {
            jfile.skipBytes(i);
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
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

    public int read(byte[] buffer, int offset, int i) throws IOException {
        try {
            return jfile.read(buffer, offset, i);
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
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

    public int readInt() throws IOException {
        try {
            return jfile.readInt();
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public void read(byte[] buf) throws IOException {
        try {
            jfile.read(buf);
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public byte readByte() throws IOException {
        try {
            return jfile.readByte();
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public long readLong() throws IOException {
        try {
            return jfile.readLong();
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public void writeLong(long v) throws IOException {
        try {
            jfile.writeLong(v);
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }

    }

    public void writeInt(int v) throws IOException {
        try {
            jfile.writeInt(v);
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public void write(byte[] buf) throws IOException {
        try {
            jfile.write(buf);
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public void writeByte(byte b) throws IOException {
        try {
            jfile.writeByte(b);
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }

    }

    public long length() throws IOException {
        try {
            return jfile.length();
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
    }

}
