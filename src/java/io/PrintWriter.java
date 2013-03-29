package java.io;

import java.util.Locale;

public class PrintWriter extends Writer {
    private Writer w;

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
    public void write(char[] cbuf, int off, int len) throws IOException {
        w.write(cbuf, off, len);
    }

    @Override
    public void close() throws IOException {
        w.close();
    }

    @Override
    public void flush() throws IOException {
        w.flush();
    }

    public PrintWriter append(char c) throws IOException {
        super.append(c);
        return this;
    }

    public PrintWriter append(CharSequence c) throws IOException {
        super.append(c);
        return this;
    }

    public PrintWriter append(CharSequence c, int start, int end) throws IOException {
        super.append(c);
        return this;
    }

    public boolean checkError() throws IOException {
        return false;
    }

    public PrintWriter format(String format, Object... args) throws IOException {
        write(format);
        for (Object arg : args) {
            write(arg.toString());
        }
        return this;
    }

    public PrintWriter format(Locale l, String format, Object... args) throws IOException {
        return format(format, args);
    }

    public void print(boolean b) throws IOException {
        append(String.valueOf(b));
    }

    public void print(char c) throws IOException {
        append(String.valueOf(c));
    }
    public void print(char[] s) throws IOException {
        append(String.valueOf(s));
    }
    public void print(double c) throws IOException {
        append(String.valueOf(c));
    }
    public void print(float c) throws IOException {
        append(String.valueOf(c));
    }
    public void print(int c) throws IOException {
        append(String.valueOf(c));
    }
    public void print(long c) throws IOException {
        append(String.valueOf(c));
    }
    public void print(Object c) throws IOException {
        append(String.valueOf(c));
    }
    public void print(String c) throws IOException {
        append(c);
    }

    public PrintWriter printf(Locale l, String format, Object... args) throws IOException {
        format(l, format, args);
        return this;
    }

    public PrintWriter printf(String format, Object... args) throws IOException {
        return printf(format, args);
    }

    public void println(boolean b) throws IOException {
        append(""+b+"\n");

    }

    public void println(char c) throws IOException {
        append(String.valueOf(c) + "\n");
    }
    public void println(char[] c) throws IOException {
        append(String.valueOf(c) + "\n");
    }
    public void println(double c) throws IOException {
        append(String.valueOf(c) + "\n");
    }
    public void println(float c) throws IOException {
        append(String.valueOf(c) + "\n");
    }
    public void println(int c) throws IOException {
        append(String.valueOf(c) + "\n");
    }
    public void println(long c) throws IOException {
        append(String.valueOf(c) + "\n");
    }
    public void println(Object c) throws IOException {
        append(String.valueOf(c) + "\n");
    }
    public void println(String c) throws IOException {
        append(String.valueOf(c) + "\n");
    }
    public void println() throws IOException {
        append("\n");
    }



}
