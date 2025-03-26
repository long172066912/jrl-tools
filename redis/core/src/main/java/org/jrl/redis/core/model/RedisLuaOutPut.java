package org.jrl.redis.core.model;

/**
* lua结果输出类型，不建议其他类型
* @author JerryLong
*/
public enum RedisLuaOutPut {
    /**
     * 常用的字符串
     */
    VALUE,
    /**
     * 常用的数字
     */
    INTEGER,
    VOID,
    BOOLEAN,
    BOOLEAN_LIST,
    DOUBLE,
    DOUBLE_LIST,
    KEY_VALUE,
    KEY_VALUE_LIST,
    ARRAY,
    MAP,
    MULTI,
    STRING_LIST,
    GET_COORDINATES_VALUE_LIST,
    GET_COORDINATES_LIST,
    ;
}
