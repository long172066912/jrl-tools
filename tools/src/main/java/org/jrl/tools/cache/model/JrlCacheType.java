package org.jrl.tools.cache.model;

/**
 * 缓存类型
 *
 * @author JerryLong
 */
public enum JrlCacheType {
    /**
     * 本地缓存
     */
    LOCAL("local-"),
    /**
     * 分布式缓存
     */
    MESH("mesh-"),
    /**
     * 本地+分布式缓存
     */
    BOTH("both-"),
    ;

    private String prefix;

    JrlCacheType(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
