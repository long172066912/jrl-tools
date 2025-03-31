package org.jrl.utils.cache.spi;

import org.jrl.redis.client.CacheClientFactory;
import org.jrl.redis.core.BaseCacheExecutor;
import org.jrl.redis.core.cache.redis.lettuce.config.LettuceConnectSourceConfig;
import org.jrl.redis.core.model.CacheConfigModel;
import org.jrl.tools.cache.extend.mesh.JrlCacheMeshConnectType;

public class JrlRedisClientUtils {

    public static BaseCacheExecutor getCacheExecutor(String cacheType, JrlCacheMeshConnectType connectType) {
        if (connectType.equals(JrlCacheMeshConnectType.POOL)) {
            return CacheClientFactory.getCacheExecutor(CacheConfigModel.lettucePool(cacheType), new LettuceConnectSourceConfig());
        }
        return CacheClientFactory.getCacheExecutor(cacheType, new LettuceConnectSourceConfig());
    }
}
