package gwtjava.security;

import java.io.InputStream;

public class DigestInputStream {
    public DigestInputStream(InputStream is, MessageDigest md) {
    }

    public int read(byte[] buf) {
        return buf.length;
    }

    public void close() {
    }
}
