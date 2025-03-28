package org.jrl.tools.cache.extend.both;

import com.fasterxml.jackson.core.type.TypeReference;
import org.jrl.tools.cache.JrlCache;
import org.jrl.tools.cache.config.AbstractJrlCacheConfig;
import org.jrl.tools.cache.core.JrlCacheManager;
import org.jrl.tools.cache.extend.AbstractJrlCache;
import org.jrl.tools.cache.extend.BaseJrlCache;
import org.jrl.tools.cache.extend.mesh.JrlCacheMeshConfig;
import org.jrl.tools.cache.extend.model.JrlCacheChannelHandleType;
import org.jrl.tools.cache.model.JrlCacheSubscriber;
import org.jrl.tools.cache.model.JrlCacheType;
import org.apache.commons.collections4.CollectionUtils;
import org.jrl.tools.json.JrlJsonNoExpUtil;
import org.jrl.tools.log.JrlLoggerFactory;
import org.jrl.tools.utils.JrlSunParameterizedTypeImpl;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Supplier;

/**
 * 多级缓存抽象类，通过事件通知，实现本地缓存数据删除
 *
 * @author JerryLong
 */
public abstract class AbstractJrlBothCache<K, V> extends AbstractJrlCache<K, V> {

    private static Logger LOGGER = JrlLoggerFactory.getLogger(AbstractJrlBothCache.class);

    protected final String name;
    protected final JrlCache<K, V> localCache;
    protected final BaseJrlCache<K, V> meshCache;
    private static final String POD_UNIQUE_ID = UUID.randomUUID().toString().replaceAll("-", "");
    private static final ThreadLocal<Boolean> IS_SUBSCRIBE_THREAD_LOCAL = ThreadLocal.withInitial(() -> false);

    public AbstractJrlBothCache(String name, JrlCache<K, V> meshCache, JrlCache<K, V> localCache) {
        super(name, new JrlCacheBothConfig<>(name, localCache.getConfig(), meshCache.getConfig()), ((JrlCacheMeshConfig<K, V>) meshCache.getConfig()).getJrlCacheHotKeyConfig());
        this.name = name;
        this.meshCache = (BaseJrlCache<K, V>) meshCache;
        ((AbstractJrlCacheConfig<K, V>) this.meshCache.getConfig()).setCacheType(JrlCacheType.BOTH);
        this.localCache = localCache;
        //消费变更事件
        JrlCacheManager.addInnerSubListener(this.name, JrlCacheChannelHandleType.REFRESH, this::refresh);
    }

    private void publishChangeMessage(Set<K> keys) {
        this.innerPublish(JrlCacheChannelHandleType.REFRESH, JrlJsonNoExpUtil.toJson(new JrlCacheChangeMessage<>(POD_UNIQUE_ID, keys)));
    }

    private void refresh(String message) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("jrl-cache subscribe change message : {}", message);
        }
        JrlCacheChangeMessage<K> data = JrlJsonNoExpUtil.fromJson(message, new TypeReference<JrlCacheChangeMessage<K>>() {
            @Override
            public Type getType() {
                return JrlSunParameterizedTypeImpl.make(JrlCacheChangeMessage.class,
                        new Type[]{meshCache.getConfig().getCacheLoader().getCacheLoader().getKeyType()},
                        meshCache.getConfig().getCacheLoader().getCacheLoader().getKeyType());
            }
        });
        if (null == data) {
            LOGGER.error("jrl-cache subscribe change message error ! message : {}", message);
            return;
        }
        //如果节点是本身，不处理
        if (data.getPodUniqueId().equals(POD_UNIQUE_ID)) {
            return;
        }
        IS_SUBSCRIBE_THREAD_LOCAL.set(true);
        try {
            this.removeAll(data.getKeys());
        } catch (Throwable e) {
            LOGGER.error("jrl-cache subscribe change message error ! message : {}", message);
        } finally {
            IS_SUBSCRIBE_THREAD_LOCAL.remove();
        }
    }

    @Override
    public V getWithLock(K key) {
        return localCache.get(key);
    }

    @Override
    public V getIfPresent(K key) {
        return localCache.getIfPresent(key);
    }

    @Override
    public boolean exists(K key) {
        return localCache.exists(key);
    }

    @Override
    public Map<K, V> getAll(Set<K> keys) {
        return localCache.getAll(keys);
    }

    @Override
    public V refresh(K key) {
        final V v = meshCache.refresh(key);
        if (null != v) {
            localCache.put(key, v);
        }
        return v;
    }

    @Override
    public void put(K key, V value) {
        meshCache.put(key, value);
        localCache.put(key, value);
        publishChangeMessage(new HashSet<>(Collections.singletonList(key)));
    }

    @Override
    public void remove(K key) {
        meshCache.remove(key);
        localCache.remove(key);
        publishChangeMessage(new HashSet<>(Collections.singletonList(key)));
    }

    @Override
    public void putAll(Map<K, V> map) {
        if (null == map || map.isEmpty()) {
            return;
        }
        meshCache.putAll(map);
        localCache.putAll(map);
        publishChangeMessage(map.keySet());
    }

    @Override
    public void removeAll(Set<K> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        meshCache.removeAll(keys);
        localCache.removeAll(keys);
        publishChangeMessage(keys);
    }

    @Override
    public void putWithLock(K key, Supplier<V> value) {
        meshCache.putWithLock(key, () -> {
            V v = value.get();
            localCache.put(key, v);
            publishChangeMessage(new HashSet<>(Collections.singletonList(key)));
            return v;
        });
    }

    @Override
    public void removeWithLock(K key, Runnable runnable) {
        meshCache.removeWithLock(key, () -> {
            runnable.run();
            localCache.remove(key);
            publishChangeMessage(new HashSet<>(Collections.singletonList(key)));
        });
    }

    @Override
    public void close() throws IOException {
        meshCache.close();
        localCache.close();
    }

    @Override
    public void publish(String topic, String message) {
        this.meshCache.publish(topic, message);
    }

    @Override
    public void subscribe(String topic, JrlCacheSubscriber subscriber) {
        this.meshCache.subscribe(topic, subscriber);
    }

    private static class JrlCacheChangeMessage<K> implements Serializable {
        private String podUniqueId;
        private Set<K> keys;

        public JrlCacheChangeMessage() {
        }

        public JrlCacheChangeMessage(String podUniqueId, Set<K> keys) {
            this.podUniqueId = podUniqueId;
            this.keys = keys;
        }

        public String getPodUniqueId() {
            return podUniqueId;
        }

        public void setPodUniqueId(String podUniqueId) {
            this.podUniqueId = podUniqueId;
        }

        public Set<K> getKeys() {
            return keys;
        }

        public void setKeys(Set<K> keys) {
            this.keys = keys;
        }
    }

    @Override
    protected Type getKeyType() {
        return meshCache.getConfig().getCacheLoader().getCacheLoader().getKeyType();
    }

    @Override
    protected Type getValueType() {
        return meshCache.getConfig().getCacheLoader().getCacheLoader().getValueType();
    }
}
