package org.jrl.tools.cache.extend.both;

import org.jrl.tools.cache.JrlCache;
import org.jrl.tools.cache.JrlCacheUtil;
import org.jrl.tools.cache.config.DefaultJrlCacheExpireConfig;
import org.jrl.tools.cache.core.builder.CacheLoadBuilder;
import org.jrl.tools.cache.extend.local.JrlCacheLocalBuilder;
import org.jrl.tools.cache.extend.mesh.JrlCacheMeshConfig;
import org.jrl.tools.cache.model.JrlCacheType;
import org.apache.commons.collections4.CollectionUtils;
import org.jrl.tools.json.JrlJsonNoExpUtil;
import org.jrl.tools.log.JrlLoggerFactory;
import org.jrl.tools.utils.function.AbstractJrlFunction;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 多级缓存，热key实现，只操作统计通知的热key，通过事件通知，实现本地缓存数据删除
 *
 * @author JerryLong
 */
public class JrlCacheBothHotKeyImpl<K, V> extends AbstractJrlBothCache<K, V> {

    private static Logger LOGGER = JrlLoggerFactory.getLogger(JrlCacheBothHotKeyImpl.class);

    private final JrlCache<K, K> hotKeyLocalCache;

    public JrlCacheBothHotKeyImpl(String name, JrlCache<K, V> meshCache, JrlCacheMeshConfig<K, V> config) {
        super(name, meshCache, JrlCacheUtil.getLocalCache(JrlCacheType.LOCAL.getPrefix() + meshCache.getConfig().name()
                , meshCache::get
                , new DefaultJrlCacheExpireConfig(config.getJrlCacheHotKeyConfig().getLocalCacheSeconds(), TimeUnit.SECONDS)
        ));
        hotKeyLocalCache = new JrlCacheLocalBuilder<K, K>(JrlCacheType.LOCAL.getPrefix() + name + "-hotKeys")
                .cacheLoader(CacheLoadBuilder.<K, K>builder()
                        .cacheLoader(new AbstractJrlFunction<K, K>() {
                            @Override
                            public K apply(K s) {
                                return s;
                            }
                        })
                        .build())
                .expire(new DefaultJrlCacheExpireConfig(config.getJrlCacheHotKeyConfig().getLocalCacheSeconds(), TimeUnit.SECONDS))
                .cacheNullValue(false)
                .maxSize(5000)
                .initialCapacity(100)
                .build();
    }

    public void setHotKey(Set<K> key) {
        if (CollectionUtils.isEmpty(key)) {
            return;
        }
        LOGGER.info("jrl-cache HotKeyCache : {} add hot key : {}", this.name, JrlJsonNoExpUtil.toJson(key));
        for (K k : key) {
            hotKeyLocalCache.put(k, k);
        }
        LOGGER.info("jrl-cache HotKeyCache : {} add hot key success ! size : {}", this.name, key.size());
    }

    @Override
    public V getWithLock(K key) {
        return isHotKey(key) ? localCache.get(key) : meshCache.get(key);
    }

    @Override
    public V getIfPresent(K key) {
        return isHotKey(key) ? localCache.getIfPresent(key) : meshCache.getIfPresent(key);
    }

    @Override
    public boolean exists(K key) {
        return isHotKey(key) ? localCache.exists(key) : meshCache.exists(key);
    }

    @Override
    public Map<K, V> getAll(Set<K> keys) {
        //对key根据热key进行分组
        Set<K> hotKeyGroup = new HashSet<>();
        Set<K> meshKeyGroup = new HashSet<>();
        for (K key : keys) {
            if (isHotKey(key)) {
                hotKeyGroup.add(key);
            } else {
                meshKeyGroup.add(key);
            }
        }
        final Map<K, V> map = new HashMap<>();
        map.putAll(localCache.getAll(hotKeyGroup));
        map.putAll(meshCache.getAll(meshKeyGroup));
        return map;
    }

    private boolean isHotKey(K key) {
        return hotKeyLocalCache.exists(key);
    }

    @Override
    public void close() throws IOException {
        super.close();
        hotKeyLocalCache.close();
    }
}
