package org.jrl.tools.cache.core.event.listener;

import org.jrl.tools.cache.core.JrlCacheManager;
import org.jrl.tools.cache.core.JrlCacheProxy;
import org.jrl.tools.cache.core.event.model.JrlCacheHotKeyEventData;
import org.jrl.tools.cache.extend.both.JrlCacheBothHotKeyImpl;
import org.jrl.tools.cache.extend.mesh.JrlCacheMeshConfig;
import org.jrl.tools.cache.model.JrlCacheType;
import org.jrl.tools.event.JrlEventListener;
import org.jrl.tools.json.JrlJsonNoExpUtil;
import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;

/**
 * 热key监听器
 *
 * @author JerryLong
 */
public class JrlCacheHotKeyEventListener extends JrlCacheManager implements JrlEventListener<JrlCacheHotKeyEventData> {

    private static Logger LOGGER = JrlLoggerFactory.getLogger(JrlCacheHotKeyEventListener.class);

    public static final String JRL_CACHE_HOT_KEY = "jrlCacheHotKey";

    @Override
    public String eventName() {
        return JRL_CACHE_HOT_KEY;
    }

    @Override
    public void onEvent(JrlCacheHotKeyEventData eventData) {
        JrlCacheProxy cache = getCacheProxy(eventData.getCacheName());
        //判断cache是否已经是热key实现
        if (cache.getCache() instanceof JrlCacheBothHotKeyImpl) {
            ((JrlCacheBothHotKeyImpl) cache.getCache()).setHotKey(eventData.getKeys());
            return;
        }
        //如果不是分布式的，不处理
        if (!JrlCacheType.MESH.equals(cache.getCache().getConfig().getCacheType())) {
            LOGGER.warn("jrl-cache hotKey listener not support ! not mesh ! event : {}", JrlJsonNoExpUtil.toJson(eventData));
            return;
        }
        //判断是否支持热key
        final JrlCacheMeshConfig config = (JrlCacheMeshConfig) cache.getCache().getConfig();
        if (!config.getJrlCacheHotKeyConfig().isAutoCacheHotKey()) {
            LOGGER.warn("jrl-cache hotKey listener not support ! event : {}", JrlJsonNoExpUtil.toJson(eventData));
            return;
        }
        //将cache转变成热key实现
        if (null != eventData.getLocalCacheSeconds() && eventData.getLocalCacheSeconds() > 10) {
            config.getJrlCacheHotKeyConfig().setLocalCacheSeconds(eventData.getLocalCacheSeconds());
        }
        LOGGER.info("jrl-cache hotKey listener ! hotKey autoCache start ! cacheName : {} , keys : {} , localCacheSeconds : {}",
                eventData.getCacheName(), JrlJsonNoExpUtil.toJson(eventData.getKeys()), config.getJrlCacheHotKeyConfig().getLocalCacheSeconds());
        final JrlCacheBothHotKeyImpl jrlCacheBothHotKey = new JrlCacheBothHotKeyImpl(eventData.getCacheName(), cache.getCache(), config);
        jrlCacheBothHotKey.setHotKey(eventData.getKeys());
        //替换对象
        resetCache(cache, jrlCacheBothHotKey);
    }
}
