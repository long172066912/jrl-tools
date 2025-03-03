package org.jrl.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.jrl.json.jackson.JrlJackson;
import org.jrl.log.JrlLoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * json工具
 *
 * @author JerryLong
 */
public class JrlJsonUtil {

    private static final Logger LOGGER = JrlLoggerFactory.getLogger(JrlJsonUtil.class);

    private static final JrlJackson JRL_JACKSON = JrlJackson.getInstance();

    /**
     * json字符串到对象,默认配置
     *
     * @param json      json字符串
     * @param valueType 对象类型
     * @param <T>       泛型类型
     * @return 对象
     * @throws Exception IO异常
     */
    public static <T> T fromJson(String json, Class<T> valueType) throws Exception {
        return JRL_JACKSON.fromJson(json, valueType);
    }

    /**
     * json字符串到对象,默认配置
     *
     * @param json         json字符串
     * @param valueTypeRef 例如{@code new TypeReference<Map<String, Att>>(){}}
     * @param <T>          泛型类型
     * @return 对象
     * @throws Exception IO异常
     */
    public static <T> T fromJson(String json, TypeReference<T> valueTypeRef) throws Exception {
        return JRL_JACKSON.fromJson(json, valueTypeRef);
    }

    /**
     * json流转对象
     *
     * @param stream    数据流
     * @param valueType 对象类型
     * @param <T>       泛型类型
     * @return 对象
     * @throws IOException IO异常
     */
    public static <T> T fromJson(InputStream stream, Class<T> valueType) throws Exception {
        return JRL_JACKSON.fromJson(stream, valueType);
    }

    /**
     * 对象到json字符串,默认配置
     * - 属性为NULL不被序列化
     * - java.sql.Date format yyyy-MM-dd
     *
     * @param value 对象
     * @return json字符串
     * @throws Exception IO异常
     */
    public static String toJson(Object value) throws Exception {
        return JRL_JACKSON.toJson(value);
    }

    /**
     * 将 json 转成 JsonNode
     *
     * @param json json字符串
     * @return 节点
     * @throws Exception IO异常
     */
    public static JsonNode readTree(String json) throws Exception {
        return JRL_JACKSON.readTree(json);
    }
}
