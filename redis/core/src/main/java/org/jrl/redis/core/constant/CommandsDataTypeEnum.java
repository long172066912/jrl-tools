package org.jrl.redis.core.constant;

import org.jrl.redis.core.cache.redis.commands.*;
import org.jrl.redis.exception.CacheExceptionFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: ConnectTypeEnum
 * @Description: 命令数据类型枚举
 * @date 2021/1/20 5:43 PM
 */
public enum CommandsDataTypeEnum {
    /**
     * 未知，需要设置
     */
    UNKNOWN,
    /**
     * 字符串
     */
    STRING,
    /**
     * list
     */
    LIST,
    /**
     * set
     */
    SET,
    /**
     * zset
     */
    ZSET,
    /**
     * hash
     */
    HASH,
    /**
     * geo
     */
    GEO,
    /**
     * bitmap
     */
    BITMAP,
    /**
     * lock
     */
    LOCK,
    /**
     * HYPERLOGLOG
     */
    HYPERLOGLOG,
    /**
     * pubsub
     */
    PUBSUB,
    /**
     * eval
     */
    EVAL,
    /**
     * echo
     */
    ECHO,
    /**
     * SLOWLOG
     */
    SLOWLOG,
    /**
     * EXPIRE
     */
    EXPIRE,
    /**
     * 自定义的
     */
    OTHER;

    /**
     * 命令数据类型集合
     */
    public static Map<String, CommandsDataTypeEnum> commandsDataTypes = new ConcurrentHashMap<>();
    public static Map<String, CommandsReadWriteTypeEnum> commandsReadWriteTypeTypes = new ConcurrentHashMap<>();

    static {
        //反射放入map中
        buildReidsCommands(RedisCommands.class.getMethods());
        buildReidsCommands(RedisAsyncCommands.class.getMethods());
        buildReidsCommands(RedissonCommands.class.getMethods());
        buildReidsCommands(RedisLuaCommands.class.getMethods());
    }

    private static void buildReidsCommands(Method[] methods) {
        for (Method method : methods) {
            //判断属性是否标注了@CommandsDataType注解
            boolean methodHasAnno = method.isAnnotationPresent(CommandsDataType.class);
            if (methodHasAnno) {
                //获取CommandsDataType注解
                CommandsDataType commandsDataType = method.getAnnotation(CommandsDataType.class);
                commandsDataTypes.put(commandsDataType.commands(), commandsDataType.dataType());
                commandsReadWriteTypeTypes.put(commandsDataType.commands(), commandsDataType.readWriteType());
            } else {
                CacheExceptionFactory.addWarnLog("RedisCommands commands:{} dataType is null !", method.getName());
            }
        }
    }

    public static CommandsDataTypeEnum getCommandsDataType(String commands) {
        return commandsDataTypes.getOrDefault(commands, CommandsDataTypeEnum.UNKNOWN);
    }

    public static CommandsReadWriteTypeEnum getCommandsReadWriteType(String commands) {
        return commandsReadWriteTypeTypes.getOrDefault(commands, CommandsReadWriteTypeEnum.WRITE);
    }
}
