package gwtjava.statics;

import gwtjava.util.Locale;

import java.util.Arrays;

public class SString {
    public static java.lang.String format(java.lang.String fmt, Object... args) {
        return fmt + ".format(" + Arrays.toString(args) + ")";
    }

    public static java.lang.String format(Locale l, java.lang.String fmt, Object... args) {
        return fmt + ".format(" + Arrays.toString(args) + ")";
    }

}
