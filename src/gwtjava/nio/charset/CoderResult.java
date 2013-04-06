package gwtjava.nio.charset;

public class CoderResult {
    private boolean codingError;
    private boolean overflow;
    private int length;

    public CoderResult(int length, boolean codingError, boolean overflow) {
        this.codingError = codingError;
        this.overflow = overflow;
        this.length = length;
    }

    public boolean isMalformed() {
        return codingError;
    }

    public boolean isUnmappable() {
        return codingError;
    }

    public boolean isOverflow() {
        return overflow;
    }

    public boolean isUnderflow() {
        return !(codingError || overflow);
    }

    public int length() {
        return length;
    }

}
