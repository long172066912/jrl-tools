package org.jrl.tools.cache;

import org.jrl.tools.cache.config.DefaultJrlCacheExpireConfig;
import org.jrl.tools.cache.config.JrlCacheExpireConfig;
import org.jrl.tools.cache.core.builder.CacheLoadBuilder;
import org.jrl.tools.cache.core.builder.JrlCacheBuilder;
import org.jrl.tools.cache.extend.mesh.JrlCacheLockConfig;
import org.jrl.tools.cache.extend.mesh.JrlCacheMeshConnectType;
import org.jrl.tools.cache.model.JrlCacheLoadType;
import org.jrl.tools.utils.function.AbstractJrlFunction;

import java.util.List;
import java.util.function.Function;

/**
 * JrlCache工具类，提供简单的构建方法
 *
 * @author JerryLong
 */
public class JrlCacheUtil {

    /**
     * 获取本地缓存
     *
     * @param name        缓存名称
     * @param cacheLoader 缓存加载器
     * @param <K>         缓存key类型
     * @param <V>         缓存值类型
     * @return 缓存
     */
    public static <K, V> JrlCache<K, V> getLocalCache(String name, Function<K, V> cacheLoader) {
        return JrlCacheBuilder.<K, V>builder(name)
                .localCache()
                .cacheLoader(CacheLoadBuilder.<K, V>builder()
                        .cacheLoader(new AbstractJrlFunction<K, V>() {
                            @Override
                            public V apply(K s) {
                                return cacheLoader.apply(s);
                            }
                        })
                        .build()
                )
                .build();
    }

    /**
     * 获取本地缓存
     *
     * @param name                  缓存名称
     * @param cacheLoader           缓存加载器
     * @param jrlCacheExpireConfig 过期策略
     * @param <K>                   缓存key类型
     * @param <V>                   缓存值类型
     * @return 缓存
     */
    public static <K, V> JrlCache<K, V> getLocalCache(String name, Function<K, V> cacheLoader, JrlCacheExpireConfig jrlCacheExpireConfig) {
        return JrlCacheBuilder.<K, V>builder(name)
                .localCache()
                .cacheLoader(CacheLoadBuilder.<K, V>builder()
                        .cacheLoader(new AbstractJrlFunction<K, V>() {
                            @Override
                            public V apply(K s) {
                                return cacheLoader.apply(s);
                            }
                        })
                        .build()
                )
                .expire(jrlCacheExpireConfig)
                .build();
    }


    public static <K, V> JrlCache<K, V> getLocalCache(String name, Function<K, V> cacheLoader, JrlCacheExpireConfig jrlCacheExpireConfig, List<K> preLoadKeys) {
        return getLocalCache(name, cacheLoader, jrlCacheExpireConfig, preLoadKeys, JrlCacheLoadType.PRELOAD);
    }

    /**
     * 获取本地缓存
     *
     * @param name                  缓存名称
     * @param cacheLoader           缓存加载器
     * @param preLoadKeys           预加载的key
     * @param loadType              加载方式
     * @param jrlCacheExpireConfig 过期配置
     * @param <K>                   缓存key类型
     * @param <V>                   缓存值类型
     * @return 缓存
     */
    public static <K, V> JrlCache<K, V> getLocalCache(String name, Function<K, V> cacheLoader, JrlCacheExpireConfig jrlCacheExpireConfig, List<K> preLoadKeys, JrlCacheLoadType loadType) {
        return JrlCacheBuilder.<K, V>builder(name)
                .localCache()
                .cacheLoader(CacheLoadBuilder.<K, V>builder()
                        .cacheLoader(new AbstractJrlFunction<K, V>() {
                            @Override
                            public V apply(K s) {
                                return cacheLoader.apply(s);
                            }
                        })
                        .loadType(loadType)
                        .preLoad(preLoadKeys)
                        .build()
                )
                .expire(jrlCacheExpireConfig)
                .build();
    }

    /**
     * 获取redis缓存（常规的，过期失效时加载，缓存空值）
     * 过期时间：1天 ~ 2天，随机
     *
     * @param name        缓存名称
     * @param cacheSource 缓存源
     * @param cacheLoader 缓存加载器
     * @param lockConfig  锁配置
     * @param <V>         缓存值类型
     * @return 缓存
     */
    public static <V> JrlCache<String, V> getRedisCache(String name,
                                                        String cacheSource,
                                                        AbstractJrlFunction<String, V> cacheLoader,
                                                        JrlCacheLockConfig lockConfig) {
        return JrlCacheBuilder.<String, V>builder(name)
                .redis(cacheSource, lockConfig)
                .cacheLoader(CacheLoadBuilder.<String, V>builder()
                        .cacheLoader(cacheLoader)
                        .build()
                )
                .expire(DefaultJrlCacheExpireConfig.oneDay(new JrlCacheExpireConfig.ExpireRandom(0L, 86400L)))
                .build();
    }

    /**
     * 获取redis(连接池)缓存（常规的，过期失效时加载，缓存空值）
     * 过期时间：1天 ~ 2天，随机
     *
     * @param name        缓存名称
     * @param cacheSource 缓存源
     * @param cacheLoader 缓存加载器
     * @param lockConfig  锁配置
     * @param <V>         缓存值类型
     * @return 缓存
     */
    public static <V> JrlCache<String, V> getRedisPoolCache(String name,
                                                            String cacheSource,
                                                            AbstractJrlFunction<String, V> cacheLoader,
                                                            JrlCacheLockConfig lockConfig) {
        return JrlCacheBuilder.<String, V>builder(name)
                .redis(cacheSource, lockConfig)
                .cacheLoader(CacheLoadBuilder.<String, V>builder()
                        .cacheLoader(cacheLoader)
                        .build()
                )
                .connectType(JrlCacheMeshConnectType.POOL)
                .expire(DefaultJrlCacheExpireConfig.oneDay(new JrlCacheExpireConfig.ExpireRandom(0L, 86400L)))
                .build();
    }

    /**
     * 获取redis缓存
     *
     * @param name         缓存名称
     * @param cacheSource  缓存源
     * @param cacheLoader  缓存加载器
     * @param lockConfig   锁配置
     * @param expireConfig 过期配置
     * @param <V>          缓存值类型
     * @return 缓存
     */
    public static <V> JrlCache<String, V> getRedisCache(String name,
                                                        String cacheSource,
                                                        AbstractJrlFunction<String, V> cacheLoader,
                                                        JrlCacheLockConfig lockConfig, JrlCacheExpireConfig expireConfig) {
        return JrlCacheBuilder.<String, V>builder(name)
                .redis(cacheSource, lockConfig)
                .cacheLoader(CacheLoadBuilder.<String, V>builder()
                        .cacheLoader(cacheLoader)
                        .build()
                )
                .expire(expireConfig)
                .build();
    }

    /**
     * 获取redis缓存，通过keyBuilder方式构建key
     *
     * @param name         缓存名称
     * @param cacheSource  缓存源
     * @param cacheLoader  缓存加载器
     * @param lockConfig   锁配置
     * @param expireConfig 过期配置
     * @param <P>          key参数对象
     * @param <V>          缓存值类型
     * @return 缓存
     */
    public static <P, V> JrlCache<JrlCacheKeyBuilder<P, String>, V> getRedisCacheByKeyBuilder(String name,
                                                                                              String cacheSource,
                                                                                              AbstractJrlFunction<JrlCacheKeyBuilder<P, String>, V> cacheLoader,
                                                                                              JrlCacheLockConfig lockConfig, JrlCacheExpireConfig expireConfig) {
        return JrlCacheBuilder.<JrlCacheKeyBuilder<P, String>, V>builder(name)
                .redis(cacheSource, lockConfig)
                .cacheLoader(CacheLoadBuilder.<JrlCacheKeyBuilder<P, String>, V>builder()
                        .cacheLoader(cacheLoader)
                        .build()
                )
                .expire(expireConfig)
                .build();
    }


    public static <P, K extends JrlCacheKeyBuilder<P, String>, V> JrlCache<K, V> getRedisCacheExtKeyBuilder(String name,
                                                                                                            String cacheSource,
                                                                                                            AbstractJrlFunction<K, V> cacheLoader,
                                                                                                            JrlCacheLockConfig lockConfig, JrlCacheExpireConfig expireConfig) {
        return JrlCacheBuilder.<K, V>builder(name)
                .redis(cacheSource, lockConfig)
                .cacheLoader(CacheLoadBuilder.<K, V>builder()
                        .cacheLoader(cacheLoader)
                        .build()
                )
                .expire(expireConfig)
                .build();
    }

    /**
     * 多级缓存
     *
     * @param name        缓存名称
     * @param cacheSource 资源名称
     * @param cacheLoader 缓存加载器
     * @param lockConfig  锁配置
     * @param <V>         缓存值类型
     * @return JrlCache
     */
    public static <V> JrlCache<String, V> getLocalAndRedisCache(String name,
                                                                String cacheSource,
                                                                AbstractJrlFunction<String, V> cacheLoader,
                                                                JrlCacheLockConfig lockConfig) {
        return JrlCacheBuilder.<String, V>builder(name).both(cacheSource, cacheLoader, lockConfig, DefaultJrlCacheExpireConfig.oneDay())
                .build();
    }

    /**
     * 多级缓存，并设置预热 or 定时策略
     *
     * @param name         缓存名称
     * @param cacheSource  资源名称
     * @param cacheLoader  缓存加载器
     * @param lockConfig   锁配置
     * @param expireConfig 过期配置
     * @param preLoadKeys  预加载的key
     * @param loadType     加载方式
     * @param <V>          缓存值类型
     * @return JrlCache
     */
    public static <V> JrlCache<String, V> getLocalAndRedisCache(String name,
                                                                String cacheSource,
                                                                AbstractJrlFunction<String, V> cacheLoader,
                                                                JrlCacheLockConfig lockConfig, JrlCacheExpireConfig expireConfig,
                                                                List<String> preLoadKeys, JrlCacheLoadType loadType) {
        return JrlCacheBuilder.<String, V>builder(name).both(cacheSource, cacheLoader, lockConfig, expireConfig)
                .preLoadKeys(preLoadKeys)
                .loadType(loadType)
                .build();
    }

    /**
     * 多级缓存，并设置本地缓存过期配置和redis缓存过期配置
     *
     * @param name              缓存名称
     * @param cacheSource       资源名称
     * @param cacheLoader       缓存加载器
     * @param lockConfig        锁配置
     * @param localExpireConfig 本地缓存过期配置
     * @param redisExpireConfig redis缓存过期配置
     * @param <V>               缓存值类型
     * @return JrlCache
     */
    public static <V> JrlCache<String, V> getLocalAndRedisCache(String name,
                                                                String cacheSource,
                                                                AbstractJrlFunction<String, V> cacheLoader,
                                                                JrlCacheLockConfig lockConfig, JrlCacheExpireConfig localExpireConfig, JrlCacheExpireConfig redisExpireConfig) {
        return JrlCacheBuilder.<String, V>builder(name).both(cacheSource, cacheLoader, lockConfig, redisExpireConfig).localExpire(localExpireConfig).build();
    }

    /**
     * 多级缓存，分布式对象类型
     *
     * @param name         缓存名称
     * @param cacheSource  资源名称
     * @param cacheLoader  缓存加载器
     * @param lockConfig   锁配置
     * @param expireConfig 过期配置
     * @param <K>          key类型
     * @param <V>          缓存值类型
     * @return JrlCache
     */
    public static <P, K extends JrlCacheKeyBuilder<P, String>, V> JrlCache<K, V> getLocalAndRedisCacheExtKeyBuilder(String name,
                                                                                                                    String cacheSource,
                                                                                                                    AbstractJrlFunction<K, V> cacheLoader,
                                                                                                                    JrlCacheLockConfig lockConfig, JrlCacheExpireConfig expireConfig) {
        return JrlCacheBuilder.<K, V>builder(name).both(cacheSource, cacheLoader, lockConfig, expireConfig).build();
    }
}
