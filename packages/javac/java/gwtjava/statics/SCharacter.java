package gwtjava.statics;

public class SCharacter {
    public static boolean isWhitespace(char ch) {
        return ch == ' ' || ch == '\t';
    }

    public static boolean isJavaIdentifierStart(int ch) {
        return ch == '_' || ch == '$' || ('a' <= ch && ch <= 'z')
                || ('A' <= ch && ch <= 'Z');
    }

    public static boolean isJavaIdentifierStart(char ch) {
        return isJavaIdentifierStart((int)ch);
    }

    public static boolean isJavaIdentifierPart(int ch) {
        return isJavaIdentifierStart(ch) || ('0' <= ch && ch <= '9');
    }

    public static boolean isJavaIdentifierPart(char ch) {
        return isJavaIdentifierPart((int)ch);
    }
}
