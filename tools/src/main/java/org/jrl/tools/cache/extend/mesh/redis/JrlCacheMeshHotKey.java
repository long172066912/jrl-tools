package org.jrl.tools.cache.extend.mesh.redis;

import org.jrl.tools.cache.hotkey.JrlCacheHotKey;

import java.util.Objects;

/**
 * 分布式热键包装类，重写了equals方法
 *
 * @author JerryLong
 */
public class JrlCacheMeshHotKey<K> extends JrlCacheHotKey<K> {

    public JrlCacheMeshHotKey(String cacheName, K key) {
        super(cacheName, key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JrlCacheMeshHotKey)) return false;
        JrlCacheMeshHotKey<K> hotKey = (JrlCacheMeshHotKey<K>) o;
        if (!this.getCacheName().equals(hotKey.getCacheName())) {
            return false;
        }
        //判断key是否是 JrlCacheRedisKeyBuilder
        String key;
        String key1;
        if (this.getKey() instanceof JrlCacheRedisKeyBuilder) {
            key = ((JrlCacheRedisKeyBuilder) this.getKey()).build();
            key1 = ((JrlCacheRedisKeyBuilder) hotKey.getKey()).build();
        } else {
            key = this.getKey().toString();
            key1 = hotKey.getKey().toString();
        }
        return Objects.equals(key, key1);
    }
}
