package org.jrl.tools.cache.config;

import org.jrl.tools.cache.JrlCacheLoader;
import org.jrl.tools.cache.model.JrlCacheType;

/**
 * 缓存配置抽象类
 *
 * @author JerryLong
 */
public abstract class AbstractJrlCacheConfig<K, V> implements JrlCacheConfig<K, V> {
    /**
     * 缓存名称
     */
    private final String name;
    /**
     * 缓存类型
     */
    private JrlCacheType cacheType;
    /**
     * 缓存加载器
     */
    private JrlCacheLoader<K, V> cacheLoader;
    /**
     * 是否缓存空值，默认缓存空值
     */
    private boolean cacheNullValue = true;
    /**
     * 过期策略
     */
    private JrlCacheExpireConfig expireConfig;

    protected AbstractJrlCacheConfig(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public JrlCacheType getCacheType() {
        return cacheType;
    }

    public void setCacheType(JrlCacheType cacheType) {
        this.cacheType = cacheType;
    }

    @Override
    public JrlCacheLoader<K, V> getCacheLoader() {
        return cacheLoader;
    }

    public void setCacheLoader(JrlCacheLoader<K, V> cacheLoader) {
        this.cacheLoader = cacheLoader;
    }

    @Override
    public boolean cacheNullValue() {
        return cacheNullValue;
    }

    public void setCacheNullValue(boolean cacheNullValue) {
        this.cacheNullValue = cacheNullValue;
    }

    public boolean isCacheNullValue() {
        return cacheNullValue;
    }

    @Override
    public JrlCacheExpireConfig expire() {
        return expireConfig;
    }

    public void setExpireConfig(JrlCacheExpireConfig expireConfig) {
        this.expireConfig = expireConfig;
    }
}
