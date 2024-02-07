package org.alist.hub.bean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 实现一个简易的Redis Map 用于存放有有效期的数据
 *
 * @param <K>
 * @param <V>
 */
public class ExpiringMap<K, V> {

    private final Map<K, V> map = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public void put(K key, V value, long expirationTimeMillis) {
        map.put(key, value);

        if (expirationTimeMillis > 0) {
            scheduleExpiration(key, expirationTimeMillis);
        }
    }

    public V get(K key) {
        return map.get(key);
    }

    public void remove(K key) {
        map.remove(key);
    }

    private void scheduleExpiration(K key, long expirationTimeMillis) {
        executor.schedule(() -> {
            remove(key);
        }, expirationTimeMillis, TimeUnit.MILLISECONDS);
    }

}
