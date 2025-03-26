package org.jrl.redis.core.cache.redis.lettuce.proxy;

import org.jrl.redis.core.handle.AbstractCacheHandle;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: LettuceAsyncProxy
 * @Description: 生菜异步代理
 * @date 2021/10/21 2:24 PM
 */
public class LettuceAsyncProxy implements InvocationHandler {
    /**
     * 被代理的对象
     */
    private RedisClusterAsyncCommands redisAsyncCommands;

    private AbstractCacheHandle executor;

    public LettuceAsyncProxy(AbstractCacheHandle executor, RedisClusterAsyncCommands redisAsyncCommands) {
        this.executor = executor;
        this.redisAsyncCommands = redisAsyncCommands;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return executor.execute(() -> method.invoke(redisAsyncCommands, args));
    }

    public RedisClusterAsyncCommands getProxy() {
        return (RedisClusterAsyncCommands) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                redisAsyncCommands.getClass().getInterfaces(),
                this);
    }
}
