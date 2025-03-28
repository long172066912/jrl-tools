package org.jrl.tools.cache.core.event.model;

import java.util.Set;

/**
* 热key事件数据
* @author JerryLong
*/
public class JrlCacheHotKeyEventData<K> {
    /**
     * 缓存名称
     */
    private String cacheName;
    /**
     * 本地缓存时间
     */
    private Integer localCacheSeconds;
    /**
     * 热key
     */
    private Set<K> keys;

    public JrlCacheHotKeyEventData() {
    }

    public JrlCacheHotKeyEventData(String cacheName, Integer localCacheSeconds, Set<K> keys) {
        this.cacheName = cacheName;
        this.localCacheSeconds = localCacheSeconds;
        this.keys = keys;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public Integer getLocalCacheSeconds() {
        return localCacheSeconds;
    }

    public void setLocalCacheSeconds(Integer localCacheSeconds) {
        this.localCacheSeconds = localCacheSeconds;
    }

    public Set<K> getKeys() {
        return keys;
    }

    public void setKeys(Set<K> keys) {
        this.keys = keys;
    }
}