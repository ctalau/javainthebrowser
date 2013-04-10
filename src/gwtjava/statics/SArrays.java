package gwtjava.statics;

import java.lang.reflect.Array;
import java.util.Arrays;

public class SArrays {

    public static int[] copyOf(int[] array, int newSize) {
        return Arrays.copyOf(array, newSize);
    }
    public static Object newInstance(Class<?> elemType, int size) {
        return Array.newInstance(elemType, size);
    }
    public static void set(Object array, int index, Object value) {
        Array.set(array, index, value);
    }
}
