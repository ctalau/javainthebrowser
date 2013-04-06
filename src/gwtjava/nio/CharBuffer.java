package gwtjava.nio;


public class CharBuffer implements CharSequence {
    public java.nio.CharBuffer buffer;

    public CharBuffer(java.nio.CharBuffer buffer) {
        this.buffer = buffer;
    }

    public int limit() {
        // TODO Auto-generated method stub
        return buffer.limit();
    }

    public int capacity() {
        // TODO Auto-generated method stub
        return buffer.capacity();
    }

    public CharBuffer flip() {
        // TODO Auto-generated method stub
        return new CharBuffer((java.nio.CharBuffer)buffer.flip());
    }

    public void position(int limit) {
        // TODO Auto-generated method stub
        buffer.position(limit);
    }

    public void limit(int capacity) {
        // TODO Auto-generated method stub
        buffer.limit(capacity);
    }

    public void put(char c) {
        // TODO Auto-generated method stub
        buffer.put(c);
    }

    public ByteBuffer put(ByteBuffer flip) {
        // TODO Auto-generated method stub
        //return buffer.put(flip.buffer);
        return null;
    }

    public static CharBuffer allocate(int i) {
        // TODO Auto-generated method stub
        return new CharBuffer(java.nio.CharBuffer.allocate(i));
    }

    public CharBuffer put(CharBuffer dest) {
        // TODO Auto-generated method stub
        return new CharBuffer(buffer.put(dest.buffer));
    }

    public boolean hasArray() {
        // TODO Auto-generated method stub
        return buffer.hasArray();
    }

    public CharBuffer compact() {
        // TODO Auto-generated method stub
        return new CharBuffer(buffer.compact());
    }

    public char[] array() {
        // TODO Auto-generated method stub
        return buffer.array();
    }

    public static CharBuffer wrap(char[] charArray, int i, int length) {
        // TODO Auto-generated method stub
        return new CharBuffer(java.nio.CharBuffer.wrap(charArray, i, length));
    }

    @Override
    public char charAt(int index) {
        // TODO Auto-generated method stub
        return buffer.charAt(index);
    }

    @Override
    public int length() {
        // TODO Auto-generated method stub
        return buffer.length();
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        // TODO Auto-generated method stub
        return buffer.subSequence(start, end);
    }
}
