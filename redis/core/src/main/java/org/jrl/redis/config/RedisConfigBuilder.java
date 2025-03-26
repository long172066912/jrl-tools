package org.jrl.redis.config;

import org.jrl.redis.config.manager.CacheConfigFactory;
import org.jrl.redis.core.constant.CacheConfigSourceTypeEnum;
import org.jrl.redis.core.model.CacheConfigModel;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: RedisConfigBuilder
 * @Description: redis配置构建器，包含构建、配置切换功能
 * @date 2021/3/31 3:48 PM
 */
public class RedisConfigBuilder {

    /**
     * cacheConfigModel
     */
    private CacheConfigModel cacheConfigModel;

    public static RedisConfigBuilder builder(CacheConfigModel cacheConfigModel) {
        RedisConfigBuilder redisConfigBuilder = new RedisConfigBuilder();
        redisConfigBuilder.setCacheConfigModel(cacheConfigModel);
        return redisConfigBuilder;
    }

    /**
     * 构建配置，如果非DB未获取到，再拿一次db配置
     *
     * @return
     */
    public BaseCacheConfig build() {
        BaseCacheConfig config = null;
        if (this.cacheConfigModel.getConfigSourceType() != CacheConfigSourceTypeEnum.DB) {
            config = CacheConfigFactory.getConfig(this.cacheConfigModel);
            if (null == config) {
                this.cacheConfigModel.setConfigSourceType(CacheConfigSourceTypeEnum.DB);
            }
        }
        if (this.cacheConfigModel.getConfigSourceType() == CacheConfigSourceTypeEnum.DB) {
            return CacheConfigFactory.getConfig(this.cacheConfigModel);
        }
        return config;
    }

    public CacheConfigModel getCacheConfigModel() {
        return cacheConfigModel;
    }

    public void setCacheConfigModel(CacheConfigModel cacheConfigModel) {
        this.cacheConfigModel = cacheConfigModel;
    }
}
