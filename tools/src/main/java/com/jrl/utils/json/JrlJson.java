package com.jrl.utils.json;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.InputStream;

/**
 * json接口
 *
 * @author JerryLong
 */
public interface JrlJson<N> {

    /**
     * 从json字符串中解析对象
     *
     * @param json      json字符串
     * @param valueType 解析对象类型
     * @param <T>       class类型
     * @return 对象
     * @throws Exception 异常
     */
    <T> T fromJson(String json, Class<T> valueType) throws Exception;

    /**
     * 从json字符串中解析对象
     *
     * @param json         json字符串
     * @param valueTypeRef 解析对象类型
     * @param <T>          class类型
     * @return 反序列化对象
     * @throws Exception 异常
     */
    <T> T fromJson(String json, TypeReference<T> valueTypeRef) throws Exception;

    /**
     * 从json字符串中解析对象
     *
     * @param stream    json stream流
     * @param valueType 解析对象类型
     * @param <T>       class类型
     * @return 对象
     * @throws Exception 异常
     */
    <T> T fromJson(InputStream stream, Class<T> valueType) throws Exception;

    /**
     * 将对象转换为json字符串
     *
     * @param value 对象
     * @return json字符串
     * @throws Exception 异常
     */
    String toJson(Object value) throws Exception;

    /**
     * 读取json，转成jsonNode
     *
     * @param json json字符串
     * @return 节点
     * @throws Exception 异常
     */
    N readTree(String json) throws Exception;
}
