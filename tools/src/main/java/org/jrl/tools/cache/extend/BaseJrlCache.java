package org.jrl.tools.cache.extend;

import org.jrl.tools.cache.JrlCache;
import org.jrl.tools.cache.JrlCachePubSub;
import org.jrl.tools.cache.extend.model.JrlCacheChannelHandleType;
import org.jrl.tools.cache.model.JrlCacheSubscriber;

/**
 * 实际的缓存接口
 *
 * @author JerryLong
 */
public interface BaseJrlCache<K, V> extends JrlCache<K, V>, JrlCachePubSub<K, V> {

    @Override
    default void publish(String topic, String message) {

    }

    @Override
    default void subscribe(String topic, JrlCacheSubscriber subscriber) {

    }

    /**
     * 开启热key统计（mesh时生效）
     *
     * @param statSeconds 统计时间，秒
     */
    default void statHotKey(int statSeconds) {

    }

    /**
     * 自动缓存热key（mesh时生效）
     *
     * @param capacity          每次统计容量
     * @param countLeastValue   最少缓存数量
     * @param localCacheSeconds 本地缓存时间
     */
    default void autoCacheHotKey(int capacity, int countLeastValue, int localCacheSeconds) {

    }

    /**
     * 内部发布消息
     *
     * @param handleType
     * @param msg
     */
    default void innerPublish(JrlCacheChannelHandleType handleType, String msg) {

    }

    /**
     * 订阅热key（mesh时生效）
     */
    default void subscribeHotKey() {

    }
}
