package org.jrl.tools.event;

/**
 * jrl事件监听器
 *
 * @author JerryLong
 */
public interface JrlEventListener<T> {
    /**
     * 监听的事件名称
     *
     * @return
     */
    String eventName();

    /**
     * 注册监听器
     */
    default void register() {
        JrlEventBus.register(this);
    }

    /**
     * 监听器名称
     *
     * @return
     */
    default String listenerName() {
        return this.getClass().getName();
    }

    /**
     * 监听到事件
     *
     * @param eventData 事件数据
     */
    void onEvent(T eventData);
}
