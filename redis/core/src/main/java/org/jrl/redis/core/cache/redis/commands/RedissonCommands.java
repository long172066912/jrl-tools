package org.jrl.redis.core.cache.redis.commands;

import org.jrl.redis.core.constant.CommandsReadWriteTypeEnum;

/**
* @Title: RedissonCommands
* @Description: //TODO (用一句话描述该文件做什么) 
* @author JerryLong  
* @date 2021/12/10 6:06 下午 
* @version V1.0    
*/
public interface RedissonCommands {
    /**
     * 查询
     * @param key
     * @return
     */
    @CommandsDataType(commands = "getByRedissonMap", readWriteType = CommandsReadWriteTypeEnum.READ)
    Object getByRedissonMap(Object key);

    /**
     * 写入
     * @param key
     * @param value
     */
    @CommandsDataType(commands = "putByRedissonMap", readWriteType = CommandsReadWriteTypeEnum.WRITE)
    void putByRedissonMap(Object key, Object value);

    /**
     * putIfAbsent
     * @param key
     * @param value
     * @return
     */
    @CommandsDataType(commands = "putIfAbsentByRedissonMap", readWriteType = CommandsReadWriteTypeEnum.WRITE)
    Object putIfAbsentByRedissonMap(Object key, Object value);

    /**
     * 删除
     * @param key
     */
    @CommandsDataType(commands = "removeByRedissonMap", readWriteType = CommandsReadWriteTypeEnum.WRITE)
    void removeByRedissonMap(Object key);
}
