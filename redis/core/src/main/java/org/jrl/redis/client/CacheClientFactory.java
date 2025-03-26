package org.jrl.redis.client;

import org.jrl.redis.config.BaseCacheConfig;
import org.jrl.redis.config.CacheBasicConfig;
import org.jrl.redis.config.RedisConfigBuilder;
import org.jrl.redis.core.BaseCacheExecutor;
import org.jrl.redis.core.cache.redis.jedis.config.JedisConnectSourceConfig;
import org.jrl.redis.core.cache.redis.lettuce.config.LettuceConnectSourceConfig;
import org.jrl.redis.core.cache.redis.lua.RedisLuaInterface;
import org.jrl.redis.core.constant.CacheConfigSourceTypeEnum;
import org.jrl.redis.core.constant.ConnectTypeEnum;
import org.jrl.redis.core.constant.UseTypeEnum;
import org.jrl.redis.core.model.CacheConfigModel;
import org.jrl.redis.executor.CacheExecutorFactory;

import java.util.List;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: CacheClientFactory
 * @Description: 缓存实例工厂，获取缓存执行器
 * @date 2021/1/19 4:17 PM
 */
public class CacheClientFactory {

    /**
     * 获取缓存执行器
     * 只传入cacheType
     *
     * @param cacheType
     * @return
     */
    public static BaseCacheExecutor getCacheExecutor(String cacheType) {
        CacheConfigModel cacheConfigModel = CacheConfigModel.newCache(cacheType);
        return CacheExecutorFactory.getCacheExecutor(RedisConfigBuilder.builder(cacheConfigModel).build(), cacheConfigModel);
    }

    /**
     * 获取Apollo配置方式缓存执行器
     *
     * @param cacheType
     * @return
     */
    public static BaseCacheExecutor getApolloCacheExecutor(String cacheType) {
        CacheConfigModel cacheConfigModel = CacheConfigModel.newCache(cacheType).setConfigSourceType(CacheConfigSourceTypeEnum.APOLLO);
        return CacheExecutorFactory.getCacheExecutor(RedisConfigBuilder.builder(cacheConfigModel).build(), cacheConfigModel);
    }

    /**
     * 获取缓存执行器
     * 传入CacheConfigModel
     *
     * @param cacheConfigModel
     * @return
     */
    public static BaseCacheExecutor getCacheExecutor(CacheConfigModel cacheConfigModel) {
        return CacheExecutorFactory.getCacheExecutor(RedisConfigBuilder.builder(cacheConfigModel).build(), cacheConfigModel);
    }

    /**
     * 自定义配置获取缓存执行器
     *
     * @param cacheType
     * @param config
     * @return
     */
    public static BaseCacheExecutor getCacheExecutor(String cacheType, BaseCacheConfig config) {
        CacheConfigModel cacheConfigModel = new CacheConfigModel();
        cacheConfigModel.setConfigSourceType(CacheConfigSourceTypeEnum.CUSTOM);
        cacheConfigModel.setCacheType(cacheType);
        return CacheExecutorFactory.getCacheExecutor(config, cacheConfigModel);
    }

    public static BaseCacheExecutor getCacheExecutor(CacheConfigModel configModel, int timeout) {
        final BaseCacheConfig cacheConfig = RedisConfigBuilder.builder(configModel).build();
        if (cacheConfig instanceof LettuceConnectSourceConfig) {
            ((LettuceConnectSourceConfig) cacheConfig).setTimeout(timeout);
            ((LettuceConnectSourceConfig) cacheConfig).setSoTimeout(timeout);
        } else if (cacheConfig instanceof JedisConnectSourceConfig) {
            ((JedisConnectSourceConfig) cacheConfig).setTimeout(timeout);
            ((JedisConnectSourceConfig) cacheConfig).setSoTimeout(timeout);
        }
        return CacheExecutorFactory.getCacheExecutor(cacheConfig, configModel);
    }

    /**
     * 获取缓存执行器
     * 传入自定义配置
     * Jedis:
     * simple:CacheClientFactory.getCacheExecutor(cacheConfigModel,new LettuceConnectSourceConfig())
     * cluster:CacheClientFactory.getCacheExecutor(cacheConfigModel,new JedisClusterConnectSourceConfig())
     * Lettuce:
     * simple:CacheClientFactory.getCacheExecutor(cacheConfigModel,new LettuceConnectSourceConfig())
     * cluster:CacheClientFactory.getCacheExecutor(cacheConfigModel,new LettuceClusterConnectSourceConfig())
     *
     * @param cacheConfigModel
     * @param config
     * @return
     */
    public static BaseCacheExecutor getCacheExecutor(CacheConfigModel cacheConfigModel, BaseCacheConfig config) {
        cacheConfigModel.setConfigSourceType(CacheConfigSourceTypeEnum.CUSTOM);
        return CacheExecutorFactory.getCacheExecutor(config, cacheConfigModel);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * builder
     */
    public static class Builder {
        /**
         * cacheType
         */
        private String cacheType;
        /**
         * 客户端类型，默认Lettuce
         */
        private Integer clientType;
        /**
         * 默认连接池方式
         */
        private ConnectTypeEnum connectType;
        /**
         * 是否本地缓存，默认不开启
         */
        private Boolean isLocalCache;
        /**
         * 用途
         */
        private UseTypeEnum useType = CacheBasicConfig.useType;
        /**
         * 配置方式
         */
        private CacheConfigSourceTypeEnum configSourceType;
        /**
         * 是否开启monitor监控，默认开启
         */
        private Boolean isOpenMonitor;
        /**
         * 连接资源配置
         */
        private BaseCacheConfig cacheConfig;
        /**
         * 配置需要注入的lua脚本
         */
        private List<RedisLuaInterface> luas;

        public Builder cacheType(String cacheType) {
            this.cacheType = cacheType;
            return this;
        }

        public Builder clientType(Integer clientType) {
            this.clientType = null == clientType ? CacheBasicConfig.clientType : clientType;
            return this;
        }

        public Builder connectType(ConnectTypeEnum connectType) {
            this.connectType = null == connectType ? CacheBasicConfig.connectTypeEnum : connectType;
            return this;
        }

        public Builder isLocalCache(Boolean isLocalCache) {
            this.isLocalCache = isLocalCache;
            return this;
        }

        public Builder configSourceType(CacheConfigSourceTypeEnum configSourceType) {
            this.configSourceType = configSourceType;
            return this;
        }

        public Builder isOpenMonitor(Boolean isOpenMonitor) {
            this.isOpenMonitor = isOpenMonitor;
            return this;
        }

        public Builder cacheConfig(BaseCacheConfig cacheConfig) {
            this.cacheConfig = cacheConfig;
            return this;
        }

        public Builder addLua(List<RedisLuaInterface> luas) {
            this.luas = luas;
            return this;
        }

        public BaseCacheExecutor build() {
            CacheConfigModel cacheConfigModel = CacheConfigModel.newCache(cacheType)
                    .setClientType(clientType)
                    .setConnectTypeEnum(connectType)
                    .setLocalCache(null == isLocalCache ? CacheBasicConfig.isLocalCache : isLocalCache)
                    .setUseType(useType)
                    .setOpenMonitor(null == isOpenMonitor ? true : isOpenMonitor)
                    .setConfigSourceType(null == configSourceType ? CacheConfigSourceTypeEnum.DB : configSourceType);
            if (null == cacheConfig) {
                cacheConfig = RedisConfigBuilder.builder(cacheConfigModel).build();
            }
            BaseCacheExecutor cacheExecutor = CacheExecutorFactory.getCacheExecutor(cacheConfig, cacheConfigModel);
            cacheExecutor.addLua(luas);
            return cacheExecutor;
        }
    }
}