package org.jrl.tools.cache.core.builder;

import org.jrl.tools.cache.config.JrlCacheExpireConfig;
import org.jrl.tools.cache.extend.both.JrlBothBuilder;
import org.jrl.tools.cache.extend.local.JrlCacheLocalBuilder;
import org.jrl.tools.cache.extend.mesh.JrlCacheLockConfig;
import org.jrl.tools.cache.extend.mesh.JrlCacheMeshBuilder;
import org.jrl.tools.utils.function.AbstractJrlFunction;

import java.util.concurrent.TimeUnit;

/**
 * JrlCache缓存构建器
 *
 * @author JerryLong
 */
public class JrlCacheBuilder {

    /**
     * 缓存构建器
     *
     * @param name 缓存
     * @param <K>  key类型
     * @param <V>  value类型
     * @return 缓存构建器
     */
    public static <K, V> ConfigBuilder<K, V> builder(String name) {
        return new ConfigBuilder<>(name);
    }

    /**
     * 缓存配置构建器
     *
     * @param <K> key类型
     * @param <V> value类型
     */
    public static class ConfigBuilder<K, V> {
        private String name;

        public ConfigBuilder(String name) {
            this.name = name;
        }

        public JrlCacheLocalBuilder<K, V> localCache() {
            return new JrlCacheLocalBuilder<>(name);
        }

        public JrlCacheMeshBuilder<K, V> redis(String cacheSource, JrlCacheLockConfig lockConfig) {
            return new JrlCacheMeshBuilder<>(name, cacheSource, lockConfig);
        }

        public JrlCacheMeshBuilder<K, V> redisNoLock(String cacheSource) {
            return new JrlCacheMeshBuilder<>(name, cacheSource, JrlCacheLockConfig.noLock());
        }

        public JrlCacheMeshBuilder<K, V> redisLock(String cacheSource, long expireTime, TimeUnit timeUnit) {
            return new JrlCacheMeshBuilder<>(name, cacheSource, JrlCacheLockConfig.lock(expireTime, timeUnit));
        }

        public JrlCacheMeshBuilder<K, V> redisTryLock(String cacheSource, long waitTime, long expireTime, TimeUnit timeUnit) {
            return new JrlCacheMeshBuilder<>(name, cacheSource, JrlCacheLockConfig.tryLock(waitTime, expireTime, timeUnit));
        }

        public JrlBothBuilder<K, V> both(String cacheSource,
                                         AbstractJrlFunction<K, V> cacheLoader,
                                         JrlCacheLockConfig lockConfig, JrlCacheExpireConfig expireConfig) {
            return new JrlBothBuilder<>(name, cacheSource, cacheLoader, lockConfig, expireConfig);
        }
    }
}
