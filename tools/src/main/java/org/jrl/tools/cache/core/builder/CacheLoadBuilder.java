package org.jrl.tools.cache.core.builder;

import org.jrl.tools.cache.JrlCacheLoader;
import org.jrl.tools.cache.core.loader.DefaultJrlCacheLoader;
import org.jrl.tools.cache.model.JrlCacheLoadType;
import org.jrl.tools.utils.function.AbstractJrlFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 缓存加载器构建器
 *
 * @author JerryLong
 */
public class CacheLoadBuilder<K, V> {
    private JrlCacheLoadType loadType = JrlCacheLoadType.EXPIRED;
    private final List<K> preLoadKeys = new ArrayList<>();
    private AbstractJrlFunction<K, V> cacheLoader;

    public static <K, V> CacheLoadBuilder<K, V> builder() {
        return new CacheLoadBuilder<>();
    }

    public CacheLoadBuilder<K, V> loadType(JrlCacheLoadType loadType) {
        this.loadType = loadType;
        return this;
    }

    public CacheLoadBuilder<K, V> preLoad(List<K> keys) {
        this.preLoadKeys.addAll(keys);
        return this;
    }

    @SafeVarargs
    public final CacheLoadBuilder<K, V> preLoad(K... keys) {
        this.preLoadKeys.addAll(Arrays.asList(keys));
        return this;
    }

    public CacheLoadBuilder<K, V> cacheLoader(AbstractJrlFunction<K, V> cacheLoader) {
        this.cacheLoader = cacheLoader;
        return this;
    }

    public JrlCacheLoader<K, V> build() {
        if (cacheLoader == null) {
            throw new IllegalArgumentException("cacheLoader is null");
        }
        return new DefaultJrlCacheLoader<>(cacheLoader, loadType, preLoadKeys);
    }
}