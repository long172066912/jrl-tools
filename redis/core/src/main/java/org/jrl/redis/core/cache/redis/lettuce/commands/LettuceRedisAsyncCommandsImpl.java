package org.jrl.redis.core.cache.redis.lettuce.commands;

import org.jrl.redis.core.cache.redis.commands.AbstractRedisAsyncCommands;
import org.jrl.redis.core.cache.redis.lettuce.AbstractLettuceHandleExecutor;
import io.lettuce.core.RedisFuture;

import java.util.concurrent.CompletableFuture;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: LettuceRedisAsyncCommandsImpl
 * @Description: 异步命令实现
 * @date 2021/3/29 7:48 PM
 */
public class LettuceRedisAsyncCommandsImpl extends AbstractRedisAsyncCommands {

    private AbstractLettuceHandleExecutor asyncSource;

    @Override
    public void setAsyncExeutor(Object asyncSource) {
        this.asyncSource = (AbstractLettuceHandleExecutor) asyncSource;
    }

    @Override
    public RedisFuture<Long> publish(String channel, String message) {
        return (RedisFuture<Long>) asyncSource.execute(() -> (asyncSource.async(asyncSource.getConnectResource())).publish(channel, message));
    }

    @Override
    public RedisFuture<Long> incr(String key, int seconds) {
        try {
            return (RedisFuture<Long>) asyncSource.execute(() -> (asyncSource.async(asyncSource.getConnectResource())).incr(key), key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> asyncSource.expire(key, seconds), asyncSource.getExpireThreadPoolExecutor());
            }
        }
    }
}
