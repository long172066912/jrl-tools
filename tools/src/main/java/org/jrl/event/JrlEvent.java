package org.jrl.event;

import org.apache.commons.lang3.StringUtils;
import org.jrl.json.JrlJsonNoExpUtil;
import org.jrl.log.JrlLoggerFactory;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * 事件
 *
 * @author JerryLong
 */
public class JrlEvent<T> {

    private static Logger LOGGER = JrlLoggerFactory.getLogger(JrlEvent.class);

    /**
     * 事件名称
     */
    private final String eventName;
    /**
     * 是否异步
     */
    private final ThreadLocal<Boolean> async = new ThreadLocal<>();
    /**
     * 事件处理线程池，如果不自定义，则默认使用全局线程池
     */
    private final ExecutorService executorService;
    /**
     * 事件监听器
     */
    private Map<String, JrlEventListener<T>> listeners = new ConcurrentHashMap<>();

    protected JrlEvent(String eventName, boolean async, ExecutorService executorService) {
        this.eventName = eventName;
        this.async.set(async);
        this.executorService = executorService;
    }

    public void addListener(JrlEventListener<T> listener) {
        listeners.put(StringUtils.isNotBlank(listener.listenerName()) ? listener.listenerName() : listener.getClass().getName(), listener);
    }

    public String getEventName() {
        return eventName;
    }

    public JrlEvent<T> sync() {
        async.set(false);
        return this;
    }

    public int getListenerSize() {
        return listeners.size();
    }

    /**
     * 发布事件，如果要同步发布，请调用sync().publish
     *
     * @param eventData 事件数据
     */
    public void publish(T eventData) {
        publish(eventData, null);
    }

    public void publishToListener(T eventData, String listenerName) {
        if (null == eventData) {
            throw new IllegalArgumentException(eventName + ".eventData is null");
        }
        publish(eventData, listenerName);
    }

    /**
     * 发布事件，如果要同步发布，请调用sync().publish
     *
     * @param eventData 事件数据
     */
    private void publish(T eventData, String selectListenerName) {
        try {
            if (listeners.size() > 0) {
                if (null == eventData) {
                    LOGGER.warn("eventData or eventData.data is null , event : {} , eventData : {}", eventName, JrlJsonNoExpUtil.toJson(eventData));
                    return;
                }
                boolean selectListener = StringUtils.isNotBlank(selectListenerName);
                listeners.forEach((listenerName, listener) -> {
                    if (selectListener && !listener.listenerName().equals(selectListenerName)) {
                        return;
                    }
                    //默认使用异步模式
                    if (Optional.ofNullable(this.async.get()).orElse(true)) {
                        this.executorService.execute(() -> onEvent(listener, eventData));
                    } else {
                        onEvent(listener, eventData);
                    }
                });
            }
        } finally {
            async.remove();
        }
    }

    /**
     * 执行监听器
     *
     * @param listener  监听器
     * @param eventData 事件数据
     */
    private void onEvent(JrlEventListener<T> listener, T eventData) {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("zeus-event bus publish event ! eventName : {} , listener : {} , data : {}", listener.eventName(), listener.listenerName(), JrlJsonNoExpUtil.toJson(eventData));
            }
            listener.onEvent(eventData);
        } catch (Throwable e) {
            LOGGER.error("zeus-event bus error ! eventName : {} , listener : {} , data : {}", listener.eventName(), listener.listenerName(), JrlJsonNoExpUtil.toJson(eventData), e);
        }
    }
}
