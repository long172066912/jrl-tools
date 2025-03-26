package org.jrl.redis.util;

import org.jrl.redis.core.constant.RedisMagicConstants;
import org.jrl.redis.exception.CacheExceptionFactory;
import org.jrl.tools.json.JrlJsonNoExpUtil;

import java.lang.invoke.SerializedLambda;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: CacheCommonUtils
 * @Description: 统一帮助类
 * @date 2021/4/19 3:25 PM
 */
public class CacheCommonUtils {

    /**
     * 反射对象缓存
     */
    public static Map<Object, SerializedLambda> cacheFunctionClassMap = new ConcurrentHashMap<>();

    /**
     * key1,value1,key2,value2 = > {key1=>value1,key2=>value2}
     *
     * @param keysvalues
     * @return
     */
    public static Map<String, Object> stringsToMap(String... keysvalues) {
        if (keysvalues.length % RedisMagicConstants.TWO > 0) {
            StringBuilder fields = new StringBuilder();
            for (String keysvalue : keysvalues) {
                fields.append(keysvalue);
            }
            CacheExceptionFactory.throwException(" CacheCommonUtils->stringsToMap 参数错误" + fields.toString());
            return null;
        }
        Map<String, Object> map = new HashMap<>(keysvalues.length);
        for (int i = 0; i < keysvalues.length; i++) {
            map.put(keysvalues[i], keysvalues[++i]);
        }
        return map;
    }

    public static Map<String, String> strings2Map(String... keysvalues) {
        if (keysvalues.length % RedisMagicConstants.TWO > 0) {
            StringBuilder fields = new StringBuilder();
            for (String keysvalue : keysvalues) {
                fields.append(keysvalue);
            }
            CacheExceptionFactory.throwException(" CacheCommonUtils->stringsToMap 参数错误" + fields.toString());
            return null;
        }
        Map<String, String> map = new HashMap<>(keysvalues.length);
        for (int i = 0; i < keysvalues.length; i++) {
            map.put(keysvalues[i], keysvalues[++i]);
        }
        return map;
    }


    /**
     * 对象复制
     *
     * @param source
     * @param target
     * @param <K>
     * @param <T>
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static <K, T> T copy(K source, Class<T> target) {
        return JrlJsonNoExpUtil.fromJson(JrlJsonNoExpUtil.toJson(source), target);
    }
}
