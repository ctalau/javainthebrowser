package gwtjava.nio.charset;


public class Charset {
    private Charset() {
    }

    public String name() {
        return "UTF8";
    }

    public static Charset forName(String encodingName) {
        assert (encodingName.equals("UTF8"));
        return new Charset();
    }

    public CharsetDecoder newDecoder() {
        return new CharsetDecoder();
    }

}
