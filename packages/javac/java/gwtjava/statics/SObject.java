package gwtjava.statics;

import java.util.ArrayList;

public class SObject {

    public static <T> T[] clone(T[] table, T[] empty) {
        ArrayList<T> list = new ArrayList<T>();
        for (T elt : table) {
            list.add(elt);
        }
        return list.toArray(empty);
    }

    public static int[] clone(int[] table) {
        return SArrays.copyOf(table, table.length);
    }

}
