package org.jrl.redis.core.cache.redis.jedis.connect;

import org.jrl.redis.config.InterfaceCacheConfig;
import org.jrl.redis.core.cache.redis.jedis.config.JedisClusterConnectSourceConfig;
import org.jrl.redis.core.cache.redis.jedis.config.JedisConnectSourceConfig;
import org.jrl.redis.core.cache.redis.jedis.config.JedisShardConnectSourceConfig;
import org.jrl.redis.core.constant.RedisClientConstants;
import org.jrl.redis.core.handle.AbstractConnectHandle;
import org.jrl.redis.core.model.CacheConfigModel;
import org.jrl.redis.exception.CacheExceptionConstants;
import org.jrl.redis.exception.CacheExceptionFactory;
import org.jrl.redis.util.CacheConfigBuildUtils;
import org.apache.commons.lang3.StringUtils;
import org.jrl.redis.core.constant.ConnectTypeEnum;
import org.jrl.tools.json.JrlJsonNoExpUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ShardedJedis;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: JedisConnectionFactory
 * @Description: Jedis连接工厂
 * @date 2021/1/27 5:15 PM
 */
public class JedisConnectionFactory extends AbstractConnectHandle {
    private JedisConnectionFactory(){}
    public static final JedisConnectionFactory SINGLETON = new JedisConnectionFactory();

    @Override
    public int getClientType() {
        return RedisClientConstants.JEDIS;
    }

    /**
     * 获取Jedis连接
     *
     * @param redisSourceConfig
     * @param redisSourceConfig
     * @return
     */
    public JedisConnectResource getJedisConnectionResource(InterfaceCacheConfig redisSourceConfig, CacheConfigModel cacheConfigModel) {
        try {
            switch (cacheConfigModel.getConnectTypeEnum()) {
                case SIMPLE:
                    return new JedisConnectResource().setJedis(this.getJedis((JedisConnectSourceConfig) redisSourceConfig, cacheConfigModel));
                case POOL:
                    return new JedisConnectResource().setJedisPool(this.getJedisPool((JedisConnectSourceConfig) redisSourceConfig, cacheConfigModel));
                case SHARDED:
                    return new JedisConnectResource().setShardedJedis(this.getShardedJedis((JedisShardConnectSourceConfig) redisSourceConfig, cacheConfigModel));
                case CLUSTER:
                    return new JedisConnectResource().setJedisCluster(this.getJedisCluster((JedisClusterConnectSourceConfig) redisSourceConfig, cacheConfigModel));
                case CLUSTER_POOL:
                    return null;
                default:
                    return null;
            }
        } catch (Exception e) {
            CacheExceptionFactory.throwException(CacheExceptionConstants.CACHE_ERROR_CODE, "RedisConnectionManager->getJedisConnectionResource", JrlJsonNoExpUtil.toJson(cacheConfigModel), e);
            return null;
        }
    }

    /**
     * 获取一个Jedis连接
     *
     * @param jedisConnectSourceConfig
     * @return
     */
    public Jedis getJedis(JedisConnectSourceConfig jedisConnectSourceConfig, CacheConfigModel cacheConfigModel) {
        return (Jedis) this.execute(() -> {
            Jedis jedis = new Jedis(jedisConnectSourceConfig.getHost(), jedisConnectSourceConfig.getPort(), jedisConnectSourceConfig.getTimeout(), jedisConnectSourceConfig.getSoTimeout()
                    , false, null, null, null);
            if (StringUtils.isNotBlank(jedisConnectSourceConfig.getPwd())) {
                jedis.auth(jedisConnectSourceConfig.getPwd());
            }
            return jedis;
        });
    }

    /**
     * 获取JedisPool连接
     *
     * @param redisSourceConfig
     * @return
     */
    public JedisPool getJedisPool(JedisConnectSourceConfig redisSourceConfig, CacheConfigModel cacheConfigModel) {
        return (JedisPool) this.execute(() -> {
            return new JedisPool(CacheConfigBuildUtils.getJedisPoolConfig(redisSourceConfig.getCommonCacheConfig()),
                    redisSourceConfig.getHost(), redisSourceConfig.getPort(),
                    redisSourceConfig.getTimeout(), redisSourceConfig.getSoTimeout(),
                    StringUtils.isNotBlank(redisSourceConfig.getPwd()) ? redisSourceConfig.getPwd() : null, redisSourceConfig.getDatabase(),
                    "rediscluster." + cacheConfigModel.getCacheType(), false, null, null, null);
        });
    }

    /**
     * 获取JedisCluster连接
     *
     * @param redisSourceConfig
     * @return
     */
    public JedisCluster getJedisCluster(JedisClusterConnectSourceConfig redisSourceConfig, CacheConfigModel cacheConfigModel) {
        return (JedisCluster) this.execute(() -> {
            return new JedisCluster(redisSourceConfig.getNodes(), redisSourceConfig.getTimeout(), redisSourceConfig.getSoTimeout(),
                    redisSourceConfig.getMaxAttempts(), StringUtils.isNotBlank(redisSourceConfig.getPwd()) ? redisSourceConfig.getPwd() : null, CacheConfigBuildUtils.getJedisPoolConfig(redisSourceConfig.getCommonCacheConfig()));
        });
    }

    /**
     * 获取ShardedJedis连接
     *
     * @param redisSourceConfig
     * @return
     */
    public ShardedJedis getShardedJedis(JedisShardConnectSourceConfig redisSourceConfig, CacheConfigModel cacheConfigModel) {
        return (ShardedJedis) this.execute(() -> {
            return new ShardedJedis(redisSourceConfig.getShards());
        });
    }


}
