package org.jrl.tools.cache.extend.mesh;

import org.jrl.tools.cache.model.JrlCacheLockControlType;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁配置
 *
 * @author JerryLong
 */
public class JrlCacheLockConfig {
    /**
     * 分布式锁控制类型
     */
    private JrlCacheLockControlType lockType;
    /**
     * 获取锁等待时间，tryLock时有效
     */
    private Long waitTime;
    /**
     * 获取锁超时时间
     */
    private Long expireTime;
    /**
     * 获取锁超时时间单位
     */
    private TimeUnit timeUnit;

    protected JrlCacheLockConfig(JrlCacheLockControlType lockType, Long expireTime, TimeUnit timeUnit) {
        this.lockType = lockType;
        this.expireTime = expireTime;
        this.timeUnit = timeUnit;
    }

    protected JrlCacheLockConfig(JrlCacheLockControlType lockType, Long waitTime, Long expireTime, TimeUnit timeUnit) {
        this.lockType = lockType;
        this.waitTime = waitTime;
        this.expireTime = expireTime;
        this.timeUnit = timeUnit;
    }


    public static JrlCacheLockConfig noLock() {
        return new JrlCacheLockConfig(JrlCacheLockControlType.NO_LOCK, null, null, null);
    }

    public static JrlCacheLockConfig tryLock(Long waitTime, Long expireTime, TimeUnit timeUnit) {
        return new JrlCacheLockConfig(JrlCacheLockControlType.TRY_LOCK, waitTime, expireTime, timeUnit);
    }

    public static JrlCacheLockConfig lock(Long expireTime, TimeUnit timeUnit) {
        return new JrlCacheLockConfig(JrlCacheLockControlType.LOCK, null, expireTime, timeUnit);
    }

    public JrlCacheLockControlType getLockType() {
        return lockType;
    }

    public void setLockType(JrlCacheLockControlType lockType) {
        this.lockType = lockType;
    }

    public Long getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(Long waitTime) {
        this.waitTime = waitTime;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }
}
