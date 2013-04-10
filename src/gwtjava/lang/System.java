package gwtjava.lang;

import gwtjava.io.IOException;
import gwtjava.io.InputStream;
import gwtjava.io.PrintStream;


public class System {

    private static class JPrintStream implements PrintStream {

        @Override
        public void close() {
            java.lang.System.out.close();
        }

        @Override
        public void flush() throws IOException {
            java.lang.System.out.flush();
        }

        @Override
        public void println(Object string) {
            java.lang.System.out.println(string);
        }

        @Override
        public void print(Object string) {
            java.lang.System.out.print(string);
        }

        @Override
        public void println() {
            java.lang.System.out.println();
        }

        @Override
        public java.io.PrintStream getPrintStream() {
            return java.lang.System.err;
//XXX            throw new UnsupportedOperationException();
        }

    }

    public static void arraycopy(Object src, int offsrc, Object dst, int offdst, int len) {
        java.lang.System.arraycopy(src, offsrc, dst, offdst, len);
    }

    public static PrintStream err = new JPrintStream();
    public static PrintStream out = new JPrintStream();
    public static InputStream in = null ;


    public static String getProperty(String key) {
        if (key.equals("sun.boot.class.path")) {
            return "/usr/lib/jvm/java-7-openjdk-amd64/jre/lib/rt.jar";
        } else if (key.equals("java.specification.version")) {
            return "1.7";
        } else if (key.equals("java.class.path")) {
            return "../../build/toolclasses/";
        }
        return null;
    }

    public static void exit(int code) {
        java.lang.System.exit(code);
    }

    public static long currentTimeMillis() {
        return 0;
    }
}

