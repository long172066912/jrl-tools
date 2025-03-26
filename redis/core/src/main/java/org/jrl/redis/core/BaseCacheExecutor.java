package org.jrl.redis.core;

import org.jrl.redis.config.BaseCacheConfig;
import org.jrl.redis.core.cache.redis.commands.RedisCommands;
import org.jrl.redis.core.cache.redis.commands.RedisLuaCommands;
import org.jrl.redis.core.cache.redis.commands.RedissonCommands;
import org.jrl.redis.core.cache.redis.lua.RedisLuaInterface;
import org.jrl.redis.core.cache.redis.lua.RedisLuaScripts;
import org.jrl.redis.core.cache.redis.redisson.RedissonClientManager;
import org.jrl.redis.core.model.CacheConfigModel;
import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RedissonClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: BaseCacheExecutor
 * @Description: 缓存命令抽象
 * @date 2021/1/22 2:40 PM
 */
public abstract class BaseCacheExecutor implements InterfaceCacheExecutor, RedisCommands, RedissonCommands, RedisLuaCommands {
    /**
     * 组件常用信息
     */
    private CacheConfigModel cacheConfigModel = new CacheConfigModel();
    /**
     * 默认的缓存命中率统计key转换器，将所有数字转成*
     */
    private static Map<String, CacheHitKeyConvertor> keyConvertors = new ConcurrentHashMap<>();

    private ThreadLocal<String> hitKeyThreadLocal = new ThreadLocal<>();

    /**
     * 连接配置
     */
    private BaseCacheConfig redisSourceConfig;

    public CacheConfigModel getCacheConfigModel() {
        return cacheConfigModel;
    }

    public void setCacheConfigModel(CacheConfigModel cacheConfigModel) {
        this.cacheConfigModel = cacheConfigModel;
    }

    public BaseCacheConfig getRedisSourceConfig() {
        return redisSourceConfig;
    }

    public void setRedisSourceConfig(BaseCacheConfig redisSourceConfig) {
        this.redisSourceConfig = redisSourceConfig;
    }

    /**
     * 获取Redisson客户端
     *
     * @return
     */
    public RedissonClient getRedissonClient() {
        return RedissonClientManager.getRedissonClient(this.getCacheConfigModel(), this.getRedisSourceConfig());
    }

    public void addLua(List<RedisLuaInterface> luas) {
        if (CollectionUtils.isNotEmpty(luas)) {
            RedisLuaScripts.addLua(luas);
            this.loadLuaScripts();
        }
    }

    public void setCacheHitKeyConvertor(CacheHitKeyConvertor keyConvertor) {
        keyConvertors.put(this.getCacheConfigModel().getCacheType(), keyConvertor);
    }

    public static CacheHitKeyConvertor getCacheHitKeyConvertor(String cacheType) {
        return keyConvertors.getOrDefault(cacheType, CacheHitKeyConvertor.DefaultCacheHitKeyConvertor.INSTANCE);
    }

    public BaseCacheExecutor hitKey(String hitKey) {
        this.hitKeyThreadLocal.set(hitKey);
        return this;
    }

    public String getHitKeyThreadLocal() {
        return hitKeyThreadLocal.get();
    }

    public void cleanHitKey() {
        hitKeyThreadLocal.remove();
    }
}
