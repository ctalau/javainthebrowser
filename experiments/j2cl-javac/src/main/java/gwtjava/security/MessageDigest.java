package gwtjava.security;


public class MessageDigest {

    public MessageDigest() {
    }
    public static MessageDigest getInstance(String algorithm) {
        return new MessageDigest();
    }

    public byte[] digest() {
        return new byte[] {0xd, 0xe, 0xa, 0xd, 0xb, 0xe, 0xe, 0xf};
    }
}
