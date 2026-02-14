package gwtjava.nio;

public class ByteBuffer extends Buffer {
    private byte [] array;

    public ByteBuffer(int capacity) {
        super(capacity);
        array = new byte[capacity];
    }

    public byte[] array() {
        return array;
    }

    public ByteBuffer put(ByteBuffer flip) {
        return put(flip.array(), flip.position, flip.remaining());
    }

    private ByteBuffer put(byte[] src, int off, int len) {
        if (off < 0 || len < 0 || (long)off + (long)len > src.length) {
            throw new IndexOutOfBoundsException();
        }
        if (len > remaining()) {
            throw new IndexOutOfBoundsException();
        }
        for (int i = 0; i < len; i++) {
            array[position++] = src[off++];
        }
        return this;
    }

    public static ByteBuffer allocate(int capacity) {
        // TODO Auto-generated method stub
        return new ByteBuffer(capacity);
    }

}
