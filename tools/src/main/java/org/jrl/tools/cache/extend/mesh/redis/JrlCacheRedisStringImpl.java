package org.jrl.tools.cache.extend.mesh.redis;

import org.jrl.tools.cache.JrlCacheKeyBuilder;
import org.jrl.tools.cache.core.loader.JrlCacheLoaderHandler;
import org.jrl.tools.cache.exception.JrlCacheException;
import org.jrl.tools.cache.extend.mesh.AbstractJrlCacheMesh;
import org.jrl.tools.cache.extend.mesh.JrlCacheLockConfig;
import org.jrl.tools.cache.extend.mesh.JrlCacheMeshConfig;
import org.jrl.tools.cache.extend.mesh.redis.codec.JrlCacheRedisJsonCodec;
import org.jrl.tools.cache.hotkey.JrlCacheHotKeyConfig;
import org.jrl.tools.cache.model.JrlCacheLock;
import org.jrl.tools.cache.model.JrlCacheSubscriber;
import org.jrl.tools.cache.spi.JrlCacheLockSpi;
import org.jrl.tools.cache.spi.JrlCacheMeshSpi;
import org.apache.commons.lang3.StringUtils;
import org.jrl.tools.spi.JrlSpiLoader;
import org.jrl.tools.thread.JrlThreadUtil;
import org.jrl.tools.thread.core.factory.pool.JrlPoolExecutor;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * redis 扩展实现，string类型 get、set等
 *
 * @author JerryLong
 */
public class JrlCacheRedisStringImpl<K, V> extends AbstractJrlCacheMesh<K, V> {
    private final String name;
    private final JrlCacheLockConfig lockConfig;
    private final JrlCacheMeshConfig<K, V> config;
    private final JrlCacheMeshSpi<String> cacheMeshSpi;
    private final JrlCacheLockSpi cacheLockSpi;
    private static final JrlCacheLock JRL_CACHE_LOCK = () -> {
    };
    private final JrlCacheRedisJsonCodec<V> codec;
    private final Type valueType;
    private static final String LOCK_SUFFIX = ":lock";
    /**
     * 缓存空值，用一个特殊值表示缓存空值
     */
    public static final String JRL_CACHE_MESH_NULL_VALUE = "jnuil";
    private static String nullValueEncode = null;

    private static JrlPoolExecutor executorService = JrlThreadUtil.newPool("jrl-cache-redis-executor");

    public JrlCacheRedisStringImpl(String name, JrlCacheMeshConfig<K, V> config, JrlCacheHotKeyConfig jrlCacheHotKeyConfig) {
        super(name, config, jrlCacheHotKeyConfig);
        this.name = name;
        this.config = config;
        this.lockConfig = config.getLockConfig();
        // 获取spi，找不到会抛异常
        this.cacheMeshSpi = JrlSpiLoader.getInstance(JrlCacheMeshSpi.class);
        this.cacheLockSpi = JrlSpiLoader.getInstance(JrlCacheLockSpi.class);
        this.valueType = config.getCacheLoader().getCacheLoader().getValueType();
        this.codec = new JrlCacheRedisJsonCodec<>(valueType);
    }

    @Override
    public JrlCacheMeshConfig<K, V> getConfig() {
        return config;
    }

    @Override
    public V getWithLock(K key) {
        V v1 = null;
        boolean isLoad = false;
        try {
            String k = getKey(key);
            String v = cacheMeshSpi.get(this.getConfig(), k);
            final String nullValue = getNullValue();
            if (null == v) {
                //锁
                JrlCacheLock lock = lock(key);
                try {
                    //防止缓存穿透
                    v = cacheMeshSpi.get(this.getConfig(), k);
                    if (null == v) {
                        //加载数据
                        v1 = JrlCacheLoaderHandler.load(this, config.getCacheLoader(), key);
                        isLoad = true;
                        if (null == v1) {
                            if (config.isCacheNullValue() && null != nullValue) {
                                cacheMeshSpi.put(this.getConfig(), k, nullValue, config.getExpireTime(), config.getUnit());
                            }
                        } else {
                            this.put(key, v1);
                            return v1;
                        }
                    }
                } finally {
                    lock.unlock();
                }
            }
            if (null == v) {
                return null;
            }
            //处理缓存空值
            if (null != nullValue && nullValue.equals(v)) {
                return null;
            }
            return codec.decode(v);
        } catch (Throwable e) {
            if (!this.config.isLoadWithException()) {
                throw new JrlCacheException("jrl-cache get error ! key=" + key.toString(), e);
            }
            if (isLoad) {
                return v1;
            } else {
                return JrlCacheLoaderHandler.load(this, config.getCacheLoader(), key);
            }
        }
    }

    @Override
    public V getIfPresent(K key) {
        final String v = cacheMeshSpi.get(this.getConfig(), getKey(key));
        if (null == v) {
            return null;
        }
        final String nullValue = getNullValue();
        if (StringUtils.isNotBlank(nullValue) && nullValue.equals(v)) {
            return null;
        }
        return codec.decode(v);
    }

    @Override
    public V refresh(K key) {
        //加载数据
        V v = JrlCacheLoaderHandler.load(this, config.getCacheLoader(), key, false);
        if (null == v) {
            //缓存空值
            final String nullValue = this.getNullValue();
            if (StringUtils.isNotBlank(nullValue)) {
                cacheMeshSpi.put(this.getConfig(), getKey(key), nullValue, config.getExpireTime(), config.getUnit());
            }
        } else {
            this.put(key, v);
        }
        return v;
    }

    @Override
    public boolean exists(K key) {
        return null != this.getIfPresent(key);
    }

    @Override
    public void put(K key, V value) {
        if (null == value) {
            cacheMeshSpi.remove(this.getConfig(), getKey(key));
            return;
        }
        cacheMeshSpi.put(this.getConfig(), getKey(key), codec.encode(value), config.getExpireTime(), config.getUnit());
    }

    @Override
    public void putWithLock(K key, Supplier<V> value) {
        final JrlCacheLock lock = this.lock(key);
        try {
            final V v = value.get();
            if (null != v) {
                this.put(key, v);
            } else {
                this.remove(key);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void remove(K key) {
        cacheMeshSpi.remove(this.getConfig(), getKey(key));
    }

    @Override
    public void removeWithLock(K key, Runnable runnable) {
        final JrlCacheLock lock = this.lock(key);
        try {
            runnable.run();
            this.remove(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public Map getAll(Set<K> keys) {
        final Map<String, Object> kvs = cacheMeshSpi.getAll(this.getConfig(), getKeys(keys));
        Map<String, V> result = new HashMap<>();
        List<String> emptyKeys = new ArrayList<>();
        kvs.forEach((k, v) -> {
            if (null == v) {
                //异步加载
                emptyKeys.add(k);
            } else {
                result.put(k, codec.decode(v.toString()));
            }
        });
        List<Callable<Object>> tasks = new ArrayList<>();
        for (String k : emptyKeys) {
            tasks.add(() -> {
                result.put(k, this.get((K) k));
                return 1;
            });
        }
        //使用jrl线程池处理批量异步任务
        JrlThreadUtil.executeTasks(2000, tasks, executorService);
        return result;
    }

    @Override
    public void putAll(Map<K, V> map) {
        cacheMeshSpi.putAll(this.getConfig(),
                map.entrySet().stream().collect(Collectors.toMap(entry -> getKey(entry.getKey()), entry -> codec.encode(entry.getValue()))),
                config.getExpireTime(), config.getUnit()
        );
    }

    @Override
    public void removeAll(Set<K> keys) {
        cacheMeshSpi.removeAll(this.getConfig(), getKeys(keys));
    }

    private JrlCacheLock lock(K key) {
        String k = getKey(key);
        JrlCacheLock lock;
        switch (lockConfig.getLockType()) {
            case TRY_LOCK:
                lock = cacheLockSpi.tryLock(this.getConfig(), k + LOCK_SUFFIX, lockConfig.getWaitTime(), lockConfig.getExpireTime(), lockConfig.getTimeUnit());
                break;
            case LOCK:
                lock = cacheLockSpi.lock(this.getConfig(), k + LOCK_SUFFIX, lockConfig.getExpireTime(), lockConfig.getTimeUnit());
            case NO_LOCK:
            default:
                lock = JRL_CACHE_LOCK;
                break;
        }
        return lock;
    }

    private String getNullValue() {
        if (!config.isCacheNullValue()) {
            return null;
        }
        if (StringUtils.isNotBlank(nullValueEncode)) {
            return nullValueEncode;
        }
        final V nullValue = config.getNullValue();
        if (null == nullValue) {
            return JRL_CACHE_MESH_NULL_VALUE;
        }
        synchronized (this) {
            if (StringUtils.isNotBlank(nullValueEncode)) {
                return nullValueEncode;
            }
            nullValueEncode = codec.encode(nullValue);
        }
        return nullValueEncode;
    }

    protected String getKey(K key) {
        String k;
        if (key instanceof String) {
            k = (String) key;
        } else if (key instanceof JrlCacheKeyBuilder) {
            k = ((JrlCacheKeyBuilder<K, String>) key).build();
        } else {
            throw new IllegalArgumentException("key must be String or JrlCacheKeyBuilder");
        }
        return k;
    }

    private Set<String> getKeys(Set<K> keys) {
        return keys.stream().map(this::getKey).collect(Collectors.toSet());
    }

    @Override
    public void publish(String topic, String message) {
        cacheMeshSpi.publish(this.getConfig(), topic, message);
    }

    @Override
    public void subscribe(String topic, JrlCacheSubscriber subscriber) {
        cacheMeshSpi.subscribe(this.getConfig(), topic, subscriber);
    }
}
