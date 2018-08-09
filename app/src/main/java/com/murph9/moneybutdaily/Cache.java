package com.murph9.moneybutdaily;

import java.util.HashMap;

public class Cache<K, T> {

    private HashMap<K, T> cache;

    public Cache() {
        cache = new HashMap<>();
    }

    public T get(K key) {
        if (cache.containsKey(key))
            return cache.get(key);
        return null;
    }

    public boolean set(K key, T value) {
        return cache.put(key, value) != null;
    }
}
