package org.jrl.tools.cache.model;

/**
 * 缓存pubsub消费
 *
 * @author JerryLong
 */
@FunctionalInterface
public interface JrlCacheSubscriber {
    /**
     * 对message的处理，业务自己实现
     *
     * @param message
     */
    void onMessage(String message);
}