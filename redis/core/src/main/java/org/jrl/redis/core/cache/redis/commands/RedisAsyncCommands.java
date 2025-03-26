package org.jrl.redis.core.cache.redis.commands;

import org.jrl.redis.core.constant.CommandsDataTypeEnum;
import org.jrl.redis.core.constant.CommandsReadWriteTypeEnum;
import io.lettuce.core.RedisFuture;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: RedisAsyncCommands
 * @Description: redis异步命令接口，只支持Lettuce
 * @date 2021/3/29 7:33 PM
 */
public interface RedisAsyncCommands {

    /**
     * 异步publish
     *
     * @param channel
     * @param message
     * @return
     */
    @CommandsDataType(commands = "asyncPublish", readWriteType = CommandsReadWriteTypeEnum.WRITE, dataType = CommandsDataTypeEnum.PUBSUB)
    RedisFuture<Long> publish(String channel, String message);

    /**
     * 异步自增
     *
     * @param key
     * @param expireSeconds
     * @return
     */
    @CommandsDataType(commands = "asyncIncr", readWriteType = CommandsReadWriteTypeEnum.WRITE)
    RedisFuture<Long> incr(String key, int expireSeconds);
}
