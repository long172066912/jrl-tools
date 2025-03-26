package org.jrl.redis.extend.config;

import org.jrl.redis.config.BaseCacheConfig;
import org.jrl.redis.config.manager.AbstractConfigManager;
import org.jrl.redis.core.constant.CacheConfigSourceTypeEnum;
import org.jrl.redis.core.model.CacheConfigModel;
import org.jrl.redis.extend.config.model.ApolloRedisConfig;
import org.jrl.redis.extend.config.model.RedisDbConfigModel;
import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: DbCacheConfigManager
 * @Description: 获取缓存DB配置，应该有统一抽象类，此为DB方式的实现
 * @date 2021/1/28 9:01 PM
 */
public class ApolloCacheConfigManager extends AbstractConfigManager {

    private static final Logger LOGGER = JrlLoggerFactory.getLogger(ApolloCacheConfigManager.class);
    /**
     * Apollo配置中心前缀
     */
    public static final String APOLLO_CACHE_PRE = "jrl-redis";
    /**
     * namespace中的key
     */
    public static final String APOLLO_CONFIG_KEY = "config";


    @Override
    public CacheConfigSourceTypeEnum getConfigType() {
        return CacheConfigSourceTypeEnum.APOLLO;
    }

    @Override
    protected ApolloRedisConfig getConfig(CacheConfigModel cacheConfigModel) {
//        apolloInit();
//        ApolloRedisConfig apolloRedisConfig = null;
//        JrlPropertiesConfig properties = JrlConfigs.getProperties(getApolloNamespace(cacheConfigModel));
//        if (null != properties) {
//            apolloRedisConfig = properties.getObj(APOLLO_CONFIG_KEY, ApolloRedisConfig.class);
//            if (null != apolloRedisConfig) {
//                //增加监听
//                addApolloConfigListener(cacheConfigModel, properties);
//            }
//        }
//        return apolloRedisConfig;
        return null;
    }

    @Override
    protected BaseCacheConfig getJedisConfig(CacheConfigModel cacheConfigModel) {
        ApolloRedisConfig apolloRedisConfig = this.getConfig(cacheConfigModel);
        RedisDbConfigModel redisDbConfigModel = new RedisDbConfigModel(apolloRedisConfig.getNodes());
        BaseCacheConfig baseCacheConfig = null;
        switch (cacheConfigModel.getConnectTypeEnum()) {
            case SIMPLE:
                baseCacheConfig = DbConfigConvertUtils.dbConfigToJedisConnectSourceConfig(redisDbConfigModel);
                break;
            case POOL:
                baseCacheConfig = DbConfigConvertUtils.dbConfigToJedisConnectSourceConfig(redisDbConfigModel);
                break;
            case SHARDED:
                baseCacheConfig = DbConfigConvertUtils.dbConfigToJedisShardConnectSourceConfig(redisDbConfigModel);
                break;
            case CLUSTER:
                baseCacheConfig = DbConfigConvertUtils.dbConfigToJedisClusterConnectSourceConfig(redisDbConfigModel);
                break;
            case CLUSTER_POOL:
                return null;
            default:
                return null;
        }
        baseCacheConfig.setCommonCacheConfig(apolloRedisConfig.getCommonConfig());
        return baseCacheConfig;
    }

    @Override
    protected BaseCacheConfig getLettuceConfig(CacheConfigModel cacheConfigModel) {
        ApolloRedisConfig apolloRedisConfig = this.getConfig(cacheConfigModel);
        RedisDbConfigModel redisDbConfigModel = new RedisDbConfigModel(apolloRedisConfig.getNodes());
        BaseCacheConfig baseCacheConfig = null;
        switch (cacheConfigModel.getConnectTypeEnum()) {
            case SIMPLE:
                baseCacheConfig = DbConfigConvertUtils.dbConfigToLettuceConnectSourceConfig(redisDbConfigModel);
                break;
            case POOL:
                baseCacheConfig = DbConfigConvertUtils.dbConfigToLettuceConnectSourceConfig(redisDbConfigModel);
                break;
            case SHARDED:
                return null;
            case CLUSTER:
                baseCacheConfig = DbConfigConvertUtils.dbConfigToLettuceClusterConnectSourceConfig(redisDbConfigModel);
                break;
            case CLUSTER_POOL:
                baseCacheConfig = DbConfigConvertUtils.dbConfigToLettuceClusterConnectSourceConfig(redisDbConfigModel);
                break;
            default:
                return null;
        }
        baseCacheConfig.setCommonCacheConfig(apolloRedisConfig.getCommonConfig());
        return baseCacheConfig;
    }

//    /**
//     * 监听Apollo
//     *
//     * @param cacheConfigModel
//     * @param config
//     */
//    private void addApolloConfigListener(CacheConfigModel cacheConfigModel, JrlPropertiesConfig config) {
//        // 实时监听
//        config.addChangeListener(changeEvent -> {
//            if (changeEvent.getNamespace().equals(getApolloNamespace(cacheConfigModel))) {
//                if (changeEvent.isChanged(APOLLO_CONFIG_KEY)) {
//                    LOGGER.info("ApolloCacheConfigManager->apolloConfigListener change ! changeEvent:[{}]", JrlJsonNoExpUtil.toJson(changeEvent));
//                    //连接重置
//                    RedisConnectionManager.resetConnectionResource(cacheConfigModel, this.getConfigByCacheModel(cacheConfigModel));
//                }
//            }
//        });
//    }

    /**
     * 获取命名空间
     *
     * @param cacheConfigModel
     * @return
     */
    private String getApolloNamespace(CacheConfigModel cacheConfigModel) {
        return APOLLO_CACHE_PRE + cacheConfigModel.getCacheType();
    }
}
