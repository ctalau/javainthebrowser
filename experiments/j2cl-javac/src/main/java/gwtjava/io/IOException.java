package gwtjava.io;

public class IOException extends Exception {
    private static final long serialVersionUID = 4470152601623612878L;

    public IOException(String msg) {
        super(msg);
    }

    public IOException() {
    }

    public IOException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
