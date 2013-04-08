package gwtjava.lang;

import gwtjava.io.InputStream;
import gwtjava.io.PrintStream;


public class System {

    public static PrintStream err = java.lang.System.err;
    public static PrintStream out = java.lang.System.out;
    public static InputStream in = java.lang.System.in;

    public static long currentTimeMillis() {
        return java.lang.System.currentTimeMillis();
    }

    public static void arraycopy(Object src, int offsrc, Object dst, int offdst, int len) {
        java.lang.System.arraycopy(src, offsrc, dst, offdst, len);
    }

    public static String getProperty(String key) {
        return java.lang.System.getenv(key);
    }

    public static void exit(int code) {
        java.lang.System.exit(code);
    }

}

