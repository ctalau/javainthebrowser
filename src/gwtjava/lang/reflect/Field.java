package gwtjava.lang.reflect;

public class Field {
    java.lang.reflect.Field declaredField;
    public Field(java.lang.reflect.Field declaredField) {
        this.declaredField = declaredField;
    }

    public boolean isAccessible() {
        return declaredField.isAccessible();
    }

    public void setAccessible(boolean b) {
        declaredField.setAccessible(b);
    }

    public Object get(Object o) throws IllegalArgumentException, IllegalAccessException {
        return declaredField.get(o);
    }

}
