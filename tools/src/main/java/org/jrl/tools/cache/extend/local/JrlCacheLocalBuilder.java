package org.jrl.tools.cache.extend.local;

import org.jrl.tools.cache.JrlCache;
import org.jrl.tools.cache.JrlCacheLoader;
import org.jrl.tools.cache.config.DefaultJrlCacheExpireConfig;
import org.jrl.tools.cache.config.JrlCacheExpireConfig;
import org.jrl.tools.cache.core.JrlCacheManager;

/**
 * 本地缓存构建器
 *
 * @author JerryLong
 */
public class JrlCacheLocalBuilder<K, V> {
    private final String name;
    private final JrlCacheLocalConfig<K, V> cacheConfig;

    public JrlCacheLocalBuilder(String name) {
        this.name = name;
        this.cacheConfig = new JrlCacheLocalConfig<>(this.name);
    }

    public JrlCacheLocalBuilder<K, V> initialCapacity(Integer initialCapacity) {
        this.cacheConfig.setInitialCapacity(initialCapacity);
        return this;
    }

    public JrlCacheLocalBuilder<K, V> maxSize(Integer maxSize) {
        this.cacheConfig.setMaxSize(maxSize);
        return this;
    }

    /**
     * 是否缓存空值
     *
     * @param cacheNullValue true，则可以缓存控制
     * @return JrlCacheLocalBuilder
     */
    public JrlCacheLocalBuilder<K, V> cacheNullValue(boolean cacheNullValue) {
        this.cacheConfig.setCacheNullValue(cacheNullValue);
        return this;
    }

    /**
     * 缓存加载器，可通过 CacheLoadBuilder 构建
     *
     * @param cacheLoader 缓存数据加载
     * @return JrlCacheLocalBuilder
     */
    public JrlCacheLocalBuilder<K, V> cacheLoader(JrlCacheLoader<K, V> cacheLoader) {
        this.cacheConfig.setCacheLoader(cacheLoader);
        return this;
    }

    public JrlCacheLocalBuilder<K, V> expire(JrlCacheExpireConfig expire) {
        this.cacheConfig.setExpireConfig(expire);
        return this;
    }

    public JrlCache<K, V> build() {
        if (this.cacheConfig.getCacheLoader() == null) {
            throw new IllegalArgumentException("cacheLoader is null");
        }
        if (this.cacheConfig.expire() == null) {
            cacheConfig.setExpireConfig(DefaultJrlCacheExpireConfig.oneMinute());
        }
        if (this.cacheConfig.getInitialCapacity() == null) {
            cacheConfig.setInitialCapacity(100);
        }
        if (this.cacheConfig.getMaxSize() == null) {
            cacheConfig.setMaxSize(5000);
        }
        return JrlCacheManager.getCache(name, () -> new JrlCacheCaffeineImpl<>(name, cacheConfig));
    }
}