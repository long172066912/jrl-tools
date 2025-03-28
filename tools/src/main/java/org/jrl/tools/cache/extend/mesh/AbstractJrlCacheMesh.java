package org.jrl.tools.cache.extend.mesh;

import org.jrl.tools.cache.extend.AbstractJrlCache;
import org.jrl.tools.cache.hotkey.JrlCacheHotKeyConfig;

import java.lang.reflect.Type;

/**
 * 分布式cache抽象类
 *
 * @author JerryLong
 */
public abstract class AbstractJrlCacheMesh<K, V> extends AbstractJrlCache<K, V> {

    public AbstractJrlCacheMesh(String cacheName, JrlCacheMeshConfig<K, V> config, JrlCacheHotKeyConfig jrlCacheHotKeyConfig) {
        super(cacheName, config, jrlCacheHotKeyConfig);
    }

    @Override
    protected Type getKeyType() {
        return this.getConfig().getCacheLoader().getCacheLoader().getKeyType();
    }

    @Override
    protected Type getValueType() {
        return this.getConfig().getCacheLoader().getCacheLoader().getValueType();
    }
}
