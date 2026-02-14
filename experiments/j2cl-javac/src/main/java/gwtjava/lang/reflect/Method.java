package gwtjava.lang.reflect;

import java.util.Iterator;

import gwtjava.util.ServiceLoader;
import gwtjava.lang.ClassLoader;
import gwtjava.lang.reflect.InvocationTargetException;
import gwtjava.lang.IllegalAccessException;

public class Method {
    private String name;

    public Method(String name) {
        this.name = name;
        if (!name.equals("load") && !name.equals("reload") && !name.equals("iterator")) {
            throw new IllegalArgumentException(name);
        }
    }

    public Object invoke(Object object, Object... objects) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException  {
        if (name.equals("load")) {
            return ServiceLoader.load((Class<?>)objects[0], (ClassLoader)objects[1]);
        } else if (name.equals("reload")) {
            ((ServiceLoader<?>)object).reload();
            return null;
        } else if (name.equals("iterator")) {
            return ((ServiceLoader<?>)object).iterator();
        }
        throw new AssertionError();
    }

    public Class<?> getReturnType() {
        if (name.equals("load")) {
            return ServiceLoader.class;
        } else if (name.equals("reload")) {
            return Void.class;
        } else if (name.equals("iterator")) {
            return Iterator.class;
        }
        throw new AssertionError();
    }
}
