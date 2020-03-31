package com.murph9.moneybutdaily.service;

import java.util.HashMap;

public class Cache<K, T> {

    private HashMap<K, T> cache;

    Cache() {
        cache = new HashMap<>();
    }

    public T get(K key) {
        if (cache.containsKey(key))
            return cache.get(key);
        return null;
    }

    void set(K key, T value) {
        cache.put(key, value);
    }
}
