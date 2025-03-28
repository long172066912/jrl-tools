package org.jrl.utils.cache.spi;

import org.jrl.tools.cache.extend.mesh.JrlCacheMeshConfig;
import org.jrl.tools.cache.model.JrlCacheSubscriber;
import org.jrl.tools.cache.spi.JrlCacheMeshSpi;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author JerryLong
 * spi实现
 */
public class JrlRedisMeshSpiImpl implements JrlCacheMeshSpi<String> {

    @Override
    public String get(JrlCacheMeshConfig jrlCacheMeshConfig, String key) {
        return JrlRedisClientUtils.getCacheExecutor(jrlCacheMeshConfig.getCacheSource(), jrlCacheMeshConfig.getConnectType()).get(key);
    }

    @Override
    public boolean exists(JrlCacheMeshConfig jrlCacheMeshConfig, String key) {
        return JrlRedisClientUtils.getCacheExecutor(jrlCacheMeshConfig.getCacheSource(), jrlCacheMeshConfig.getConnectType()).exists(key);
    }

    @Override
    public void put(JrlCacheMeshConfig jrlCacheMeshConfig, String key, String value, long expireTime, TimeUnit timeUnit) {
        final long expire = TimeUnit.SECONDS.convert(expireTime, timeUnit);
        JrlRedisClientUtils.getCacheExecutor(jrlCacheMeshConfig.getCacheSource(), jrlCacheMeshConfig.getConnectType()).set(key, value, (int) expire);
    }

    @Override
    public void expire(JrlCacheMeshConfig jrlCacheMeshConfig, String key, long expireTime, TimeUnit timeUnit) {
        final long expire = TimeUnit.SECONDS.convert(expireTime, timeUnit);
        JrlRedisClientUtils.getCacheExecutor(jrlCacheMeshConfig.getCacheSource(), jrlCacheMeshConfig.getConnectType()).expire(key, (int) expire);
    }

    @Override
    public void remove(JrlCacheMeshConfig jrlCacheMeshConfig, String key) {
        JrlRedisClientUtils.getCacheExecutor(jrlCacheMeshConfig.getCacheSource(), jrlCacheMeshConfig.getConnectType()).del(key);
    }

    @Override
    public Map<String, Object> getAll(JrlCacheMeshConfig jrlCacheMeshConfig, Set<String> keys) {
        return JrlRedisClientUtils.getCacheExecutor(jrlCacheMeshConfig.getCacheSource(), jrlCacheMeshConfig.getConnectType()).mget(keys.toArray(new String[0]));
    }

    @Override
    public void putAll(JrlCacheMeshConfig jrlCacheMeshConfig, Map<String, String> map, long expireTime, TimeUnit timeUnit) {
        final long expire = TimeUnit.SECONDS.convert(expireTime, timeUnit);
        JrlRedisClientUtils.getCacheExecutor(jrlCacheMeshConfig.getCacheSource(), jrlCacheMeshConfig.getConnectType()).mset((int) expire, map);
    }

    @Override
    public void removeAll(JrlCacheMeshConfig jrlCacheMeshConfig, Set<String> keys) {
        JrlRedisClientUtils.getCacheExecutor(jrlCacheMeshConfig.getCacheSource(), jrlCacheMeshConfig.getConnectType()).delBatch(keys.toArray(new String[0]));
    }

    @Override
    public void publish(JrlCacheMeshConfig jrlCacheMeshConfig, String topic, String message) {
        JrlRedisClientUtils.getCacheExecutor(jrlCacheMeshConfig.getCacheSource(), jrlCacheMeshConfig.getConnectType()).publish(topic, message);
    }

    @Override
    public void subscribe(JrlCacheMeshConfig jrlCacheMeshConfig, String topic, JrlCacheSubscriber subscriber) {
        JrlRedisClientUtils.getCacheExecutor(jrlCacheMeshConfig.getCacheSource(), jrlCacheMeshConfig.getConnectType()).subscribe((subscriber::onMessage), topic);
    }
}
