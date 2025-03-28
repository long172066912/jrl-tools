package org.jrl.tools.cache.model;

/**
 * 缓存锁控制方式
 *
 * @author JerryLong
 */
public enum JrlCacheLockControlType {
    /**
     * 不加锁
     */
    NO_LOCK,
    /**
     * 尝试锁
     */
    TRY_LOCK,
    /**
     * 锁
     */
    LOCK;
}
