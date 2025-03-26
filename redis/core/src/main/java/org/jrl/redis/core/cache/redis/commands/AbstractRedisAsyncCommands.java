package org.jrl.redis.core.cache.redis.commands;

/**
* @Title: AbstractRedisAsyncCommands
* @Description: 异步命令抽象类
* @author JerryLong
* @date 2021/3/30 10:40 AM
* @version V1.0
*/
public abstract class AbstractRedisAsyncCommands implements RedisAsyncCommands {

    /**
     * 放入异步执行器
     *
     * @param asyncSource
     */
    public abstract void setAsyncExeutor(Object asyncSource);
}
