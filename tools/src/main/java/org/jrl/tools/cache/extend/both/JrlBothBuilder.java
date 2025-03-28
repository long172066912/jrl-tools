package org.jrl.tools.cache.extend.both;

import org.jrl.tools.cache.JrlCache;
import org.jrl.tools.cache.config.DefaultJrlCacheExpireConfig;
import org.jrl.tools.cache.config.JrlCacheExpireConfig;
import org.jrl.tools.cache.core.JrlCacheManager;
import org.jrl.tools.cache.core.builder.CacheLoadBuilder;
import org.jrl.tools.cache.core.builder.JrlCacheBuilder;
import org.jrl.tools.cache.extend.mesh.JrlCacheLockConfig;
import org.jrl.tools.cache.extend.mesh.JrlCacheMeshConnectType;
import org.jrl.tools.cache.model.JrlCacheLoadType;
import org.jrl.tools.cache.model.JrlCacheType;
import org.jrl.tools.utils.function.AbstractJrlFunction;

import java.util.Collections;
import java.util.List;

/**
 * 多级缓存构建器
 *
 * @author JerryLong
 */
public class JrlBothBuilder<K, V> {

    private String name;
    private String cacheSource;
    private JrlCacheMeshConnectType connectType;
    private AbstractJrlFunction<K, V> cacheLoader;
    private JrlCacheLockConfig lockConfig;
    private JrlCacheExpireConfig meshExpireConfig;
    private JrlCacheExpireConfig localExpireConfig;
    private List<K> preLoadKeys;
    private JrlCacheLoadType loadType;

    public JrlBothBuilder(String name, String cacheSource, AbstractJrlFunction<K, V> cacheLoader,
                          JrlCacheLockConfig lockConfig,
                          JrlCacheExpireConfig meshExpireConfig) {
        this.name = name;
        this.cacheSource = cacheSource;
        this.connectType = JrlCacheMeshConnectType.NORMAL;
        this.cacheLoader = cacheLoader;
        this.lockConfig = lockConfig;
        this.meshExpireConfig = meshExpireConfig;
        this.localExpireConfig = DefaultJrlCacheExpireConfig.oneMinute();
        this.loadType = JrlCacheLoadType.EXPIRED;
        this.preLoadKeys = Collections.emptyList();
    }

    public JrlBothBuilder<K, V> connectType(JrlCacheMeshConnectType connectType) {
        this.connectType = connectType;
        return this;
    }

    public JrlBothBuilder<K, V> localExpire(JrlCacheExpireConfig localExpireConfig) {
        this.localExpireConfig = localExpireConfig;
        return this;
    }

    public JrlBothBuilder<K, V> loadType(JrlCacheLoadType loadType) {
        this.loadType = loadType;
        return this;
    }

    public JrlBothBuilder<K, V> preLoadKeys(List<K> preLoadKeys) {
        this.preLoadKeys = preLoadKeys;
        return this;
    }

    public JrlCache<K, V> build() {
        final JrlCache<K, V> meshCache = JrlCacheBuilder.<K, V>builder(JrlCacheType.MESH.getPrefix() + name)
                .redis(cacheSource, lockConfig)
                .cacheLoader(CacheLoadBuilder.<K, V>builder()
                        .cacheLoader(cacheLoader)
                        .build()
                )
                .expire(meshExpireConfig)
                .connectType(this.connectType)
                .build();
        final JrlCache<K, V> localCache = JrlCacheBuilder.<K, V>builder(JrlCacheType.LOCAL.getPrefix() + name)
                .localCache()
                .cacheLoader(CacheLoadBuilder.<K, V>builder()
                        .cacheLoader(new AbstractJrlFunction<K, V>() {
                            @Override
                            public V apply(K s) {
                                return meshCache.get(s);
                            }
                        })
                        .loadType(loadType)
                        .preLoad(preLoadKeys)
                        .build()
                )
                .expire(localExpireConfig)
                .build();
        return JrlCacheManager.getCache(name, () -> new JrlCacheBothImpl<>(this.name, meshCache, localCache));
    }
}