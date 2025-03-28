package org.jrl.tools.cache.extend.mesh;

import org.jrl.tools.cache.JrlCache;
import org.jrl.tools.cache.JrlCacheLoader;
import org.jrl.tools.cache.config.DefaultJrlCacheExpireConfig;
import org.jrl.tools.cache.config.JrlCacheExpireConfig;
import org.jrl.tools.cache.core.JrlCacheManager;
import org.jrl.tools.cache.extend.BaseJrlCache;
import org.jrl.tools.cache.extend.mesh.redis.JrlCacheRedisStringImpl;
import org.jrl.tools.cache.hotkey.JrlCacheHotKeyConfig;

/**
 * 本地缓存构建器
 *
 * @author JerryLong
 */
public class JrlCacheMeshBuilder<K, V> {
    private final String name;
    private final JrlCacheMeshConfig<K, V> cacheConfig;

    public JrlCacheMeshBuilder(String name, String cacheSource, JrlCacheLockConfig lockConfig) {
        this.name = name;
        this.cacheConfig = new JrlCacheMeshConfig<>(this.name, cacheSource, lockConfig);
    }

    /**
     * 是否缓存空值
     *
     * @param cacheNullValue 是否缓存控制
     * @return this
     */
    public JrlCacheMeshBuilder<K, V> cacheNullValue(boolean cacheNullValue) {
        this.cacheConfig.setCacheNullValue(cacheNullValue);
        return this;
    }

    /**
     * 缓存加载器，可通过 CacheLoadBuilder 构建
     *
     * @param cacheLoader 缓存加载器
     * @return this
     */
    public JrlCacheMeshBuilder<K, V> cacheLoader(JrlCacheLoader<K, V> cacheLoader) {
        this.cacheConfig.setCacheLoader(cacheLoader);
        return this;
    }

    public JrlCacheMeshBuilder<K, V> expire(JrlCacheExpireConfig expire) {
        this.cacheConfig.setExpireConfig(expire);
        return this;
    }

    public JrlCacheMeshBuilder<K, V> connectType(JrlCacheMeshConnectType connectType) {
        this.cacheConfig.setConnectType(connectType);
        return this;
    }

    public JrlCacheMeshBuilder<K, V> nullValue(V nullValue) {
        if (nullValue == null) {
            throw new IllegalArgumentException("nullValue is null");
        }
        this.cacheConfig.setNullValue(nullValue);
        return this;
    }

    /**
     * 开启热点key统计，默认统计数量是 1000
     *
     * @param stat      是否开启热key统计
     * @param autoCache 是否开启热key自动本地缓存
     * @return
     */
    public JrlCacheMeshBuilder<K, V> hotKey(boolean stat, boolean autoCache) {
        final JrlCacheHotKeyConfig jrlCacheHotKeyConfig = this.cacheConfig.getJrlCacheHotKeyConfig();
        jrlCacheHotKeyConfig.setStatHotKey(stat);
        jrlCacheHotKeyConfig.setAutoCacheHotKey(autoCache);
        return this;
    }

    /**
     * 开启热点key统计，默认统计数量是 1000
     *
     * @param stat      是否开启热key统计
     * @param autoCache 是否开启热key自动本地缓存
     * @return
     */
    public JrlCacheMeshBuilder<K, V> hotKey(boolean stat, boolean autoCache, int localCacheSeconds) {
        if (localCacheSeconds < 1) {
            throw new IllegalArgumentException("localCacheSeconds must be greater than 1");
        }
        final JrlCacheHotKeyConfig jrlCacheHotKeyConfig = this.cacheConfig.getJrlCacheHotKeyConfig();
        jrlCacheHotKeyConfig.setStatHotKey(stat);
        jrlCacheHotKeyConfig.setAutoCacheHotKey(autoCache);
        jrlCacheHotKeyConfig.setStatSeconds(localCacheSeconds);
        return this;
    }

    /**
     * 开启自动缓存热点key
     *
     * @param stat            是否开启热key统计
     * @param autoCache       是否开启热key自动本地缓存
     * @param statSeconds     统计时间，单位是秒，默认60秒，不能少于1秒
     * @param capacity        容量，默认50
     * @param countLeastValue 热key统计周期内最小值，默认1000
     * @return
     */
    public JrlCacheMeshBuilder<K, V> hotKey(boolean stat, boolean autoCache, int statSeconds, int capacity, int countLeastValue, int localCacheSeconds) {
        if (statSeconds < 1) {
            throw new IllegalArgumentException("statSeconds must be greater than 1");
        }
        if (localCacheSeconds < 1) {
            throw new IllegalArgumentException("localCacheSeconds must be greater than 1");
        }
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be greater than 1");
        }
        if (countLeastValue < 10) {
            throw new IllegalArgumentException("countLeastValue must be greater than 1");
        }
        final JrlCacheHotKeyConfig jrlCacheHotKeyConfig = this.cacheConfig.getJrlCacheHotKeyConfig();
        jrlCacheHotKeyConfig.setStatHotKey(stat);
        jrlCacheHotKeyConfig.setAutoCacheHotKey(autoCache);
        jrlCacheHotKeyConfig.setStatSeconds(statSeconds);
        jrlCacheHotKeyConfig.setCapacity(capacity);
        jrlCacheHotKeyConfig.setCountLeastValue(countLeastValue);
        jrlCacheHotKeyConfig.setLocalCacheSeconds(localCacheSeconds);
        return this;
    }

    /**
     * 开启异常加载，默认为false
     * 如果开启，缓存异常时，会打异常日志，返回load中的数据
     * @return
     */
    public JrlCacheMeshBuilder<K, V> loadWithException() {
        this.cacheConfig.setLoadWithException(true);
        return this;
    }

    public JrlCache<K, V> build() {
        if (this.cacheConfig.getCacheLoader() == null) {
            throw new IllegalArgumentException("cacheLoader is null");
        }
        if (this.cacheConfig.expire() == null) {
            //默认过期策略，1天 + 1天内的随机时间
            cacheConfig.setExpireConfig(DefaultJrlCacheExpireConfig.oneDay(new JrlCacheExpireConfig.ExpireRandom(0L, 86400L)));
        }
        final BaseJrlCache<K, V> cache = JrlCacheManager.getCache(name,
                () -> new JrlCacheRedisStringImpl<>(name, cacheConfig, cacheConfig.getJrlCacheHotKeyConfig())
        );
        //开启热key消费
        cache.subscribeHotKey();
        return cache;
    }
}