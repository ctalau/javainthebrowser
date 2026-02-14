package gwtjava.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Collections {

    public static <T> List<T> emptyList() {
        return java.util.Collections.emptyList();
    }

    public static <T> Set<T> emptySet() {
        return java.util.Collections.emptySet();
    }

    public static <K, V> Map<K, V> emptyMap() {
        return java.util.Collections.emptyMap();
    }


    public static <K, V> Map<K, V> synchronizedMap(Map<K, V> map) {
        return map;
    }

    public static <T> Set<T> synchronizedSet(Set<T> set) {
        return set;
    }

    public static <T> List<T> synchronizedList(List<T> list) {
        return list;
    }


    public static <T> Collection<T> unmodifiableCollection(Collection<? extends T> col) {
        return java.util.Collections.unmodifiableCollection(col);
    }

    public static <T> List<T> unmodifiableList(List<? extends T> col) {
        return java.util.Collections.unmodifiableList(col);
    }

    public static <K, V> Map<K, V> unmodifiableMap(Map<? extends K, ? extends V> col) {
        return java.util.Collections.unmodifiableMap(col);
    }

    public static <T> Set<T> unmodifiableSet(Set<? extends T> col) {
        return java.util.Collections.unmodifiableSet(col);
    }

    public static <T> int binarySearch(List<? extends Comparable<? super T>> list, T key) {
        return java.util.Collections.binarySearch(list, key);
    }

    public static final Set<?> EMPTY_SET = java.util.Collections.EMPTY_SET;
}
