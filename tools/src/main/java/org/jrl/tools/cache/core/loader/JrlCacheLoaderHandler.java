package org.jrl.tools.cache.core.loader;

import org.jrl.tools.cache.JrlCache;
import org.jrl.tools.cache.JrlCacheLoader;
import org.jrl.tools.cache.spi.monitor.JrlCacheMonitorSpi;
import org.jrl.tools.spi.JrlSpiLoader;

/**
 * 缓存加载处理器
 *
 * @author JerryLong
 */
public class JrlCacheLoaderHandler {

    private static final JrlCacheMonitorSpi MONITOR_SPI = JrlSpiLoader.getInstanceOrDefault(JrlCacheMonitorSpi.class, JrlCacheMonitorSpi.DefaultJrlCacheMonitorSpi::new);

    private static ThreadLocal<Boolean> isLoading = new ThreadLocal<>();

    /**
     * 加载缓存
     *
     * @param cache       缓存
     * @param cacheLoader 缓存加载器
     * @param key         key
     * @param <K>         key类型
     * @param <V>         value类型
     * @return value
     */
    public static synchronized <K, V> V load(JrlCache<K, V> cache, JrlCacheLoader<K, V> cacheLoader, K key) {
        return load(cache, cacheLoader, key, true);
    }

    /**
     * 加载缓存
     *
     * @param cache       缓存
     * @param cacheLoader 缓存加载器
     * @param key         key
     * @param readCache   是否查一次缓存
     * @param <K>         key类型
     * @param <V>         value类型
     * @return value
     */
    public static synchronized <K, V> V load(JrlCache<K, V> cache, JrlCacheLoader<K, V> cacheLoader, K key, boolean readCache) {
        final long l = System.currentTimeMillis();
        try {
            if (readCache) {
                final V v = cache.getIfPresent(key);
                //如果缓存中有值，则直接返回
                if (null != v) {
                    return v;
                }
            }
            isLoading.set(true);
            return cacheLoader.load(key);
        } finally {
            isLoading.remove();
            //监控
            MONITOR_SPI.monitor(cache.getConfig().name(), cache.getConfig().getCacheType().name(), "load", System.currentTimeMillis() - l, null);
        }
    }

    public static boolean isLoading() {
        return null != isLoading.get() && isLoading.get();
    }
}
