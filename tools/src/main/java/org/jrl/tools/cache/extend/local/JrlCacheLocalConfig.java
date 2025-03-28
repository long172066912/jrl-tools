package org.jrl.tools.cache.extend.local;

import org.jrl.tools.cache.config.AbstractJrlCacheConfig;
import org.jrl.tools.cache.model.JrlCacheLockControlType;
import org.jrl.tools.cache.model.JrlCacheType;

import java.util.concurrent.TimeUnit;

/**
 * 缓存配置，通过配置，可以配置缓存类型等等
 *
 * @author JerryLong
 */
public class JrlCacheLocalConfig<K, V> extends AbstractJrlCacheConfig<K, V> {
    /**
     * 缓存初始容量
     */
    private Integer initialCapacity;
    /**
     * 缓存最大数量
     */
    private Integer maxSize;

    public JrlCacheLocalConfig(String name) {
        super(name);
        this.setCacheType(JrlCacheType.LOCAL);
    }

    @Override
    public JrlCacheLockControlType lockType() {
        //本地缓存走默认锁
        return JrlCacheLockControlType.LOCK;
    }

    public long getExpireTime() {
        return super.expire().expire();
    }

    public TimeUnit getUnit() {
        return super.expire().unit();
    }

    public Integer getInitialCapacity() {
        return initialCapacity;
    }

    public void setInitialCapacity(Integer initialCapacity) {
        this.initialCapacity = initialCapacity;
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }
}
