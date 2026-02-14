package gwtjava.util;

public class StringTokenizer {
    private String [] tokens;
    private int crtToken;

    public StringTokenizer(String string, String delim) {
        tokens = string.split(delim);
    }

    public StringTokenizer(String string) {
        this(string, " |\t|\r|\n|\f");
    }

    public boolean hasMoreTokens() {
        return crtToken < tokens.length;
    }

    public String nextToken() {
        return tokens[crtToken++];
    }

    public int countTokens() {
        return tokens.length;
    }

}
