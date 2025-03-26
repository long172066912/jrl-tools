package org.jrl.redis.core.cache.redis.commands;

import org.jrl.redis.core.BaseCacheExecutor;
import org.jrl.redis.core.constant.CommandsDataTypeEnum;
import org.jrl.redis.core.constant.CommandsReadWriteTypeEnum;
import org.redisson.api.RLock;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author JerryLong
 * @version V1.0
 * 分布式锁，待扩展
 * @date 2021/12/10 6:06 下午
 */
public interface LockCommands {
    /**
     * 分布式锁，未拿到锁会同步阻塞
     *
     * @param name      锁名称
     * @param leaseTime 持有锁时间，逻辑执行完释放
     * @param unit      时间类型
     * @return
     */
    @CommandsDataType(commands = "lock", dataType = CommandsDataTypeEnum.LOCK, readWriteType = CommandsReadWriteTypeEnum.WRITE)
    JrlRedisLock lock(String name, long leaseTime, TimeUnit unit);

    /**
     * 分布式锁，未拿到锁会同步阻塞 waitTime 时间
     *
     * @param name      锁名称
     * @param waitTime  未抢占到锁的等待时间
     * @param leaseTime 持有锁时间，逻辑执行完释放
     * @param unit      时间类型
     * @return
     */
    @CommandsDataType(commands = "tryLock", dataType = CommandsDataTypeEnum.LOCK, readWriteType = CommandsReadWriteTypeEnum.WRITE)
    JrlRedisLock tryLock(String name, long waitTime, long leaseTime, TimeUnit unit);

    /**
     * 释放锁
     *
     * @param lock
     */
    @CommandsDataType(commands = "unlock", dataType = CommandsDataTypeEnum.LOCK, readWriteType = CommandsReadWriteTypeEnum.WRITE)
    void unlock(JrlRedisLock lock);


    public static class JrlRedisLock implements Closeable {
        private final RLock lock;
        private final BaseCacheExecutor cacheExecutor;

        public JrlRedisLock(RLock lock, BaseCacheExecutor cacheExecutor) {
            this.lock = lock;
            this.cacheExecutor = cacheExecutor;
        }

        @Override
        public void close() throws IOException {
            unlock();
        }

        public void unlock() {
            cacheExecutor.unLock(lock);
        }
    }
}
