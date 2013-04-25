package java.io;

import java.io.IOException;
import java.io.OutputStream;

public class ConsolePrintStream extends PrintStream {

    private static class ConsoleOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
            write0(b);
        }

        public static native void write0(int b) throws IOException;
    }

    public ConsolePrintStream() {
        super(new ConsoleOutputStream());
    }

}
