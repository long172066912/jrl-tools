package org.jrl.redis.config.manager;

import org.jrl.redis.config.BaseCacheConfig;
import org.jrl.redis.core.constant.CacheConfigSourceTypeEnum;
import org.jrl.redis.core.model.CacheConfigModel;
import org.jrl.redis.exception.CacheExceptionFactory;
import org.jrl.redis.extend.config.ApolloCacheConfigManager;
import org.jrl.redis.extend.config.DbCacheConfigManager;
import org.jrl.redis.util.CacheConfigUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: CacheConfigFactory
 * @Description: 缓存源配置工厂
 * @date 2021/7/25 3:47 PM
 */
public class CacheConfigFactory {
    /**
     * 配置管理
     */
    private static Map<CacheConfigSourceTypeEnum, AbstractConfigManager> managers = new ConcurrentHashMap<>();
    /**
     * 配置缓存
     */
    protected static Map<String, BaseCacheConfig> redisConfigMap = new ConcurrentHashMap<>();

    static {
        DbCacheConfigManager dbCacheConfigManager = new DbCacheConfigManager();
        CacheConfigFactory.register(dbCacheConfigManager.getConfigType(), dbCacheConfigManager);
        ApolloCacheConfigManager apolloCacheConfigManager = new ApolloCacheConfigManager();
        CacheConfigFactory.register(apolloCacheConfigManager.getConfigType(), apolloCacheConfigManager);
    }

    /**
     * 注册
     * @param cacheConfigSourceTypeEnum
     * @param manager
     */
    public static void register(CacheConfigSourceTypeEnum cacheConfigSourceTypeEnum, AbstractConfigManager manager) {
        managers.put(cacheConfigSourceTypeEnum, manager);
    }

    /**
     * 获取配置
     * @param cacheConfigModel
     * @return
     */
    public static BaseCacheConfig getConfig(CacheConfigModel cacheConfigModel) {
        if (null == cacheConfigModel || StringUtils.isBlank(cacheConfigModel.getCacheType())) {
            CacheExceptionFactory.throwException("CacheConfigFactory getRedisConfig cacheType empty !");
        }
        return redisConfigMap.computeIfAbsent(CacheConfigUtils.modelToHashKeyNoUseType(cacheConfigModel)
                , e -> managers.get(cacheConfigModel.getConfigSourceType()).getConfigByCacheModel(cacheConfigModel)
        );
    }

    /**
     * 重置配置
     *
     * @param cacheConfigModel
     * @param baseCacheConfig
     */
    public static void resetConfig(CacheConfigModel cacheConfigModel, BaseCacheConfig baseCacheConfig) {
        redisConfigMap.put(CacheConfigUtils.modelToHashKeyNoUseType(cacheConfigModel), baseCacheConfig);
    }
}
