package org.jrl.tools.cache.spi.monitor;

/**
 * 缓存-缓存命中率监控-spi，后续支持
 *
 * @author JerryLong
 */
public interface JrlCacheHitCountMonitorSpi {
    /**
     * 监控缓存命中率
     *
     * @param cache 缓存类型
     * @param key   缓存key
     * @param isHit 是否命中
     */
    void monitor(String cache, String key, boolean isHit);
}
