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
        return new CoderResult(decoder.decode(inbuf.buffer, dest.buffer, b));
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
