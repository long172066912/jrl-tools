package org.jrl.redis.extend.config;

import org.jrl.redis.config.BaseCacheConfig;
import org.jrl.redis.config.CacheBasicConfig;
import org.jrl.redis.config.manager.AbstractConfigManager;
import org.jrl.redis.connect.RedisConnectionManager;
import org.jrl.redis.core.cache.redis.redisson.RedissonClientManager;
import org.jrl.redis.core.constant.CacheConfigSourceTypeEnum;
import org.jrl.redis.core.model.CacheConfigModel;
import org.jrl.redis.exception.CacheExceptionFactory;
import org.jrl.redis.extend.config.model.RedisDbConfigModel;
import org.jrl.redis.util.CacheConfigUtils;
import org.jrl.redis.util.async.AsyncExecutorUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.jrl.redis.core.constant.ConnectTypeEnum;
import org.jrl.tools.json.JrlJsonNoExpUtil;
import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: DbCacheConfigManager
 * @Description: 获取缓存DB配置，应该有统一抽象类，此为DB方式的实现
 * @date 2021/1/28 9:01 PM
 */
public class DbCacheConfigManager extends AbstractConfigManager {

    private static final Logger LOGGER = JrlLoggerFactory.getLogger(DbCacheConfigManager.class);

    /**
     * 数据库配置
     */
    protected static Map<String, RedisDbConfigModel> redisDbConfigModelMap = new ConcurrentHashMap<>();

    public DbCacheConfigManager() {
        //创建对象后启动定时
        this.dbSourceScheduled();
    }

    private void dbSourceScheduled() {
        /**
         * 检测DB配置是否有变更
         * 如果有变更，则创建新的连接替换原有的
         * 间隔执行10秒
         */
        AsyncExecutorUtils.submitScheduledTask(() -> {
            //获取当前配置
            CacheConfigModel cacheConfigModel = null;
            String hashKey = null;
            RedisDbConfigModel oldConfig = null;
            RedisDbConfigModel newConfig = null;
            for (String s : redisDbConfigModelMap.keySet()) {
                try {
                    cacheConfigModel = CacheConfigUtils.hashKeyToModel(s);
                    hashKey = CacheConfigUtils.modelToHashKeyNoUseType(cacheConfigModel);
                    if (cacheConfigModel.getConfigSourceType() == CacheConfigSourceTypeEnum.DB) {
                        //获取DB配置,对比是否变更，如果变更
                        oldConfig = redisDbConfigModelMap.get(hashKey);
                        newConfig = getRedisDbConfigModelByDb(cacheConfigModel, false);
                        if (CacheConfigUtils.checkDbConfigIsChange(oldConfig, newConfig)) {
                            LOGGER.info("DbSourceScheduled config change ! oldConfig:{},newConfig:{}", oldConfig.getConfigList().get(0).getRedisHost(), newConfig.getConfigList().get(0).getRedisHost());
                            //清理旧配置
                            redisDbConfigModelMap.remove(hashKey);
                            //替换连接
                            RedisConnectionManager.resetConnectionResource(cacheConfigModel, this.getConfig(cacheConfigModel));
                            //删除Redisson连接
                            RedissonClientManager.close(cacheConfigModel);
                            //重置配置
                            redisDbConfigModelMap.put(hashKey, newConfig);
                            LOGGER.info("DbSourceScheduled config change success ! oldConfig:{},newConfig:{}", oldConfig.getConfigList().get(0).getRedisHost(), newConfig.getConfigList().get(0).getRedisHost());
                        }
                    }
                } catch (Exception e) {
                    CacheExceptionFactory.addErrorLog("DbSourceScheduled", "dbConfigChange", "连接资源替换异常！", e);
                }
            }
        }, CacheBasicConfig.DB_CONFIG_CHECK_lINTERVAL_SECONDS, CacheBasicConfig.DB_CONFIG_CHECK_lINTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public CacheConfigSourceTypeEnum getConfigType() {
        return CacheConfigSourceTypeEnum.DB;
    }

    @Override
    protected BaseCacheConfig getJedisConfig(CacheConfigModel cacheConfigModel) {
        RedisDbConfigModel config = this.getConfig(cacheConfigModel);
        switch (cacheConfigModel.getConnectTypeEnum()) {
            case SIMPLE:
                return DbConfigConvertUtils.dbConfigToJedisConnectSourceConfig(config);
            case POOL:
                return DbConfigConvertUtils.dbConfigToJedisConnectSourceConfig(config);
            case SHARDED:
                return DbConfigConvertUtils.dbConfigToJedisShardConnectSourceConfig(config);
            case CLUSTER:
                return DbConfigConvertUtils.dbConfigToJedisClusterConnectSourceConfig(config);
            case CLUSTER_POOL:
                return null;
            default:
                return null;
        }
    }

    @Override
    protected BaseCacheConfig getLettuceConfig(CacheConfigModel cacheConfigModel) {
        RedisDbConfigModel config = this.getConfig(cacheConfigModel);
        switch (cacheConfigModel.getConnectTypeEnum()) {
            case SIMPLE:
                return DbConfigConvertUtils.dbConfigToLettuceConnectSourceConfig(config);
            case POOL:
                return DbConfigConvertUtils.dbConfigToLettuceConnectSourceConfig(config);
            case LOOP:
                return DbConfigConvertUtils.dbConfigToLettuceConnectSourceConfig(config);
            case SHARDED:
                return null;
            case CLUSTER:
                return DbConfigConvertUtils.dbConfigToLettuceClusterConnectSourceConfig(config);
            case CLUSTER_POOL:
                return DbConfigConvertUtils.dbConfigToLettuceClusterConnectSourceConfig(config);
            default:
                return null;
        }
    }

    @Override
    protected RedisDbConfigModel getConfig(CacheConfigModel cacheConfigModel) {
        return getRedisDbConfigModelByDb(cacheConfigModel, true);
    }

    private RedisDbConfigModel getRedisDbConfigModelByDb(CacheConfigModel cacheConfigModel, boolean isFindCache) {
        String hashKey = CacheConfigUtils.modelToHashKeyNoUseType(cacheConfigModel);
        RedisDbConfigModel redisDbConfigModel = null;

        if (isFindCache) {
            redisDbConfigModel = redisDbConfigModelMap.get(hashKey);
            if (null != redisDbConfigModel && CollectionUtils.isNotEmpty(redisDbConfigModel.getConfigList())) {
                return redisDbConfigModel;
            }
        }

        CacheExceptionFactory.throwException("DbCacheConfigManager->getRedisDbConfigModelByDb list empty ! cacheConfigModel:", JrlJsonNoExpUtil.toJson(cacheConfigModel));
        return null;
    }
}