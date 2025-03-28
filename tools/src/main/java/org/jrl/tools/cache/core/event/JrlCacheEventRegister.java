package org.jrl.tools.cache.core.event;

import org.jrl.tools.cache.core.event.listener.JrlCacheDynamicEventListener;
import org.jrl.tools.cache.core.event.listener.JrlCacheHotKeyEventListener;
import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;

/**
 * 缓存事件注册器
 *
 * @author JerryLong
 */
public class JrlCacheEventRegister {

    private static final Logger LOGGER = JrlLoggerFactory.getLogger(JrlCacheEventRegister.class);

    private static final JrlCacheHotKeyEventListener HOT_KEY_EVENT_LISTENER = new JrlCacheHotKeyEventListener();
    private static final JrlCacheDynamicEventListener DYNAMIC_EVENT_LISTENER = new JrlCacheDynamicEventListener();

    /**
     * 初始化缓存事件监听器
     */
    public static void register() {
        LOGGER.info("JrlCacheManager init events ！HOT_KEY , DYNAMIC");
        HOT_KEY_EVENT_LISTENER.register();
        DYNAMIC_EVENT_LISTENER.register();
    }
}
