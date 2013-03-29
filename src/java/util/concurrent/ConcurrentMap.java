package java.util.concurrent;

import java.util.Map;

public interface ConcurrentMap<K, V> extends Map<K, V> {
    public V putIfAbsent(K key, V value);
}
