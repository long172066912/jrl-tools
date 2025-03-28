package org.jrl.tools.cache.core.event.listener;

import org.jrl.tools.cache.JrlCache;
import org.jrl.tools.cache.config.DefaultJrlCacheExpireConfig;
import org.jrl.tools.cache.core.JrlCacheManager;
import org.jrl.tools.cache.core.JrlCacheProxy;
import org.jrl.tools.cache.core.builder.CacheLoadBuilder;
import org.jrl.tools.cache.core.event.model.JrlCacheDynamicEventData;
import org.jrl.tools.cache.extend.both.JrlCacheBothImpl;
import org.jrl.tools.cache.extend.local.JrlCacheCaffeineImpl;
import org.jrl.tools.cache.extend.local.JrlCacheLocalConfig;
import org.jrl.tools.cache.model.JrlCacheType;
import org.jrl.tools.event.JrlEventListener;
import org.jrl.tools.json.JrlJsonNoExpUtil;
import org.jrl.tools.log.JrlLoggerFactory;
import org.jrl.tools.utils.function.AbstractJrlFunction;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * JrlCache动态监听器
 *
 * @author JerryLong
 */
public class JrlCacheDynamicEventListener extends JrlCacheManager implements JrlEventListener<JrlCacheDynamicEventData> {

    private static Logger LOGGER = JrlLoggerFactory.getLogger(JrlCacheDynamicEventListener.class);

    public static final String JRL_CACHE_DYNAMIC = "jrlCacheDynamic";

    @Override
    public String eventName() {
        return JRL_CACHE_DYNAMIC;
    }

    @Override
    public void onEvent(JrlCacheDynamicEventData eventData) {
        LOGGER.info("jrl-cache Dynamic listener ! event : {}", JrlJsonNoExpUtil.toJson(eventData));
        String cacheName = eventData.getCacheName();
        JrlCacheProxy<Object, Object> cache = getCacheProxy(cacheName);
        //判断cache是否已经是本地缓存
        final JrlCacheType cacheType = cache.getCache().getConfig().getCacheType();
        if (!JrlCacheType.MESH.equals(cacheType)) {
            LOGGER.info("jrl-cache Dynamic listener ! cache : {} is local cache, no need to do anything", cacheName);
            return;
        }
        //构建一个多级缓存，loader是mesh cache本身
        final JrlCache<Object, Object> meshCache = cache.getCache();
        final JrlCacheLocalConfig jrlCacheLocalConfig = new JrlCacheLocalConfig(cacheName);
        jrlCacheLocalConfig.setCacheLoader(CacheLoadBuilder.builder()
                .cacheLoader(new AbstractJrlFunction<Object, Object>() {
                    @Override
                    public Object apply(Object s) {
                        return meshCache.get(s);
                    }
                })
                .build());
        if (null != eventData.getExpireSeconds() && eventData.getExpireSeconds() > 0) {
            jrlCacheLocalConfig.setExpireConfig(new DefaultJrlCacheExpireConfig(eventData.getExpireSeconds(), TimeUnit.SECONDS));
        } else {
            jrlCacheLocalConfig.setExpireConfig(DefaultJrlCacheExpireConfig.oneMinute());
        }
        jrlCacheLocalConfig.setInitialCapacity(100);
        jrlCacheLocalConfig.setMaxSize(5000);
        //构建多级缓存
        final JrlCacheBothImpl kvJrlCacheBoth = new JrlCacheBothImpl(cacheName, meshCache, new JrlCacheCaffeineImpl(cacheName, jrlCacheLocalConfig));
        resetCache(cache, kvJrlCacheBoth);
        LOGGER.info("jrl-cache Dynamic listener success ! event : {}", JrlJsonNoExpUtil.toJson(eventData));
    }
}
