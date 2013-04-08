package gwtjava.util.zip;

public class Inflater {

    java.util.zip.Inflater jinfl;
    public Inflater(boolean b) {
        jinfl = new java.util.zip.Inflater(b);
    }

    public void reset() {
        jinfl.reset();
    }

    public void setInput(byte[] src) {
        jinfl.setInput(src);
    }

    public int inflate(byte[] dest) throws DataFormatException {
        try {
            return jinfl.inflate(dest);
        } catch (java.util.zip.DataFormatException e) {
            throw new DataFormatException(e.getMessage());
        }
    }

}
