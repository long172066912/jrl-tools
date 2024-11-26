package org.jrl.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JrlLruCacheTest {

    @Test
    void get() {
        JrlLruCache<String, String> cache = new JrlLruCache<>(5);
        for (int i = 0; i < 10; i++) {
            cache.put(String.valueOf(i), String.valueOf(i));
        }
        assertEquals(5, cache.size());
    }
}