package org.jrl.utils.cache.spi;

import org.jrl.redis.core.BaseCacheExecutor;
import org.jrl.tools.cache.extend.mesh.JrlCacheMeshConfig;
import org.jrl.tools.cache.model.JrlCacheLock;
import org.jrl.tools.cache.spi.JrlCacheLockSpi;
import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

/**
 * redis缓存组件实现
 *
 * @author JerryLong
 */
public class JrlRedisLockSpiImpl implements JrlCacheLockSpi {

    @Override
    public JrlCacheLock lock(JrlCacheMeshConfig jrlCacheMeshConfig, String lockKey, long expireTime, TimeUnit timeUnit) {
        final BaseCacheExecutor cacheExecutor = JrlRedisClientUtils.getCacheExecutor(jrlCacheMeshConfig.getCacheSource(), jrlCacheMeshConfig.getConnectType());
        final RLock lock = cacheExecutor.lock(lockKey, expireTime, timeUnit);
        return new JrlRedisLock(lock, cacheExecutor);
    }

    @Override
    public JrlCacheLock tryLock(JrlCacheMeshConfig jrlCacheMeshConfig, String lockKey, long waitTime, long expireTime, TimeUnit timeUnit) {
        final BaseCacheExecutor cacheExecutor = JrlRedisClientUtils.getCacheExecutor(jrlCacheMeshConfig.getCacheSource(), jrlCacheMeshConfig.getConnectType());
        final RLock lock = cacheExecutor.tryLock(lockKey, waitTime, expireTime, timeUnit);
        return new JrlRedisLock(lock, cacheExecutor);
    }

    public static class JrlRedisLock implements JrlCacheLock {

        private RLock lock;
        private BaseCacheExecutor cacheExecutor;

        public JrlRedisLock(RLock lock, BaseCacheExecutor cacheExecutor) {
            this.lock = lock;
            this.cacheExecutor = cacheExecutor;
        }

        @Override
        public void unlock() {
            cacheExecutor.unLock(lock);
        }
    }
}
