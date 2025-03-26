package org.jrl.redis.config.manager;

import org.jrl.redis.config.BaseCacheConfig;
import org.jrl.redis.core.constant.CacheConfigSourceTypeEnum;
import org.jrl.redis.core.constant.RedisClientConstants;
import org.jrl.redis.core.model.CacheConfigModel;

import javax.annotation.PostConstruct;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: AbstractConfigManager
 * @Description: 配置管理抽象
 * @date 2021/7/25 3:41 PM
 */
public abstract class AbstractConfigManager {

    /**
     * 获取配置类型
     *
     * @return
     */
    public abstract CacheConfigSourceTypeEnum getConfigType();

    @PostConstruct
    public void register() {
        CacheConfigFactory.register(this.getConfigType(), this);
    }

    /**
     * 获取配置
     *
     * @param cacheConfigModel
     * @return
     */
    public BaseCacheConfig getConfigByCacheModel(CacheConfigModel cacheConfigModel) {
        if (cacheConfigModel.getClientType() == RedisClientConstants.LETTUCE) {
            return this.getLettuceConfig(cacheConfigModel);
        } else if (cacheConfigModel.getClientType() == RedisClientConstants.JEDIS) {
            return this.getJedisConfig(cacheConfigModel);
        } else {
            return null;
        }
    }

    /**
     * 获取Jedis配置
     *
     * @param cacheConfigModel
     * @return
     */
    protected abstract BaseCacheConfig getJedisConfig(CacheConfigModel cacheConfigModel);

    /**
     * 获取lettuce配置
     *
     * @param cacheConfigModel
     * @return
     */
    protected abstract BaseCacheConfig getLettuceConfig(CacheConfigModel cacheConfigModel);

    /**
     * 通过数据源获取配置
     *
     * @param cacheConfigModel
     * @return
     */
    protected abstract Object getConfig(CacheConfigModel cacheConfigModel);
}
