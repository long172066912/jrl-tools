package org.jrl.tools.cache.model;

/**
 * 锁对象
 *
 * @author JerryLong
 */
public interface JrlCacheLock {
    /**
     * 释放锁
     */
    void unlock();
}
