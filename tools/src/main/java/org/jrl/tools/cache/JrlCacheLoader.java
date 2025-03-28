package org.jrl.tools.cache;

import org.jrl.tools.cache.model.JrlCacheLoadType;
import org.jrl.tools.utils.function.AbstractJrlFunction;

import java.util.List;

/**
 * 缓存数据加载器
 *
 * @author JerryLong
 */
public interface JrlCacheLoader<K, V> {
    /**
     * 获取缓存加载器
     *
     * @return 缓存加载器
     */
    AbstractJrlFunction<K, V> getCacheLoader();

    /**
     * 加载缓存
     *
     * @param key 过期的key
     * @return 缓存值
     */
    V load(K key);

    /**
     * 获取加载缓存的方式
     *
     * @return 加载缓存的方式
     */
    JrlCacheLoadType loadType();

    /**
     * 需要懒加载的key列表
     *
     * @param keys 待加载的key列表
     */
    void loadKeys(List<K> keys);

    /**
     * 获取需要懒加载的key列表
     *
     * @return 需要懒加载的key列表
     */
    List<K> getLoadKeys();
}
