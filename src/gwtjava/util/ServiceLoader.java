package gwtjava.util;

import java.util.Iterator;
import gwtjava.lang.ClassLoader;

public class ServiceLoader<S> {

    public void reload() {

    }

    public static <S> ServiceLoader<S>  load(Class<S> service, ClassLoader cl) {
        return new ServiceLoader<S>();
    }
    public Iterator<S> iterator() {
        return new Iterator<S>() {

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public S next() {
                return null;
            }

            @Override
            public void remove() {
            }
        };

    }
}
