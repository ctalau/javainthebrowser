package gwtjava.io;

public interface PrintStream extends Closeable, Flushable {
    @Override
    public void close() ;

    @Override
    public void flush() throws IOException ;

    public void println(Object string) ;

    public void print(Object string) ;

    public void println() ;
}
