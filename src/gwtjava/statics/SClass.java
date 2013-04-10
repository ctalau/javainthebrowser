package gwtjava.statics;

import gwtjava.lang.ClassLoader;
import gwtjava.lang.ClassNotFoundException;
import gwtjava.lang.NoSuchFieldException;
import gwtjava.lang.IllegalAccessException;
import gwtjava.lang.InstantiationException;
import gwtjava.lang.SecurityException;
import gwtjava.lang.reflect.Field;
import gwtjava.lang.reflect.Method;
import gwtjava.lang.reflect.Constructor;
import gwtjava.net.URL;

public class SClass {

    public static URL getResource(Class<?> cls, String name) {
        throw new UnsupportedOperationException();
    }

    public static Class<?> forName(String name, boolean init, ClassLoader cl)
            throws ClassNotFoundException {
        try {
            return Class.forName(name, init, cl.jcl);
        } catch (java.lang.ClassNotFoundException e) {
            throw new ClassNotFoundException(e.getMessage());
        }
    }

    public static Class<?> forName(String className)
            throws ClassNotFoundException {
        try {
            return Class.forName(className);
        } catch (java.lang.ClassNotFoundException e) {
            throw new ClassNotFoundException(e.getMessage());
        }
    }

    public static ClassLoader getClassLoader(Class<?> cls) {
        java.lang.ClassLoader cl = cls.getClassLoader();
        return cl == null ? null : new ClassLoader(cl);
    }

    public static Method getMethod(Class<?> c, String string,
            Class<?>... classes) throws NoSuchMethodException,
            SecurityException {
        for (int i = 0; i < classes.length; i++) {
            if (classes[i] == ClassLoader.class) {
                classes[i] = java.lang.ClassLoader.class;
            }
        }
        return new Method(c.getMethod(string, classes));
    }

    public static boolean isAnnotationPresent(Class<?> c, Class<?> class1) {
        return false;
    }

    public static String getSimpleName(Class<?> class1) {
        return class1.getSimpleName();
    }

    public static <T, U> Class<? extends U> asSubclass(Class<T> cls,
            Class<U> cls1) {
        return cls.asSubclass(cls1);
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

    public static boolean isInstance(Class<?> cls, Object obj) {
        return cls != null && cls.isInstance(obj);
    }

    public static <U> U cast(Class<U> cls, Object obj) {
        return cls.cast(obj);
    }

    public static String getCanonicalName(Class<?> cls) {
        return cls.getCanonicalName();
    }

    public static <T> T getAnnotation(Class<?> class1, Class<? extends T> class2) {
        return null;
    }

    public static <T> T newInstance(Class<T> loadClass)
            throws InstantiationException, IllegalAccessException {
        try {
            return loadClass.newInstance();
        } catch (java.lang.IllegalAccessException e) {
            throw new IllegalAccessException(e.getMessage());
        } catch (java.lang.InstantiationException e) {
            throw new InstantiationException(e.getMessage());
        }
    }

    public static Field getDeclaredField(Class<?> class1, String string)
            throws NoSuchFieldException, SecurityException {
        return new Field();
    }
}
