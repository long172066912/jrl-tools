package org.jrl.tools.cache.hotkey;

import org.jrl.tools.utils.hotkey.DefaultJrlHotKey;

import java.util.Objects;

/**
 * JrlCache 热key统计抽象类
 *
 * @author JerryLong
 */
public abstract class JrlCacheHotKey<K> extends DefaultJrlHotKey<K> {
    /**
     * 缓存名称
     */
    private final String cacheName;

    public JrlCacheHotKey(String cacheName, K key) {
        super(key);
        this.cacheName = cacheName;
    }

    public String getCacheName() {
        return cacheName;
    }

    /**
     * 重写equals方法，比较缓存名称和key
     * 如果key非字符串，请重写key的equals方法
     *
     * @param o
     * @return
     */
    @Override
    public abstract boolean equals(Object o);

    @Override
    public int hashCode() {
        return Objects.hash(cacheName, getKey());
    }
}
