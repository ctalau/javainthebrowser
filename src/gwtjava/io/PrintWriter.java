package gwtjava.io;


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
        System.out.println(ps);
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
                ps.print(new String(cbuf, off, len));
            }

            @Override
            public String getContent() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public java.io.PrintStream getPrintStream() {
        return null;
    }

    @Override
    public String getContent() {
        return w.getContent();
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

    @Override
    public void write(char[] cbuf, int off, int len) {
        try {
            w.write(cbuf, off, len);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public PrintWriter append(String c) {
        write(c.toCharArray(), 0, c.length());
        return this;
    }

    public void print(Object obj) {
        append(String.valueOf(obj));
    }

    public void print(String str) {
        append(str);
    }

    public void println(Object obj) {
        append(String.valueOf(obj));
        append("\n");
    }
    public void println(String str) {
        append(str);
        append("\n");
    }
    public void println() {
        append("\n");
    }
}
