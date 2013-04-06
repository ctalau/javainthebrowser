package gwtjava.nio.charset;

import gwtjava.nio.ByteBuffer;
import gwtjava.nio.CharBuffer;

public class CharsetDecoder {
    CodingErrorAction onMalformed = CodingErrorAction.REPORT;
    CodingErrorAction onUnmappableChar = CodingErrorAction.REPORT;

    public float averageCharsPerByte() {
        return 2.0f;
    }

    public float maxCharsPerByte() {
        return 6.0f;
    }

    public CoderResult decode(ByteBuffer inbuf, CharBuffer dest, boolean b) {
        // XXX: UTF8 Charset
        String converted = new String(inbuf.array(), inbuf.position(), inbuf.remaining());
        if (dest.remaining() < converted.length()) {
            return new CoderResult(converted.length(), false, true);
        } else {
            dest.put(converted.toCharArray(), 0, converted.length());
        }
        return new CoderResult(converted.length(), false, false);
    }

    public CharsetDecoder onMalformedInput(CodingErrorAction action) {
        this.onMalformed = action;
        return this;
    }

    public CharsetDecoder onUnmappableCharacter(CodingErrorAction action) {
        this.onUnmappableChar = action;
        return this;
    }

}
