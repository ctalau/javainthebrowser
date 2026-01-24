package gwtjava.lang.annotation;

import gwtjava.lang.reflect.Method;

@SuppressWarnings("serial")
public class AnnotationTypeMismatchException extends RuntimeException {

    public AnnotationTypeMismatchException(Method method, String string) {
        super(string);
    }

}
