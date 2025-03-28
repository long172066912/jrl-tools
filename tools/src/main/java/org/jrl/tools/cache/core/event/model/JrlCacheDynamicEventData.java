package org.jrl.tools.cache.core.event.model;

/**
 * 动态配置事件数据，支持从redis变成多级缓存
 *
 * @author JerryLong
 */
public class JrlCacheDynamicEventData {
    /**
     * 缓存名称
     */
    private String cacheName;
    /**
     * 缓存过期时间，单位秒
     */
    private Integer expireSeconds;

    public JrlCacheDynamicEventData() {
    }

    public JrlCacheDynamicEventData(String cacheName, Integer expireSeconds) {
        this.cacheName = cacheName;
        this.expireSeconds = expireSeconds;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public Integer getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(Integer expireSeconds) {
        this.expireSeconds = expireSeconds;
    }
}