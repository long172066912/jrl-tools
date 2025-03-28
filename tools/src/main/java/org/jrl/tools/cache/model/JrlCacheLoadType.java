package org.jrl.tools.cache.model;

/**
 * 缓存类型
 *
 * @author JerryLong
 */
public enum JrlCacheLoadType {
    /**
     * 缓存失效时加载
     */
    EXPIRED,
    /**
     * 定时加载，如果没有缓存，仍然会走 EXPIRED
     */
    SCHEDULED,
    /**
     * 预加载
     */
    PRELOAD,
    /**
     * 预加载 + 定时加载
     */
    PRELOAD_SCHEDULED;
}
