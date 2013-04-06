package gwtjava.nio.charset;


public class Charset {
    java.nio.charset.Charset charset;
    public Charset(java.nio.charset.Charset charset) {
        this.charset = charset;
    }

    public String name() {
        // TODO Auto-generated method stub
        return charset.name();
    }

    public static Charset forName(String encodingName) {
        // TODO Auto-generated method stub
        return new Charset(java.nio.charset.Charset.forName(encodingName));
    }

    public CharsetDecoder newDecoder() {
        // TODO Auto-generated method stub
        return new CharsetDecoder(charset.newDecoder());
    }

}
