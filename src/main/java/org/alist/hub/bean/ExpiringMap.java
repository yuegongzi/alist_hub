package org.alist.hub.bean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
