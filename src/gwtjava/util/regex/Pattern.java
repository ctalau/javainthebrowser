package gwtjava.util.regex;

public class Pattern {

    public Matcher matcher(String line) {
        return new Matcher();
    }

    public static Pattern compile(String pattern) {
        return new Pattern();
    }
}
