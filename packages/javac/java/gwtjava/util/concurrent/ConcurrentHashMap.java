package gwtjava.util.concurrent;

import java.util.HashMap;

public class ConcurrentHashMap<K, V> extends HashMap<K, V> implements ConcurrentMap<K, V> {
    private static final long serialVersionUID = -7576106429142010483L;

    public ConcurrentHashMap(int capacity) {
        super(capacity);
    }

    public ConcurrentHashMap() {
    }

    public V putIfAbsent(K key, V value) {
        V ret = null;
        if (this.containsKey(key)) {
            ret = get(key);
        }
        this.put(key, value);
        return ret;
    }
}
