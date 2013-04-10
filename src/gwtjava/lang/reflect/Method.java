package gwtjava.lang.reflect;

import java.lang.reflect.InvocationTargetException;

public class Method {
    java.lang.reflect.Method method;
    public Method(java.lang.reflect.Method method) {
        this.method = method;
    }

    public Object invoke(Object object, Object... objects) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException  {
        return method.invoke(object, objects);
    }

    public Class<?> getReturnType() {
        return method.getReturnType();
    }
}
