package org.jrl.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.jrl.json.jackson.JrlJackson;
import org.jrl.log.JrlLoggerFactory;
import org.slf4j.Logger;

import java.io.InputStream;

/**
 * json工具
 *
 * @author JerryLong
 */
public class JrlJsonNoExpUtil {

    private static final Logger LOGGER = JrlLoggerFactory.getLogger(JrlJsonNoExpUtil.class);

    private static final JrlJackson JRL_JACKSON = JrlJackson.getInstance();

    /**
     * json字符串到对象,默认配置
     *
     * @param json      json字符串
     * @param valueType 对象类型
     * @param <T>       泛型类型
     * @return 对象
     */
    public static <T> T fromJson(String json, Class<T> valueType) {
        try {
            return JRL_JACKSON.fromJson(json, valueType);
        } catch (Exception e) {
            LOGGER.error("JrlJsonNoExpUtil fromJson error ! json : {} , class : {}", json, valueType.getName(), e);
            return null;
        }
    }

    /**
     * json字符串到对象,默认配置
     *
     * @param json         json字符串
     * @param valueTypeRef 例如: {@code new TypeReference<Map<String, Att>>(){}}
     * @param <T>          泛型类型
     * @return 对象
     */
    public static <T> T fromJson(String json, TypeReference<T> valueTypeRef) {
        try {
            return JRL_JACKSON.fromJson(json, valueTypeRef);
        } catch (Exception e) {
            LOGGER.error("JrlJsonNoExpUtil fromJson error ! json : {} , class : {}", json, valueTypeRef.getType().getClass().getName(), e);
            return null;
        }
    }

    /**
     * json流转对象
     *
     * @param stream    数据流
     * @param valueType 对象类型
     * @param <T>       泛型类型
     * @return 对象
     */
    public static <T> T fromJson(InputStream stream, Class<T> valueType) {
        try {
            return JRL_JACKSON.fromJson(stream, valueType);
        } catch (Exception e) {
            LOGGER.error("JrlJsonNoExpUtil fromJson by stream error ! class : {}", valueType.getName(), e);
            return null;
        }
    }

    /**
     * 对象到json字符串,默认配置
     * - 属性为NULL不被序列化
     * - java.sql.Date format yyyy-MM-dd
     *
     * @param value 对象
     * @return json字符串
     */
    public static String toJson(Object value) {
        try {
            return JRL_JACKSON.toJson(value);
        } catch (Exception e) {
            LOGGER.error("JrlJsonNoExpUtil toJson error ! class : {}", value.getClass().getName(), e);
            return null;
        }
    }

    /**
     * 将 json 转成 JsonNode
     *
     * @param json json字符串
     * @return 节点
     */
    public static JsonNode readTree(String json) {
        try {
            return JRL_JACKSON.readTree(json);
        } catch (Exception e) {
            LOGGER.error("JrlJsonNoExpUtil readTree error ! json : {}", json, e);
            return null;
        }
    }
}
