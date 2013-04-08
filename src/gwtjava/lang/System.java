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
            throw new UnsupportedOperationException();
        }

    }
    public static PrintStream err = new JPrintStream(); //XXX: java.lang.System.err;
    public static PrintStream out = new JPrintStream(); //XXX: java.lang.System.out;
    public static InputStream in = new InputStream() {
        @Override
        public int read() throws IOException {
            throw new UnsupportedOperationException();
        }
    };


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

