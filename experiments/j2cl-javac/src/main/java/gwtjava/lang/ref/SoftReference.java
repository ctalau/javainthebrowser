package gwtjava.lang.ref;

/**
 * Port of the gwtjava.lang.ref.SoftReference class.
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