package org.jrl.tools.cache.extend.both;

import org.jrl.tools.cache.JrlCacheLoader;
import org.jrl.tools.cache.config.JrlCacheConfig;
import org.jrl.tools.cache.config.JrlCacheExpireConfig;
import org.jrl.tools.cache.model.JrlCacheLockControlType;
import org.jrl.tools.cache.model.JrlCacheType;

/**
 * 缓存配置，通过配置，可以配置缓存类型等等
 *
 * @author JerryLong
 */
public class JrlCacheBothConfig<K, V> implements JrlCacheConfig<K, V> {
    private final String name;
    private final JrlCacheConfig<K, V> meshConfig;
    private final JrlCacheConfig<K, V> localConfig;

    public JrlCacheBothConfig(String name, JrlCacheConfig<K, V> localConfig, JrlCacheConfig<K, V> meshConfig) {
        this.name = name;
        this.localConfig = localConfig;
        this.meshConfig = meshConfig;
    }


    @Override
    public String name() {
        return this.name;
    }

    @Override
    public boolean cacheNullValue() {
        return localConfig.cacheNullValue() || meshConfig.cacheNullValue();
    }

    @Override
    public JrlCacheType getCacheType() {
        return JrlCacheType.BOTH;
    }

    @Override
    public JrlCacheLoader<K, V> getCacheLoader() {
        return meshConfig.getCacheLoader();
    }

    @Override
    public JrlCacheLockControlType lockType() {
        return meshConfig.lockType();
    }

    @Override
    public JrlCacheExpireConfig expire() {
        return meshConfig.expire();
    }
}
