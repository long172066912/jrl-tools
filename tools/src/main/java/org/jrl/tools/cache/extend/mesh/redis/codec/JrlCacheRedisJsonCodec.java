package org.jrl.tools.cache.extend.mesh.redis.codec;

import com.fasterxml.jackson.core.type.TypeReference;
import org.jrl.tools.cache.exception.JrlCacheException;
import org.jrl.tools.cache.extend.mesh.JrlCacheMeshCodec;
import org.jrl.tools.json.JrlJsonUtil;
import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;

import java.lang.reflect.Type;

/**
 * json编解码
 *
 * @author JerryLong
 */
public class JrlCacheRedisJsonCodec<V> implements JrlCacheMeshCodec<V, String> {
    private static final Logger LOGGER = JrlLoggerFactory.getLogger(JrlCacheRedisJsonCodec.class);
    private final Type valueType;

    public JrlCacheRedisJsonCodec(Type valueType) {
        this.valueType = valueType;
    }

    @Override
    public String encode(V value) {
        if (null == value) {
            return null;
        }
        if (isWrapClass(value.getClass())) {
            return value.toString();
        }
        try {
            return JrlJsonUtil.toJson(value);
        } catch (Exception e) {
            LOGGER.error("JrlJsonUtil toJson error ! class : {}", value.getClass().getName(), e);
            throw new JrlCacheException("JrlCache json encode fail ! class : " + value.getClass().getName(), e);
        }
    }

    @Override
    public V decode(String value) {
        if (null == value) {
            return null;
        }
        if (valueType == String.class) {
            return (V) value;
        }
        try {
            return JrlJsonUtil.fromJson(value, new TypeReference<V>() {
                @Override
                public Type getType() {
                    return valueType;
                }
            });
        } catch (Exception e) {
            LOGGER.error("JrlJsonUtil fromJson error ! class : {}", valueType.getClass().getName(), e);
            throw new JrlCacheException("JrlCache json decode fail ! class : " + value.getClass().getName(), e);
        }
    }

    /**
     * 判断是否是基础类型
     *
     * @param clz 对象class信息
     * @return boolean
     */
    public static boolean isWrapClass(Class clz) {
        try {
            return clz.isEnum() || Object.class == clz || clz == String.class || clz.isPrimitive() || ((Class) clz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }
}
