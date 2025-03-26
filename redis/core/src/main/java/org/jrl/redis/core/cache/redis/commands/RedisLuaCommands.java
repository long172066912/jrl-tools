package org.jrl.redis.core.cache.redis.commands;

import org.jrl.redis.core.constant.CommandsDataTypeEnum;
import org.jrl.redis.core.constant.CommandsReadWriteTypeEnum;
import org.jrl.redis.exception.CacheKeyNotExistsException;

import java.util.List;
import java.util.Map;

/**
 * 提供一些lua命令
 *
 * @author JerryLong
 */
public interface RedisLuaCommands {

    String KEY_NOT_EXISTS = "keyNil";

    /**
     * 批量zscore
     *
     * @param key
     * @param members
     * @return Map<member, score>
     */
    @CommandsDataType(commands = "zscoreBatch", dataType = CommandsDataTypeEnum.ZSET, readWriteType = CommandsReadWriteTypeEnum.READ)
    Map<String, Double> zscoreBatch(String key, List<String> members);

    /**
     * zadd 如果key存在则添加
     *
     * @param key
     * @param score
     * @param member
     * @param seconds
     * @return
     */
    @CommandsDataType(commands = "zaddIfKeyExists", dataType = CommandsDataTypeEnum.ZSET, readWriteType = CommandsReadWriteTypeEnum.WRITE)
    Long zaddIfKeyExists(String key, double score, String member, int seconds);

    @CommandsDataType(commands = "zaddIfKeyExists", dataType = CommandsDataTypeEnum.ZSET, readWriteType = CommandsReadWriteTypeEnum.WRITE)
    Long zaddIfKeyMustExists(String key, double score, String member, int seconds) throws CacheKeyNotExistsException;

    /**
     * hget 如果key存在则返回，key不存在抛异常
     *
     * @param key
     * @param field
     * @return
     */
    @CommandsDataType(commands = "hgetIfKeyExists", dataType = CommandsDataTypeEnum.HASH, readWriteType = CommandsReadWriteTypeEnum.READ)
    String hgetIfKeyExists(String key, String field) throws CacheKeyNotExistsException;

    /**
     * hset 如果key存在则添加
     *
     * @param key
     * @param field
     * @param value
     * @param seconds
     * @return
     */
    @CommandsDataType(commands = "hsetIfKeyExists", dataType = CommandsDataTypeEnum.HASH, readWriteType = CommandsReadWriteTypeEnum.WRITE)
    Long hsetIfKeyExists(String key, String field, String value, int seconds) throws CacheKeyNotExistsException;

    /**
     * sadd 如果key存在则添加
     *
     * @param key
     * @param member
     * @param seconds
     * @return
     */
    @CommandsDataType(commands = "saddIfKeyExist", dataType = CommandsDataTypeEnum.SET, readWriteType = CommandsReadWriteTypeEnum.WRITE)
    Long saddIfKeyExist(String key, int seconds, String... member) throws CacheKeyNotExistsException;

    /**
     * lpush 如果key存在则添加
     *
     * @param key
     * @param member
     * @param seconds
     * @return
     */
    @CommandsDataType(commands = "lpushIfExists", dataType = CommandsDataTypeEnum.LIST, readWriteType = CommandsReadWriteTypeEnum.WRITE)
    Long lpushIfExists(String key, String member, int seconds) throws CacheKeyNotExistsException;

    /**
     * rpush 如果key存在则添加
     *
     * @param key
     * @param member
     * @param seconds
     * @return
     */
    @CommandsDataType(commands = "rpushIfExists", dataType = CommandsDataTypeEnum.LIST, readWriteType = CommandsReadWriteTypeEnum.WRITE)
    Long rpushIfExists(String key, String member, int seconds) throws CacheKeyNotExistsException;
}
