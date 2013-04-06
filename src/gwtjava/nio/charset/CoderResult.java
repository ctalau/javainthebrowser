package gwtjava.nio.charset;

public class CoderResult {
    java.nio.charset.CoderResult result;
    public CoderResult(java.nio.charset.CoderResult result) {
        // TODO Auto-generated constructor stub
        this.result = result;
    }

    public boolean isMalformed() {
        // TODO Auto-generated method stub
        return result.isMalformed();
    }

    public boolean isUnmappable() {
        // TODO Auto-generated method stub
        return result.isUnmappable();
    }

    public boolean isOverflow() {
        // TODO Auto-generated method stub
        return result.isOverflow();
    }

    public boolean isUnderflow() {
        // TODO Auto-generated method stub
        return result.isUnderflow();
    }

    public int length() {
        // TODO Auto-generated method stub
        return result.length();
    }

}
