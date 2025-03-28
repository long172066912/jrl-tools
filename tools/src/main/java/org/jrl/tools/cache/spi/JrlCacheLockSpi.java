package org.jrl.tools.cache.spi;

import org.jrl.tools.cache.extend.mesh.JrlCacheMeshConfig;
import org.jrl.tools.cache.model.JrlCacheLock;

import java.util.concurrent.TimeUnit;

/**
 * 缓存-分布式锁-spi
 *
 * @author JerryLong
 */
public interface JrlCacheLockSpi {
    /**
     * 获取锁
     *
     * @param jrlCacheMeshConfig 缓存配置
     * @param lockKey             锁key
     * @param expireTime          失效时间
     * @param timeUnit            失效时间单位
     * @return 是否获取成功
     */
    JrlCacheLock lock(JrlCacheMeshConfig jrlCacheMeshConfig, String lockKey, long expireTime, TimeUnit timeUnit);

    /**
     * 尝试获取锁
     *
     * @param jrlCacheMeshConfig 缓存配置
     * @param lockKey             锁key
     * @param waitTime            等待时间
     * @param expireTime          失效时间
     * @param timeUnit            失效时间单位
     * @return 是否获取成功
     */
    JrlCacheLock tryLock(JrlCacheMeshConfig jrlCacheMeshConfig, String lockKey, long waitTime, long expireTime, TimeUnit timeUnit);
}
