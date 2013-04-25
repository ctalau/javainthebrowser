package gwtjava.lang;

import gwtjava.io.IOException;
import gwtjava.io.InputStream;
import gwtjava.io.PrintStream;


public class System {

    /** In script mode, it does nothing, and in bytecode mode it outputs to console. */
    private static class JPrintStream extends PrintStream {
        @Override
        public void close() {
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void print(Object string) {
            java.lang.System.out.print(string);
        }
    }

    public static void arraycopy(Object src, int offsrc, Object dst, int offdst, int len) {
        java.lang.System.arraycopy(src, offsrc, dst, offdst, len);
    }

    public static PrintStream err = new JPrintStream();
    public static PrintStream out = new JPrintStream();
    public static InputStream in = null ;


    public static void setOut(PrintStream ps) {
        out = ps;
    }

    public static void setErr(PrintStream ps) {
        err = ps;
    }

    public static String getProperty(String key) {
        if (key.equals("sun.boot.class.path")) {
            return "";
        } else if (key.equals("java.specification.version")) {
            return "1.7";
        } else if (key.equals("java.class.path")) {
            return "/jre/";
        }
        return null;
    }

    public static void exit(int code) {
    }

    public static long currentTimeMillis() {
        return 0;
    }
}

