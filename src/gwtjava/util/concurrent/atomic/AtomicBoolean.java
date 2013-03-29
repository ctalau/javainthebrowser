package gwtjava.util.concurrent.atomic;

public class AtomicBoolean {
    boolean wrapped = false;

    public AtomicBoolean() {
    }

    public boolean getAndSet(boolean value) {
        boolean ret = wrapped;
        wrapped = value;
        return ret;
    }

    public boolean get() {
        return wrapped;
    }
}
