package gwtjava.nio;


public class CharBuffer extends Buffer implements CharSequence {
    char [] array;
    private CharBuffer(int capacity) {
        super(capacity);
        array = new char[capacity];
    }

    private CharBuffer(char [] array, int capacity) {
        super(capacity);
        this.array = array;
    }


    public void put(char c) {
        array[position++] = c;
    }

    public CharBuffer put(CharBuffer flip) {
        return put(flip.array(), flip.position, flip.remaining());
    }

    public CharBuffer put(char[] src, int off, int len) {
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


    public boolean hasArray() {
        return true;
    }

    public CharBuffer compact() {
      int rem = remaining();
      for (int i = 0; i < rem; i++) {
              array[i] = array[position + i];
      }

      position = limit - position;
      limit = capacity;
      mark = UNSET_MARK;
      return this;
  }

    public char[] array() {
        return array;
    }

    @Override
    public char charAt(int index) {
        return array[position + index];
    }

    @Override
    public int length() {
        return remaining();
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        // TODO Auto-generated method stub
//        throw new UnsupportedOperationException();
        return new String(array, start, end);
    }

    public static CharBuffer allocate(int capacity) {
        return new CharBuffer(capacity);
    }

    public static CharBuffer wrap(char[] charArray, int off, int length) {
        return new CharBuffer(charArray, length);
    }

}
