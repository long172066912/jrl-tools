package org.jrl.tools.cache.extend.local;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jrl.tools.cache.config.JrlCacheConfig;
import org.jrl.tools.cache.core.loader.JrlCacheLoaderHandler;
import org.jrl.tools.cache.extend.BaseJrlCache;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 本地缓存caffeine实现
 *
 * @author JerryLong
 */
public class JrlCacheCaffeineImpl<K, V> implements BaseJrlCache<K, V> {
    private final String name;
    private final Cache<K, JrlCacheData<V>> cacheClient;
    private final JrlCacheLocalConfig<K, V> config;

    public JrlCacheCaffeineImpl(String name, JrlCacheLocalConfig<K, V> config) {
        this.name = name;
        this.config = config;
        //创建cache对象
        this.cacheClient = Caffeine.newBuilder()
                .initialCapacity(config.getInitialCapacity())
                .maximumSize(config.getMaxSize())
                .expireAfterWrite(config.getExpireTime(), config.getUnit())
                .build();
    }

    @Override
    public JrlCacheConfig<K, V> getConfig() {
        return config;
    }

    @Override
    public V get(K key) {
        return Optional.ofNullable(cacheClient.get(key, this::load)).map(JrlCacheData::getData).orElse(null);
    }

    @Override
    public V getIfPresent(K key) {
        return Optional.ofNullable(cacheClient.getIfPresent(key)).map(JrlCacheData::getData).orElse(null);
    }

    @Override
    public V refresh(K key) {
        JrlCacheData<V> load = this.loadNoCache(key);
        if (null == load) {
            load = new JrlCacheData<>(null);
        }
        cacheClient.put(key, load);
        return load.getData();
    }

    @Override
    public boolean exists(K key) {
        return cacheClient.getIfPresent(key) != null;
    }

    @Override
    public void put(K key, V value) {
        if (null == value && !config.cacheNullValue()) {
            return;
        }
        cacheClient.put(key, new JrlCacheData<>(value));
    }

    @Override
    public void putWithLock(K key, Supplier<V> value) {
        synchronized (this) {
            cacheClient.put(key, new JrlCacheData<>(value.get()));
        }
    }

    @Override
    public void remove(K key) {
        cacheClient.invalidate(key);
    }

    @Override
    public void removeWithLock(K key, Runnable runnable) {
        synchronized (this) {
            runnable.run();
            cacheClient.invalidate(key);
        }
    }

    @Override
    public void close() throws IOException {
        cacheClient.cleanUp();
    }

    @Override
    public Map<K, V> getAll(Set<K> keys) {
        Map<K, V> result = new HashMap<>(keys.size());
        for (K key : keys) {
            result.put(key, get(key));
        }
        return result;
    }

    @Override
    public void putAll(Map<K, V> map) {
        cacheClient.putAll(map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new JrlCacheData<>(e.getValue()))));
    }

    @Override
    public void removeAll(Set<K> keys) {
        cacheClient.invalidateAll(keys);
    }

    /**
     * 加载数据
     *
     * @param k key
     * @return value
     */
    private JrlCacheData<V> load(K k) {
        final V v = JrlCacheLoaderHandler.load(this, config.getCacheLoader(), k);
        if (null == v && !config.cacheNullValue()) {
            return null;
        }
        return new JrlCacheData<>(v);
    }

    private JrlCacheData<V> loadNoCache(K k) {
        final V v = JrlCacheLoaderHandler.load(this, config.getCacheLoader(), k, false);
        if (null == v && !config.cacheNullValue()) {
            return null;
        }
        return new JrlCacheData<>(v);
    }
}
