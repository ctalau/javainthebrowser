package gwtjava.statics;

public class SCharacter {
    public static boolean isWhitespace(char ch) {
        return Character.isWhitespace(ch);
    }

    public static boolean isJavaIdentifierStart(int ch) {
        return Character.isJavaIdentifierStart(ch);
    }

    public static boolean isJavaIdentifierStart(char ch) {
        return Character.isJavaIdentifierStart(ch);
    }

    public static boolean isJavaIdentifierPart(int ch) {
        return Character.isJavaIdentifierPart(ch);
    }

    public static boolean isJavaIdentifierPart(char ch) {
        return Character.isJavaIdentifierPart(ch);
    }
}
