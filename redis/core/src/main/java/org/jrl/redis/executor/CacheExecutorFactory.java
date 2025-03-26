package org.jrl.redis.executor;

import org.jrl.redis.config.BaseCacheConfig;
import org.jrl.redis.connect.RedisConnectionManager;
import org.jrl.redis.connect.scheduled.HeartCheckScheduled;
import org.jrl.redis.core.BaseCacheExecutor;
import org.jrl.redis.core.cache.redis.jedis.JedisHandleExecutor;
import org.jrl.redis.core.cache.redis.lettuce.LettuceHandleExecutor;
import org.jrl.redis.core.constant.ConnectTypeEnum;
import org.jrl.redis.core.constant.RedisClientConstants;
import org.jrl.redis.core.model.CacheConfigModel;
import org.jrl.redis.exception.CacheExceptionFactory;
import org.jrl.redis.util.CacheConfigUtils;
import org.apache.commons.lang3.StringUtils;
import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: CacheExecutorFactory
 * @Description: 缓存执行器
 * @date 2021/1/19 3:44 PM
 */
public class CacheExecutorFactory {

    private static final Logger LOGGER = JrlLoggerFactory.getLogger(CacheExecutorFactory.class);
    /**
     * 心跳检测
     */
    private static final HeartCheckScheduled HEART_CHECK_SCHEDULED = new HeartCheckScheduled();
    /**
     * 保存执行器副本
     */
    protected static Map<String, BaseCacheExecutor> executorMap = new ConcurrentHashMap<>();
    /**
     * cacheType与redis地址对应关系
     */
    private static Map<String, List<String>> cacheTypeHosts = new ConcurrentHashMap<>();
    /**
     * 配置的对应关系
     */
    private static Map<String, BaseCacheConfig> cacheConfigMap = new ConcurrentHashMap<>();

    public static List<String> getHostsByCacheType(String cacheType) {
        return cacheTypeHosts.get(cacheType);
    }

    /**
     * 获取默认单节点连接信息
     *
     * @param cacheType
     * @return
     */
    public static String getDefaultHost(String cacheType) {
        List<String> hosts = CacheExecutorFactory.getHostsByCacheType(cacheType);
        String host = hosts.get(0);
        int index = host.indexOf(".");
        if (index > 0) {
            return host.substring(0, index);
        }
        return host;
    }

    public static BaseCacheConfig getRedisSourceConfig(CacheConfigModel cacheConfigModel) {
        return cacheConfigMap.get(getKey(cacheConfigModel));
    }

    /**
     * 获取执行器，通过默认配置
     *
     * @param redisSourceConfig
     * @param cacheConfigModel
     * @return
     */
    public static BaseCacheExecutor getCacheExecutor(BaseCacheConfig redisSourceConfig, CacheConfigModel cacheConfigModel) {
        if (null == cacheConfigModel || StringUtils.isBlank(cacheConfigModel.getCacheType()) || cacheConfigModel.getClientType() <= 0) {
            CacheExceptionFactory.throwException("CacheExecutorFactory->getCacheExecutor fail ! cacheConfigModel error ! cacheConfigModel:", null != cacheConfigModel ? cacheConfigModel.toString() : "null");
            return null;
        }

        //禁止JedisSimple方式连接
        if (cacheConfigModel.getConnectTypeEnum() == ConnectTypeEnum.SIMPLE && cacheConfigModel.getClientType() == RedisClientConstants.JEDIS) {
            CacheExceptionFactory.throwException("禁止通过Jedis普通方式连接，非线程安全！");
            return null;
        }

        //保持单例
        String key = getKey(cacheConfigModel);
        if (null != executorMap.get(key)) {
            return executorMap.get(key);
        }

        if (null == redisSourceConfig) {
            CacheExceptionFactory.throwException("CacheExecutorFactory->getCacheExecutor fail ! redisSourceConfig is null ! cacheConfigModel:", cacheConfigModel.toString());
            return null;
        }

        BaseCacheExecutor baseCacheExecutor = getCacheExecutorByClientType(cacheConfigModel);
        baseCacheExecutor.setCacheConfigModel(cacheConfigModel);
        baseCacheExecutor.setRedisSourceConfig(redisSourceConfig);
        baseCacheExecutor.setConnectionResource(RedisConnectionManager.getConnectionResource(cacheConfigModel, redisSourceConfig));

        after(key, redisSourceConfig, cacheConfigModel, baseCacheExecutor);
        return baseCacheExecutor;
    }

    private static String getKey(CacheConfigModel cacheConfigModel) {
        return CacheConfigUtils.modelToHashKeyNoUseType(cacheConfigModel);
    }

    private static BaseCacheExecutor getCacheExecutorByClientType(CacheConfigModel cacheConfigModel) {
        switch (cacheConfigModel.getClientType()) {
            case RedisClientConstants.JEDIS:
                return new JedisHandleExecutor();
            case RedisClientConstants.LETTUCE:
                return new LettuceHandleExecutor();
            default:
                CacheExceptionFactory.throwException("CacheExecutorFactory->getCacheExecutorByClientType error ! 不存在的连接方式:" + cacheConfigModel.getClientType());
                return null;
        }
    }

    /**
     * 执行器初始化后的操作
     *
     * @param key
     * @param redisSourceConfig
     * @param cacheConfigModel
     * @param baseCacheExecutor
     */
    private static void after(String key, BaseCacheConfig redisSourceConfig, CacheConfigModel cacheConfigModel, BaseCacheExecutor baseCacheExecutor) {
        executorMap.put(key, baseCacheExecutor);
        cacheTypeHosts.put(cacheConfigModel.getCacheType(), redisSourceConfig.getHosts());
        cacheConfigMap.put(key, redisSourceConfig);
        /**
         * 缓存lua脚本
         */
        baseCacheExecutor.loadLuaScripts();
    }
}
