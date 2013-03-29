package gwtjava.io;

import java.io.PrintStream;

import gwtjava.util.Locale;

public class PrintWriter extends Writer {
    private Writer w;

    public PrintWriter(Writer w, boolean flag) {
        this(w);
    }

    public PrintWriter(Writer w) {
        this.w = w;
    }

    public PrintWriter(final PrintStream ps) {
        this(ps, true);
    }

    public PrintWriter(final PrintStream ps, boolean flag) {
        this.w = new Writer() {
            @Override
            public void close() throws IOException {
                ps.close();
            }

            @Override
            public void flush() throws IOException {
                ps.flush();
            }

            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                // XXX: PrintStream.
                ps.append(new String(cbuf, off, len));
            }

            @Override
            public String getContent() {
                throw new UnsupportedOperationException();
            }

        };
    }

    @Override
    public String getContent() {
        return w.getContent();
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        try {
            w.write(cbuf, off, len);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try{
            w.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void flush() {
        try {
            w.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PrintWriter append(char c) {
        try {
            super.append(c);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public PrintWriter append(CharSequence c) {
        try {
            super.append(c);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public PrintWriter append(CharSequence c, int start, int end) {
        try {
            super.append(c);
        } catch (IOException e) {
        }
        return this;
    }

    public boolean checkError() {
        return false;
    }

    public PrintWriter format(String format, Object... args) {
        try {
            write(format);
            for (Object arg : args) {
                write(arg.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public PrintWriter format(Locale l, String format, Object... args) {
        return format(format, args);
    }

    public void print(boolean b) {
        append(String.valueOf(b));
    }

    public void print(char c) {
        append(String.valueOf(c));
    }
    public void print(char[] s) {
        append(String.valueOf(s));
    }
    public void print(double c) {
        append(String.valueOf(c));
    }
    public void print(float c) {
        append(String.valueOf(c));
    }
    public void print(int c) {
        append(String.valueOf(c));
    }
    public void print(long c) {
        append(String.valueOf(c));
    }
    public void print(Object c) {
        append(String.valueOf(c));
    }
    public void print(String c) {
        append(c);
    }

    public PrintWriter printf(Locale l, String format, Object... args) {
        format(l, format, args);
        return this;
    }

    public PrintWriter printf(String format, Object... args) {
        return printf(format, args);
    }

    public void println(boolean b) {
        append(""+b+"\n");

    }

    public void println(char c) {
        append(String.valueOf(c) + "\n");
    }
    public void println(char[] c) {
        append(String.valueOf(c) + "\n");
    }
    public void println(double c) {
        append(String.valueOf(c) + "\n");
    }
    public void println(float c) {
        append(String.valueOf(c) + "\n");
    }
    public void println(int c) {
        append(String.valueOf(c) + "\n");
    }
    public void println(long c) {
        append(String.valueOf(c) + "\n");
    }
    public void println(Object c) {
        append(String.valueOf(c) + "\n");
    }
    public void println(String c) {
        append(String.valueOf(c) + "\n");
    }
    public void println() {
        append("\n");
    }



}
