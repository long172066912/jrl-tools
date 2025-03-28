package org.jrl.tools.cache.spi;

import org.jrl.tools.cache.extend.mesh.JrlCacheMeshConfig;
import org.jrl.tools.cache.model.JrlCacheSubscriber;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存-分布式-spi
 *
 * @author JerryLong
 */
public interface JrlCacheMeshSpi<V> {
    /**
     * 获取缓存
     *
     * @param jrlCacheMeshConfig 缓存配置
     * @param key                 具体的key
     * @return value
     */
    V get(JrlCacheMeshConfig jrlCacheMeshConfig, String key);

    /**
     * 判断缓存是否存在
     *
     * @param jrlCacheMeshConfig 缓存配置
     * @param key                 具体的key
     * @return true存在
     */
    boolean exists(JrlCacheMeshConfig jrlCacheMeshConfig, String key);

    /**
     * 设置缓存
     *
     * @param jrlCacheMeshConfig 缓存配置
     * @param key                 具体的key
     * @param value               具体的值
     * @param expireTime          过期时间
     * @param timeUnit            时间单位
     */
    void put(JrlCacheMeshConfig jrlCacheMeshConfig, String key, V value, long expireTime, TimeUnit timeUnit);

    /**
     * 设置过期时间
     *
     * @param jrlCacheMeshConfig 缓存配置
     * @param key                 具体的key
     * @param expireTime          过期时间
     * @param timeUnit            时间单位
     */
    void expire(JrlCacheMeshConfig jrlCacheMeshConfig, String key, long expireTime, TimeUnit timeUnit);

    /**
     * 删除缓存
     *
     * @param jrlCacheMeshConfig 缓存配置
     * @param key                 具体的key
     */
    void remove(JrlCacheMeshConfig jrlCacheMeshConfig, String key);

    /**
     * 批量获取缓存
     *
     * @param jrlCacheMeshConfig 缓存配置
     * @param keys                具体的key
     * @return map
     */
    Map<String, Object> getAll(JrlCacheMeshConfig jrlCacheMeshConfig, Set<String> keys);

    /**
     * 批量设置缓存
     *
     * @param jrlCacheMeshConfig 缓存配置
     * @param map                 具体的值
     * @param expireTime          过期时间
     * @param timeUnit            时间单位
     */
    void putAll(JrlCacheMeshConfig jrlCacheMeshConfig, Map<V, V> map, long expireTime, TimeUnit timeUnit);

    /**
     * 批量删除缓存
     *
     * @param jrlCacheMeshConfig 缓存配置
     * @param keys                具体的key
     */
    void removeAll(JrlCacheMeshConfig jrlCacheMeshConfig, Set<V> keys);

    /**
     * 发布消息
     *
     * @param topic   主题
     * @param message 消息
     */
    void publish(JrlCacheMeshConfig jrlCacheMeshConfig, String topic, String message);

    /**
     * 订阅消息
     *
     * @param topic      主题
     * @param subscriber 订阅者
     */
    void subscribe(JrlCacheMeshConfig jrlCacheMeshConfig, String topic, JrlCacheSubscriber subscriber);
}
