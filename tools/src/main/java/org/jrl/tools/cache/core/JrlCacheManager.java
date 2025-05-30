package org.jrl.tools.cache.core;

import org.jrl.tools.cache.JrlCache;
import org.jrl.tools.cache.config.JrlCacheExpireConfig;
import org.jrl.tools.cache.core.event.JrlCacheEventRegister;
import org.jrl.tools.cache.extend.BaseJrlCache;
import org.jrl.tools.cache.extend.model.JrlCacheChannelHandleType;
import org.jrl.tools.cache.extend.model.JrlCacheSubscribeVo;
import org.jrl.tools.cache.model.JrlCacheType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jrl.tools.json.JrlJsonNoExpUtil;
import org.jrl.tools.log.JrlLoggerFactory;
import org.jrl.tools.thread.JrlThreadUtil;
import org.jrl.tools.thread.core.factory.JrlThreadPoolConfig;
import org.slf4j.Logger;

import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 管理所有的JrlCache对象
 *
 * @author JerryLong
 */
public class JrlCacheManager {

    private static final Logger LOGGER = JrlLoggerFactory.getLogger(JrlCacheManager.class);

    private static Map<String, BaseJrlCache> jrlCacheMap = new ConcurrentHashMap<>();
    private static Map<String, JrlCacheProxy> proxyMap = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService JRL_CACHE_SCHEDULE_LOADER = JrlThreadUtil.newSchedulePool("JrlCacheScheduleLoader", 5);
    private static final ExecutorService JRL_CACHE_INNER_CHANNEL_HANDLE_POOL = JrlThreadUtil.newPool("jrl_cache_inner_channel_handle_pool", JrlThreadPoolConfig.builder().corePoolSize(2).build());
    /**
     * JrlCache统一处理的channel消息
     */
    public static final String JRL_CACHE_INNER_CHANNEL = "jrl_cache_inner_channel";
    private static final Map<String, Map<JrlCacheChannelHandleType, Set<Consumer<String>>>> CHANNEL_HANDLE_TYPE_SET_MAP = new ConcurrentHashMap<>();
    private static final AtomicBoolean INIT_FLAG = new AtomicBoolean(false);

    static {
        JrlCacheEventRegister.register();
    }

    /**
     * 获取缓存对象，如果已经存在，则直接返回，如果不存在，则创建
     *
     * @param cacheName     缓存名称
     * @param cacheSupplier 缓存对象创建函数
     * @param <K>           缓存key类型
     * @param <V>           缓存value类型
     * @return 缓存对象
     */
    public static <K, V> BaseJrlCache<K, V> getCache(String cacheName, Supplier<BaseJrlCache<K, V>> cacheSupplier) {
        return jrlCacheMap.computeIfAbsent(cacheName, k -> {
            final BaseJrlCache<K, V> cache = cacheSupplier.get();
            //根据加载类型处理 预加载、定时
            switch (cache.getConfig().getCacheLoader().loadType()) {
                case PRELOAD:
                    preload(cacheName, cache);
                    break;
                case SCHEDULED:
                    schedule(cacheName, cache);
                    break;
                case PRELOAD_SCHEDULED:
                    preload(cacheName, cache);
                    schedule(cacheName, cache);
                default:
                    break;
            }
            final JrlCacheProxy<K, V> kvJrlCacheProxy = new JrlCacheProxy<>(cache);
            proxyMap.put(cache.getConfig().name(), kvJrlCacheProxy);
            if (cache.getConfig().getCacheType().equals(JrlCacheType.MESH) || cache.getConfig().getCacheType().equals(JrlCacheType.BOTH)) {
                final String channel = JRL_CACHE_INNER_CHANNEL + cacheName;
                LOGGER.info("jrl-cache inner channel init ! channel : {}", channel);
                cache.subscribe(cacheName, JrlCacheManager::subscribeJrlCacheInnerChannel);
            }
            return (BaseJrlCache) Proxy.newProxyInstance(JrlCacheManager.class.getClassLoader(),
                    new Class[]{BaseJrlCache.class}, kvJrlCacheProxy);
        });
    }

    public static <K, V> JrlCache<K, V> getCache(String name) {
        return jrlCacheMap.get(name);
    }

    protected static <K, V> JrlCacheProxy<K, V> getCacheProxy(String name) {
        return proxyMap.get(name);
    }

    protected static <K, V> void resetCache(JrlCacheProxy<K, V> proxy, JrlCache<K, V> newCache) {
        LOGGER.info("jrl-cache replace old cache : {} , new cache : {}", proxy.getCache().getConfig().name() + ":" + proxy.getCache().getClass().getSimpleName()
                , newCache.getConfig().name() + ":" + newCache.getClass().getSimpleName());
        proxy.setCache(newCache);
    }

    private static <K, V> void preload(String name, JrlCache<K, V> cache) {
        if (null != cache.getConfig().getCacheLoader().getLoadKeys() && cache.getConfig().getCacheLoader().getLoadKeys().size() > 0) {
            LOGGER.info("jrl-cache preload start ! cache:{} , key size : {}", name, cache.getConfig().getCacheLoader().getLoadKeys().size());
            final long l = System.currentTimeMillis();
            cache.getConfig().getCacheLoader().getLoadKeys().forEach(key -> {
                cache.put(key, cache.getConfig().getCacheLoader().load(key));
            });
            LOGGER.info("jrl-cache preload end ! cache:{} , key size : {} , cost : {}", name, cache.getConfig().getCacheLoader().getLoadKeys().size(), System.currentTimeMillis() - l);
        }
    }

    private static <K, V> void schedule(String name, JrlCache<K, V> cache) {
        if (null != cache.getConfig().getCacheLoader().getLoadKeys() && cache.getConfig().getCacheLoader().getLoadKeys().size() > 0) {
            final JrlCacheExpireConfig expireConfig = cache.getConfig().expire();
            JRL_CACHE_SCHEDULE_LOADER.scheduleAtFixedRate(() -> preload(name, cache), Math.max(expireConfig.expire() - 10, 0), expireConfig.expire(), expireConfig.unit());
        }
    }

    /**
     * 内部订阅监听
     *
     * @param handleType
     * @param consumer
     */
    public static void addInnerSubListener(String cacheName, JrlCacheChannelHandleType handleType, Consumer<String> consumer) {
        CHANNEL_HANDLE_TYPE_SET_MAP.computeIfAbsent(cacheName, k -> new ConcurrentHashMap<>()).computeIfAbsent(handleType, h -> new HashSet<>()).add(consumer);
    }

    /**
     * 订阅内部频道
     *
     * @param msg
     */
    private static void subscribeJrlCacheInnerChannel(String msg) {
        if (StringUtils.isBlank(msg)) {
            return;
        }
        final JrlCacheSubscribeVo jrlCacheSubscribeVo = JrlJsonNoExpUtil.fromJson(msg, JrlCacheSubscribeVo.class);
        if (null == jrlCacheSubscribeVo) {
            LOGGER.error("jrl-cache jrl_cache_inner_channel subscribe fail ! msg : {}", msg);
            return;
        }
        final Map<JrlCacheChannelHandleType, Set<Consumer<String>>> handleTypeSetMap = CHANNEL_HANDLE_TYPE_SET_MAP.get(jrlCacheSubscribeVo.getCacheName());
        if (null == handleTypeSetMap || handleTypeSetMap.size() == 0) {
            LOGGER.error("jrl-cache jrl_cache_inner_channel subscribe fail ! no consumer ! msg : {}", msg);
            return;
        }
        final Set<Consumer<String>> consumers = handleTypeSetMap.get(jrlCacheSubscribeVo.getHandleType());
        if (CollectionUtils.isEmpty(consumers)) {
            LOGGER.error("jrl-cache jrl_cache_inner_channel subscribe fail ! no consumer ! msg : {}", msg);
            return;
        }
        for (Consumer<String> consumer : consumers) {
            JRL_CACHE_INNER_CHANNEL_HANDLE_POOL.execute(() -> consumer.accept(jrlCacheSubscribeVo.getMsg()));
        }
    }
}
