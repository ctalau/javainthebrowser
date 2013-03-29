package port.java.lang.ref;

/**
 * Port of the java.lang.ref.SoftReference class.
 *
 * It is just a regular reference.
 *
 * @author ctalau
 */
public class SoftReference<T> extends Reference<T> {
    final private T object;
    public SoftReference(T object) {
        this.object = object;
    }

    public T get() {
        return object;
    }
}