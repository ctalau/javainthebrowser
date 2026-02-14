package gwtjava.statics;

public class SArrays {

    public static int[] copyOf(int[] array, int newSize) {
        int [] newArray = new int[newSize];
        System.arraycopy(array, 0, newArray, 0, Math.min(newSize, array.length));
        return newArray;
    }

    public static Object newInstance(Class<?> elemType, int size) {
        throw new UnsupportedOperationException();
    }
    public static void set(Object array, int index, Object value) {
        throw new UnsupportedOperationException();
    }
}
