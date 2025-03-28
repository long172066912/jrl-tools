package org.jrl.tools.cache.config;

import org.jrl.tools.cache.JrlCacheLoader;
import org.jrl.tools.cache.model.JrlCacheLockControlType;
import org.jrl.tools.cache.model.JrlCacheType;

/**
 * 缓存配置
 *
 * @author JerryLong
 */
public interface JrlCacheConfig<K, V> {
    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    String name();

    /**
     * 获取缓存是否缓存空值
     *
     * @return true 缓存空值
     */
    boolean cacheNullValue();

    /**
     * 获取缓存类型
     *
     * @return 缓存类型
     */
    JrlCacheType getCacheType();

    /**
     * 获取缓存加载器
     *
     * @return JrlCacheLoader
     */
    JrlCacheLoader<K, V> getCacheLoader();

    /**
     * 获取锁类型
     *
     * @return 锁类型
     */
    JrlCacheLockControlType lockType();

    /**
     * 获取过期配置
     *
     * @return 过期配置
     */
    JrlCacheExpireConfig expire();
}
