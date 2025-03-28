package org.jrl.tools.cache.core.event;

import org.jrl.tools.cache.core.event.listener.AbstractJrlCacheHandleEventListener;
import org.jrl.tools.cache.core.event.listener.JrlCacheDynamicEventListener;
import org.jrl.tools.cache.core.event.listener.JrlCacheHotKeyEventListener;
import org.jrl.tools.cache.core.event.model.JrlCacheDynamicEventData;
import org.jrl.tools.cache.core.event.model.JrlCacheHandleEventData;
import org.jrl.tools.cache.core.event.model.JrlCacheHotKeyEventData;
import org.jrl.tools.event.JrlEvent;
import org.jrl.tools.event.JrlEventBus;

/**
 * 命令事件
 *
 * @author JerryLong
 */
public class JrlCacheEvents {
    /**
     * 命令处理事件
     */
    public static final JrlEvent<JrlCacheHandleEventData> HANDLE_EVENT = JrlEventBus.of(AbstractJrlCacheHandleEventListener.CACHE_HANDLE);

    /**
     * 动态缓存事件
     */
    public static final JrlEvent<JrlCacheDynamicEventData> DYNAMIC_EVENT = JrlEventBus.of(JrlCacheDynamicEventListener.JRL_CACHE_DYNAMIC);

    /**
     * 热key事件
     */
    public static final JrlEvent<JrlCacheHotKeyEventData> HOT_KEY_EVENT = JrlEventBus.of(JrlCacheHotKeyEventListener.JRL_CACHE_HOT_KEY);
}
