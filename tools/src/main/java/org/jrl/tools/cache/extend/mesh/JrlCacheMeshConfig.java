package org.jrl.tools.cache.extend.mesh;

import org.jrl.tools.cache.config.AbstractJrlCacheConfig;
import org.jrl.tools.cache.hotkey.JrlCacheHotKeyConfig;
import org.jrl.tools.cache.model.JrlCacheLockControlType;
import org.jrl.tools.cache.model.JrlCacheType;

import java.util.concurrent.TimeUnit;

/**
 * 缓存配置，通过配置，可以配置缓存类型等等
 *
 * @author JerryLong
 */
public class JrlCacheMeshConfig<K, V> extends AbstractJrlCacheConfig<K, V> {

    private final JrlCacheLockConfig lockConfig;
    private JrlCacheMeshConnectType connectType = JrlCacheMeshConnectType.NORMAL;
    /**
     * 缓存空值
     */
    private V nullValue;
    /**
     * 缓存源
     */
    private final String cacheSource;

    private final JrlCacheHotKeyConfig jrlCacheHotKeyConfig = new JrlCacheHotKeyConfig();

    /**
     * 如果分布式缓存加载异常时，是否使用load方法返回，默认抛异常，不用load做兜底，防止load崩溃
     */
    private boolean loadWithException = false;

    public JrlCacheMeshConfig(String name, String cacheSource, JrlCacheLockConfig lockConfig) {
        super(name);
        this.lockConfig = lockConfig;
        this.cacheSource = cacheSource;
        this.setCacheType(JrlCacheType.MESH);
    }

    @Override
    public JrlCacheLockControlType lockType() {
        return lockConfig.getLockType();
    }

    public long getExpireTime() {
        return super.expire().expire();
    }

    public TimeUnit getUnit() {
        return super.expire().unit();
    }

    public JrlCacheLockConfig getLockConfig() {
        return lockConfig;
    }

    public JrlCacheMeshConnectType getConnectType() {
        return connectType;
    }

    public void setConnectType(JrlCacheMeshConnectType connectType) {
        this.connectType = connectType;
    }

    public V getNullValue() {
        return nullValue;
    }

    public void setNullValue(V nullValue) {
        this.nullValue = nullValue;
    }

    public String getCacheSource() {
        return cacheSource;
    }

    public JrlCacheHotKeyConfig getJrlCacheHotKeyConfig() {
        return jrlCacheHotKeyConfig;
    }

    public boolean isLoadWithException() {
        return loadWithException;
    }

    public void setLoadWithException(boolean loadWithException) {
        this.loadWithException = loadWithException;
    }
}
