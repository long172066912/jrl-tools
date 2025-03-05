package org.jrl.event;

import org.jrl.log.JrlLoggerFactory;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * zeus事件总线
 *
 * @author JerryLong
 */
public class JrlEventBus {

    private static Logger LOGGER = JrlLoggerFactory.getLogger(JrlEventBus.class);

    /**
     * 事件注册表，key为事件名称，value为事件监听器列表
     */
    private static final Map<String, JrlEvent<?>> EVENT_REGISTER_MAP = new ConcurrentHashMap<>(16);

    /**
     * 注册 / 初始化事件
     *
     * @param eventName 事件
     */
    public static <T> JrlEvent<T> of(String eventName) {
        return (JrlEvent<T>) EVENT_REGISTER_MAP.computeIfAbsent(eventName, e -> {
            LOGGER.info("register event : {}", eventName);
            //todo 传入线程池
            return new JrlEvent<>(eventName, true, null);
        });
    }

    public static <T> void register(JrlEventListener<T> listener) {
        JrlEvent zeusEvent = of(listener.eventName());
        zeusEvent.addListener(listener);
    }
}
