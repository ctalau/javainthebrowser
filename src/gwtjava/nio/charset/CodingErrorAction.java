package gwtjava.nio.charset;

public class CodingErrorAction {
    java.nio.charset.CodingErrorAction action;
    public CodingErrorAction(java.nio.charset.CodingErrorAction action) {
        // TODO Auto-generated constructor stub
        this.action = action;
    }

    public static final CodingErrorAction REPLACE =
            new CodingErrorAction(java.nio.charset.CodingErrorAction.REPLACE);
    public static final CodingErrorAction REPORT =
            new CodingErrorAction(java.nio.charset.CodingErrorAction.REPORT);
}
