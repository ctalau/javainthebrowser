package gwtjava.io;

public abstract class PrintStream implements Closeable, Flushable {
    @Override
    public abstract void close() ;

    @Override
    public abstract void flush() throws IOException ;

    public abstract void print(Object string) ;

    public void println() {
        print("\n");
    }

    public void println(Object string) {
        print(string);
        println();
    }

}
