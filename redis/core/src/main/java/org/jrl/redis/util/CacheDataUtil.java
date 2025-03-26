package org.jrl.redis.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jrl.redis.core.BaseCacheExecutor;
import org.jrl.redis.core.model.CacheDataBuilder;
import org.jrl.redis.exception.CacheExceptionFactory;
import org.apache.commons.lang3.StringUtils;
import org.jrl.tools.json.JrlJsonNoExpUtil;
import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author JerryLong
 * @version V1.0
 * 多级缓存帮助类，后续将提供更好的JrlCache
 * @date 2023/1/16 17:41
 */
@Deprecated
public class CacheDataUtil<K, V> {

    private static Logger LOGGER = JrlLoggerFactory.getLogger(CacheDataUtil.class);

    private final BaseCacheExecutor redisClient;
    private final String redisChannel;
    private Cache<K, Optional<V>> localCache;
    private Boolean isLocalCache;
    private Boolean isOpenPubsub;
    private Class<K> kClass;

    private CacheDataUtil(Integer initialCapacity, Integer maximumSize, Integer expireAfterWriteTime, TimeUnit expireAfterWriteUnit, BaseCacheExecutor redisClient, Boolean isLocalCache, Boolean isOpenPubsub, Class<K> kClass, String redisChannel) {
        this.redisClient = redisClient;
        this.isLocalCache = isLocalCache;
        this.isOpenPubsub = isOpenPubsub;
        this.kClass = kClass;
        this.redisChannel = redisChannel;
        localCache = Caffeine.newBuilder()
                .initialCapacity(null == initialCapacity ? 10 : initialCapacity)
                .maximumSize(null == maximumSize ? 100000 : maximumSize)
                .expireAfterWrite(null == expireAfterWriteTime ? 1 : expireAfterWriteTime, null == expireAfterWriteUnit ? TimeUnit.MINUTES : expireAfterWriteUnit)
                .build();
        doSub();
    }

    protected void doSub() {
        if (this.isOpenPubsub && null != this.redisClient) {
            redisClient.subscribe(message -> Optional.ofNullable(JrlJsonNoExpUtil.fromJson(message, kClass)).ifPresent(this::removeLocal), redisChannel);
        }
    }

    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    public static class Builder<K, V> {
        private Integer initialCapacity;
        private Integer maximumSize;
        private Integer expireAfterWriteTime;
        private TimeUnit expireAfterWriteUnit;
        private BaseCacheExecutor redisClient;
        private boolean isLocalCache = true;
        private boolean isOpenPubsub = false;
        private Class<K> kClass;
        private String redisChannel;

        public Builder<K, V> initialCapacity(Integer initialCapacity) {
            this.initialCapacity = initialCapacity;
            return this;
        }

        public Builder<K, V> maximumSize(Integer maximumSize) {
            this.maximumSize = maximumSize;
            return this;
        }

        public Builder<K, V> expireAfterWriteTime(Integer expireAfterWriteTime) {
            this.expireAfterWriteTime = expireAfterWriteTime;
            return this;
        }

        public Builder<K, V> expireAfterWriteUnit(TimeUnit expireAfterWriteUnit) {
            this.expireAfterWriteUnit = expireAfterWriteUnit;
            return this;
        }

        public Builder<K, V> redisClient(BaseCacheExecutor redisClient) {
            this.redisClient = redisClient;
            return this;
        }

        public Builder<K, V> isLocalCache(boolean isLocalCache) {
            this.isLocalCache = isLocalCache;
            return this;
        }

        public Builder<K, V> isOpenPubsub(boolean isOpenPubsub, Class<K> kClass, String redisChannel) {
            this.isOpenPubsub = isOpenPubsub;
            if (isOpenPubsub) {
                if (null == kClass || StringUtils.isBlank(redisChannel)) {
                    CacheExceptionFactory.throwException("openPubsub kClass and redisChannel must no null !");
                }
            }
            this.kClass = kClass;
            this.redisChannel = redisChannel;
            return this;
        }

        public CacheDataUtil<K, V> build() {
            return new CacheDataUtil<>(initialCapacity, maximumSize, expireAfterWriteTime, expireAfterWriteUnit, redisClient, isLocalCache, isOpenPubsub, kClass, redisChannel);
        }
    }

    /**
     * 通过本地缓存查询
     *
     * @param k
     * @return
     */
    public Optional<V> getLocalCacheData(K k) {
        return localCache.getIfPresent(k);
    }

    /**
     * 通过多级缓存查询
     *
     * @param k
     * @param cacheDataBuilder
     * @return
     */
    public Optional<V> getCacheData(K k, CacheDataBuilder<V> cacheDataBuilder) {
        if (isLocalCache) {
            return localCache.get(k, e -> Optional.ofNullable(redisClient.getCacheData(cacheDataBuilder)));
        }
        return Optional.ofNullable(redisClient.getCacheData(cacheDataBuilder));
    }

    /**
     * 通过缓存查询
     *
     * @param k
     * @param supplier
     * @return
     */
    public Optional<V> getCacheData(K k, Supplier<V> supplier) {
        if (isLocalCache) {
            return localCache.get(k, e -> Optional.ofNullable(supplier.get()));
        }
        return Optional.ofNullable(supplier.get());
    }

    /**
     * 通过多级缓存查询，增加分布式缓存返回处理器
     *
     * @param k
     * @param redisCacheHandler
     * @param cacheDataBuilder
     * @param <R>
     * @return
     */
    public <R> Optional<V> getCacheData(K k, CacheDataBuilder<R> cacheDataBuilder, Function<R, V> redisCacheHandler) {
        if (isLocalCache) {
            return localCache.get(k, e -> Optional.ofNullable(redisCacheHandler.apply(redisClient.getCacheData(cacheDataBuilder))));
        }
        return Optional.ofNullable(redisCacheHandler.apply(redisClient.getCacheData(cacheDataBuilder)));
    }

    public void remove(K k) {
        removeLocal(k);
        if (isOpenPubsub && null != redisClient && StringUtils.isNotBlank(redisChannel)) {
            redisClient.publish(redisChannel, JrlJsonNoExpUtil.toJson(k));
        }
    }

    public void removeLocal(K k) {
        localCache.invalidate(k);
    }
}
