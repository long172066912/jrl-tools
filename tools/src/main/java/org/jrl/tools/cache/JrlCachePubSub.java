package org.jrl.tools.cache;

import org.jrl.tools.cache.model.JrlCacheSubscriber;

/**
 * 缓存接口，发布订阅
 *
 * @author JerryLong
 */
public interface JrlCachePubSub<K, V> {

    /**
     * 发布消息
     *
     * @param topic   主题
     * @param message 消息
     */
    void publish(String topic, String message);

    /**
     * 订阅消息
     *
     * @param topic      主题
     * @param subscriber 订阅处理
     */
    void subscribe(String topic, JrlCacheSubscriber subscriber);
}
