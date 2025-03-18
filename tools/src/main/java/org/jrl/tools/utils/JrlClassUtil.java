package org.jrl.tools.utils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toCollection;

/**
 * 类class帮助类
 *
 * @author JerryLong
 */
public class JrlClassUtil {

    /**
     * 得到基本类型的默认值
     *
     * @param clazz Class类
     * @return 默认值
     */
    public static Object getDefaultPrimitiveValue(Class clazz) {
        if (clazz == int.class) {
            return 0;
        } else if (clazz == boolean.class) {
            return false;
        } else if (clazz == long.class) {
            return 0L;
        } else if (clazz == byte.class) {
            return (byte) 0;
        } else if (clazz == double.class) {
            return 0d;
        } else if (clazz == short.class) {
            return (short) 0;
        } else if (clazz == float.class) {
            return 0f;
        } else if (clazz == char.class) {
            return (char) 0;
        } else {
            return null;
        }
    }

    /**
     * 获取类所有字段列表（不包括合成字段）
     *
     * @param clazz
     * @return
     */
    public static Set<Field> getDeclaredFieldSets(Class<?> clazz) {
        return stream(clazz.getDeclaredFields()).filter(JrlClassUtil::classDeclaredFieldFilter).collect(toCollection(LinkedHashSet::new));
    }

    /**
     * 获取类所有字段列表（不包括合成字段）
     *
     * @param clazz
     * @return
     */
    public static Field[] getDeclaredFields(Class<?> clazz) {
        return stream(clazz.getDeclaredFields()).filter(JrlClassUtil::classDeclaredFieldFilter).toArray(Field[]::new);
    }

    /**
     * 判断是否是基础类型、包装类型、字符串
     *
     * @param clz
     * @return
     */
    public static boolean isWrapClass(Class clz) {
        try {
            return clz.isEnum() || Object.class == clz || clz == String.class || clz.isPrimitive() || ((Class) clz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isPackageClass(Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz) || Future.class.isAssignableFrom(clazz);
    }

    private static boolean classDeclaredFieldFilter(Field field) {
        return !(field.isSynthetic());
    }
}
