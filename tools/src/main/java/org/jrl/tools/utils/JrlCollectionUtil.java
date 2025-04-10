package org.jrl.tools.utils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 集合工具类
 *
 * @author JerryLong
 */
public class JrlCollectionUtil {

    /**
     * 分组
     *
     * @param list        待分组的集合
     * @param keyFunction 分组函数
     * @param <K>         分组类型
     * @param <V>         待分组集合类型
     * @return 分组后的Map
     */
    public static <K, V> Map<K, List<V>> group(List<V> list, Function<V, K> keyFunction) {
        return list.stream().collect(Collectors.groupingBy(keyFunction, HashMap::new, Collectors.toList()))
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * 分组并排序
     *
     * @param list        待分组的集合
     * @param keyFunction 分组函数
     * @param comparator  排序函数
     * @param <K>         分组类型
     * @param <V>         待分组集合类型
     * @return 分组后的Map
     */
    public static <K, V> Map<K, List<V>> groupAndSort(List<V> list, Function<V, K> keyFunction, Comparator<V> comparator) {
        return list.stream().collect(Collectors.groupingBy(keyFunction, HashMap::new, Collectors.toList()))
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().sorted(comparator).collect(Collectors.toList())));
    }

    /**
     * 获取Map中每个key的第一个元素
     *
     * @param data 待处理的Map
     * @param <K>  key类型
     * @param <V>  value类型
     * @return Map
     */
    public static <K, V> Map<K, V> getMapTop1(Map<K, List<V>> data) {
        return data.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));
    }

    /**
     * 获取Map中每个key的第一个元素，带条件过滤，如果条件不成立，value将是null
     *
     * @param data   待处理的Map
     * @param filter 过滤器
     * @param <K>    key类型
     * @param <V>    value类型
     * @return Map
     */
    public static <K, V> Map<K, V> getMapTop1(Map<K, List<V>> data, Predicate<? super V> filter) {
        Map<K, V> result = new HashMap<>();
        for (Map.Entry<K, List<V>> entry : data.entrySet()) {
            V vv = null;
            for (V v : entry.getValue()) {
                if (filter.test(v)) {
                    vv = v;
                }
            }
            result.put(entry.getKey(), vv);
        }
        return result;
    }

    /**
     * 过滤集合
     *
     * @param data   待过滤的集合
     * @param filter 过滤函数
     * @param <K>    分组类型
     * @param <V>    待过滤集合数据类型
     * @return 过滤后的集合
     */
    public static <K, V> Map<K, List<V>> filter(Map<K, List<V>> data, Predicate<? super V> filter) {
        return data.entrySet().stream().filter(e -> e.getValue().stream().anyMatch(filter)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
