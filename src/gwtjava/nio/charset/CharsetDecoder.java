package gwtjava.nio.charset;

import gwtjava.nio.ByteBuffer;
import gwtjava.nio.CharBuffer;

public class CharsetDecoder {
    java.nio.charset.CharsetDecoder decoder;
    public CharsetDecoder(java.nio.charset.CharsetDecoder decoder) {
        // TODO Auto-generated constructor stub
        this.decoder = decoder;
    }

    public float averageCharsPerByte() {
        // TODO Auto-generated method stub
        return decoder.averageCharsPerByte();
    }

    public float maxCharsPerByte() {
        // TODO Auto-generated method stub
        return decoder.maxCharsPerByte();
    }

    public CoderResult decode(ByteBuffer inbuf, CharBuffer dest, boolean b) {
        // TODO Auto-generated method stub
        java.nio.CharBuffer javadest = java.nio.CharBuffer.allocate(4 * inbuf.capacity());
        CoderResult result = new CoderResult(decoder.decode(
                java.nio.ByteBuffer.wrap(inbuf.array(), inbuf.position(), inbuf.remaining()),
                javadest,
                b));
        javadest.flip();
        dest.put(javadest.array(), javadest.arrayOffset(), javadest.length());
        return result;
    }

    public CharsetDecoder onMalformedInput(CodingErrorAction action) {
        // TODO Auto-generated method stub
        return new CharsetDecoder(decoder.onMalformedInput(action.action));
    }

    public CharsetDecoder onUnmappableCharacter(CodingErrorAction action) {
        // TODO Auto-generated method stub
        return new CharsetDecoder(decoder.onUnmappableCharacter(action.action));
    }

}
