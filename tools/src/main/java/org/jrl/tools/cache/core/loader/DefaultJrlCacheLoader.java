package org.jrl.tools.cache.core.loader;

import org.jrl.tools.cache.JrlCacheLoader;
import org.jrl.tools.cache.model.JrlCacheLoadType;
import org.jrl.tools.utils.function.AbstractJrlFunction;

import java.util.List;

/**
 * 默认缓存数据加载器
 *
 * @author JerryLong
 */
public class DefaultJrlCacheLoader<K, V> implements JrlCacheLoader<K, V> {
    private List<K> loadKeys;
    private final AbstractJrlFunction<K, V> cacheLoader;
    private final JrlCacheLoadType loadType;

    public DefaultJrlCacheLoader(AbstractJrlFunction<K, V> cacheLoader, JrlCacheLoadType loadType, List<K> loadKeys) {
        this.cacheLoader = cacheLoader;
        this.loadType = loadType;
        this.loadKeys = loadKeys;
    }

    @Override
    public AbstractJrlFunction<K, V> getCacheLoader() {
        return cacheLoader;
    }

    @Override
    public V load(K key) {
        return cacheLoader.apply(key);
    }

    @Override
    public JrlCacheLoadType loadType() {
        return loadType;
    }

    @Override
    public void loadKeys(List<K> keys) {
        this.loadKeys = keys;
    }

    @Override
    public List<K> getLoadKeys() {
        return loadKeys;
    }
}
