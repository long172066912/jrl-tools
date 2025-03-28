package org.jrl.tools.cache.extend;

import com.fasterxml.jackson.core.type.TypeReference;
import org.jrl.tools.cache.config.JrlCacheConfig;
import org.jrl.tools.cache.core.JrlCacheManager;
import org.jrl.tools.cache.core.event.JrlCacheEvents;
import org.jrl.tools.cache.core.event.model.JrlCacheHotKeyEventData;
import org.jrl.tools.cache.extend.mesh.redis.JrlCacheMeshHotKey;
import org.jrl.tools.cache.extend.model.JrlCacheChannelHandleType;
import org.jrl.tools.cache.extend.model.JrlCacheSubscribeVo;
import org.jrl.tools.cache.hotkey.JrlCacheHotKeyConfig;
import org.jrl.tools.cache.hotkey.JrlCacheHotKeyStatistics;
import org.apache.commons.collections4.CollectionUtils;
import org.jrl.tools.json.JrlJsonNoExpUtil;
import org.jrl.tools.log.JrlLoggerFactory;
import org.jrl.tools.thread.JrlThreadUtil;
import org.jrl.tools.thread.core.factory.JrlThreadPoolConfig;
import org.jrl.tools.utils.JrlSunParameterizedTypeImpl;
import org.slf4j.Logger;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * JrlCache最顶级的抽象类
 *
 * @author JerryLong
 */
public abstract class AbstractJrlCache<K, V> implements BaseJrlCache<K, V> {
    private static Logger LOGGER = JrlLoggerFactory.getLogger(AbstractJrlCache.class);
    private final String cacheName;
    private final JrlCacheHotKeyStatistics<K> hotKeyStatistics;
    private int hotKeyLocalCacheSeconds = 10;
    private final ExecutorService subscribeHandlePool = JrlThreadUtil.newPool("jrl_cache_inner_channel_handle_pool", JrlThreadPoolConfig.builder().corePoolSize(2).build());
    private static final Map<JrlCacheChannelHandleType, Set<Consumer<String>>> CHANNEL_HANDLE_TYPE_SET_MAP = new ConcurrentHashMap<>();
    private final JrlCacheConfig<K, V> config;

    public AbstractJrlCache(String cacheName, JrlCacheConfig<K, V> config, JrlCacheHotKeyConfig jrlCacheHotKeyConfig) {
        if (null == jrlCacheHotKeyConfig) {
            jrlCacheHotKeyConfig = new JrlCacheHotKeyConfig();
        }
        this.config = config;
        this.cacheName = cacheName;
        hotKeyStatistics = new JrlCacheHotKeyStatistics<>(cacheName, jrlCacheHotKeyConfig, k -> new JrlCacheMeshHotKey<>(cacheName, k), this::handlerHotKeys);
    }

    @Override
    public JrlCacheConfig<K, V> getConfig() {
        return this.config;
    }

    @Override
    public V get(K key) {
        try {
            return this.getWithLock(key);
        } finally {
            hotKeyStatistics.incr(key);
        }
    }

    /**
     * 带锁的get方法，用于在分布式环境下，防止缓存击穿
     *
     * @param key
     * @return
     */
    public abstract V getWithLock(K key);

    /**
     * 获取key的类型
     *
     * @return
     */
    protected abstract Type getKeyType();

    /**
     * 获取value的类型
     *
     * @return
     */
    protected abstract Type getValueType();

    @Override
    public void statHotKey(int statSeconds) {
        hotKeyStatistics.getConfig().setStatHotKey(true);
        hotKeyStatistics.stat(statSeconds);
    }

    @Override
    public void autoCacheHotKey(int capacity, int countLeastValue, int localCacheSeconds) {
        hotKeyStatistics.getConfig().setAutoCacheHotKey(true);
        this.hotKeyLocalCacheSeconds = localCacheSeconds;
        hotKeyStatistics.setCapacity(capacity, countLeastValue, localCacheSeconds);
    }

    private void handlerHotKeys(Set<K> hotKeys) {
        //publish热key信息
        this.innerPublish(JrlCacheChannelHandleType.HOT_KEY, JrlJsonNoExpUtil.toJson(hotKeys));
    }

    @Override
    public void innerPublish(JrlCacheChannelHandleType handleType, String msg) {
        this.publish(JrlCacheManager.JRL_CACHE_INNER_CHANNEL, JrlJsonNoExpUtil.toJson(new JrlCacheSubscribeVo(this.cacheName, handleType, msg)));
    }

    @Override
    public void subscribeHotKey() {
        JrlCacheManager.addInnerSubListener(this.cacheName, JrlCacheChannelHandleType.HOT_KEY, (msg) -> {
            Set<K> hotKeys = JrlJsonNoExpUtil.fromJson(msg, new TypeReference<Set<K>>() {
                @Override
                public Type getType() {
                    final Type keyType = getKeyType();
                    return JrlSunParameterizedTypeImpl.make(Set.class, new Type[]{keyType}, keyType);
                }
            });
            if (CollectionUtils.isEmpty(hotKeys)) {
                LOGGER.warn("jrl-cache subscribe hotKey error ! msg : {}", msg);
                return;
            }
            //发送热key事件
            JrlCacheEvents.HOT_KEY_EVENT.publish(new JrlCacheHotKeyEventData<>(cacheName, hotKeyLocalCacheSeconds, hotKeys));
        });
    }
}
