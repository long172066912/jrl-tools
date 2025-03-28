package org.jrl.tools.cache;

import org.jrl.tools.cache.config.JrlCacheConfig;

import java.io.Closeable;
import java.util.Map;
import java.util.Set;

/**
 * 缓存接口
 *
 * @author JerryLong
 */
public interface JrlCache<K, V> extends JrlCacheWithLock<K, V>, Closeable {
    /**
     * 获取缓存配置
     *
     * @return JrlCacheConfig
     */
    JrlCacheConfig<K, V> getConfig();

    /**
     * 获取缓存，如果拿不到，会通过loader进行加载
     *
     * @param key 缓存的key
     * @return 缓存的value
     */
    V get(K key);

    /**
     * 获取缓存，如果存在的话，返回缓存的value，否则返回null
     *
     * @param key 缓存的key
     * @return 缓存的value
     */
    V getIfPresent(K key);

    /**
     * 重新刷新缓存，通过CacheLoader
     *
     * @param key 缓存的key
     * @return 缓存的value
     */
    V refresh(K key);

    /**
     * 判断缓存是否存在
     *
     * @param key key
     * @return true存在，false不存在
     */
    boolean exists(K key);

    /**
     * 设置缓存
     *
     * @param key   key
     * @param value value
     */
    void put(K key, V value);

    /**
     * 删除缓存
     *
     * @param key key
     */
    void remove(K key);

    /**
     * 批量获取缓存
     *
     * @param keys key列表
     * @return 缓存map，V可能是null
     */
    Map<K, V> getAll(Set<K> keys);

    /**
     * 批量设置缓存
     *
     * @param map map
     */
    void putAll(Map<K, V> map);

    /**
     * 批量删除缓存
     *
     * @param keys key集合
     */
    void removeAll(Set<K> keys);
}
