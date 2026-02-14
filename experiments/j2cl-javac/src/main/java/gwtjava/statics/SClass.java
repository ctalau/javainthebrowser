package gwtjava.statics;

import gwtjava.lang.ClassLoader;
import gwtjava.lang.ClassNotFoundException;
import gwtjava.lang.NoSuchFieldException;
import gwtjava.lang.NoSuchMethodException;
import gwtjava.lang.IllegalAccessException;
import gwtjava.lang.InstantiationException;
import gwtjava.lang.SecurityException;
import gwtjava.lang.reflect.Field;
import gwtjava.lang.reflect.Method;
import gwtjava.lang.reflect.Constructor;
import gwtjava.net.URL;
import gwtjava.util.ServiceLoader;

public class SClass {
    public static ClassLoader getClassLoader(Class<?> cls) {
        return new ClassLoader();
    }

    public static Class<?> forName(String className)
            throws ClassNotFoundException {
        if (!className.equals("gwtjava.util.ServiceLoader")) {
            throw new IllegalArgumentException(className);
        }
        return ServiceLoader.class;
    }

    public static Method getMethod(Class<?> c, String string,
            Class<?>... classes) throws NoSuchMethodException,
            SecurityException {
        return new Method(string);
    }

    public static Class<?> forName(String name, boolean init, ClassLoader cl)
            throws ClassNotFoundException {
        throw new UnsupportedOperationException();
    }

    public static Field getDeclaredField(Class<?> class1, String string)
            throws NoSuchFieldException, SecurityException {
        return new Field();
    }

    public static <T, U> Class<? extends U> asSubclass(Class<T> cls,
            Class<U> cls1) {
        throw new UnsupportedOperationException();
    }

    public static <T> T newInstance(Class<T> loadClass)
            throws InstantiationException, IllegalAccessException {
        throw new UnsupportedOperationException();
    }

    public static URL getResource(Class<?> cls, String name) {
        throw new UnsupportedOperationException();
    }

    public static <T> Constructor<? extends T> getConstructor(Class<T> loader,
            Class<?>... constrArgTypes) {
        throw new UnsupportedOperationException();
    }

    public static boolean isAssignableFrom(Class<?> class1, Class<?> class2) {
        return false;
    }

    public static boolean isAnnotation(Class<?> cls) {
        return false;
    }

    public static <T> T getAnnotation(Class<?> class1, Class<? extends T> class2) {
        return null;
    }

    public static boolean isAnnotationPresent(Class<?> c, Class<?> class1) {
        return false;
    }

    public static String getSimpleName(Class<?> class1) {
        int split = class1.getName().lastIndexOf('$');
        return class1.getName().substring(split + 1);
    }

    public static boolean isInstance(Class<?> cls, Object obj) {
        throw new UnsupportedOperationException();
    }

    public static <U> U cast(Class<U> cls, Object obj) {
        throw new UnsupportedOperationException();
    }

    public static String getCanonicalName(Class<?> cls) {
        throw new UnsupportedOperationException();
    }

}
