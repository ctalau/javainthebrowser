package gwtjava.nio;

public class ByteBuffer {
    public java.nio.ByteBuffer buffer;
    public ByteBuffer(java.nio.ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public float remaining() {
        // TODO Auto-generated method stub
        return buffer.remaining();
    }

    public int position() {
        // TODO Auto-generated method stub
        return buffer.position();
    }

    public void position(int i) {
        // TODO Auto-generated method stub
        buffer.position(i);
    }

    public ByteBuffer flip() {
        // TODO Auto-generated method stub
        return new ByteBuffer((java.nio.ByteBuffer) buffer.flip());
    }

    public byte[] array() {
        // TODO Auto-generated method stub
        return buffer.array();
    }

    public static ByteBuffer allocate(int i) {
        // TODO Auto-generated method stub
        return new ByteBuffer(java.nio.ByteBuffer.allocate(i));
    }

    public ByteBuffer clear() {
        // TODO Auto-generated method stub
        return new ByteBuffer((java.nio.ByteBuffer) buffer.clear());
    }

    public int capacity() {
        // TODO Auto-generated method stub
        return buffer.capacity();
    }

    public ByteBuffer put(ByteBuffer flip) {
        // TODO Auto-generated method stub
        return new ByteBuffer((java.nio.ByteBuffer) buffer.put(flip.buffer));
    }

}
