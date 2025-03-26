package org.jrl.redis.core.cache.redis.redisson;

import org.jrl.redis.config.BaseCacheConfig;
import org.jrl.redis.core.cache.redis.jedis.config.JedisClusterConnectSourceConfig;
import org.jrl.redis.core.cache.redis.jedis.config.JedisConnectSourceConfig;
import org.jrl.redis.core.cache.redis.lettuce.config.LettuceClusterConnectSourceConfig;
import org.jrl.redis.core.cache.redis.lettuce.config.LettuceConnectSourceConfig;
import org.jrl.redis.core.constant.RedisClientConstants;
import org.jrl.redis.core.model.CacheConfigModel;
import org.jrl.redis.exception.CacheExceptionFactory;
import org.jrl.redis.util.CacheConfigUtils;
import org.apache.commons.lang3.StringUtils;
import org.jrl.tools.json.JrlJsonNoExpUtil;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import redis.clients.jedis.HostAndPort;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.jrl.redis.config.CacheBasicConfig.REDISSON_THREAD_NUM;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: RedissonClientManager
 * @Description: Redisson管理器
 * @date 2021/3/2 4:42 PM
 */
public class RedissonClientManager {

    /**
     * Redisson客户端
     */
    private static Map<String, RedissonClient> redissonMap = new ConcurrentHashMap<>();
    /**
     * 订阅数
     */
    private static int SUB_SCRIPTIONS_PER_SIZE = 5000;
    private static int PING_TIME = 5000;
    private static int TIMEOUT = 2000;

    /**
     * 关闭Redisson连接
     * @param cacheConfigModel
     */
    public static void close(CacheConfigModel cacheConfigModel) {
        String key = CacheConfigUtils.modelToHashKey(cacheConfigModel);
        if (null != redissonMap.get(key)) {
            RedissonClient redissonClient = redissonMap.get(key);
            redissonClient.shutdown();
            redissonMap.remove(redissonClient);
        }
    }

    /**
     * 获取Redisson客户端
     *
     * @param cacheConfigModel
     * @param cacheConfig
     * @return
     */
    public static RedissonClient getRedissonClient(CacheConfigModel cacheConfigModel, BaseCacheConfig cacheConfig) {
        RedissonClient redissonClient = null;
        String key = CacheConfigUtils.modelToHashKey(cacheConfigModel);
        redissonClient = redissonMap.get(key);
        if (null != redissonClient && !redissonClient.isShutdown()) {
            return redissonClient;
        }
        synchronized (RedissonClientManager.class) {
            redissonClient = redissonMap.get(key);
            if (null != redissonClient && !redissonClient.isShutdown()) {
                return redissonClient;
            }
            Config config = getConfig(cacheConfigModel, cacheConfig);
            if (null == config) {
                CacheExceptionFactory.throwException("RedissonClientManager->getConfig error ! config is null !,cacheConfigModel:[{}],cacheConfig:[{}]", cacheConfigModel.toString() + JrlJsonNoExpUtil.toJson(cacheConfig));
                return null;
            }
            //设置线程数
            config.setThreads(REDISSON_THREAD_NUM);
            //设置Netty线程数
            config.setNettyThreads(REDISSON_THREAD_NUM);
            redissonClient = Redisson.create(config);
            redissonMap.put(key, redissonClient);
        }
        return redissonClient;
    }

    /**
     * 配置转换
     *
     * @param cacheConfigModel
     * @param cacheConfig
     * @return
     */
    private static Config getConfig(CacheConfigModel cacheConfigModel, BaseCacheConfig cacheConfig) {
        switch (cacheConfigModel.getClientType()) {
            case RedisClientConstants.JEDIS:
                return getJedisRedissonConfig(cacheConfigModel, cacheConfig);
            case RedisClientConstants.LETTUCE:
                return getLettuceRedissonConfig(cacheConfigModel, cacheConfig);
            default:
                return null;
        }
    }

    /**
     * Jedis配置转Redisson配置
     *
     * @param cacheConfigModel
     * @param cacheConfig
     * @return
     */
    private static Config getJedisRedissonConfig(CacheConfigModel cacheConfigModel, BaseCacheConfig cacheConfig) {
        Config config = new Config();
        switch (cacheConfigModel.getConnectTypeEnum()) {
            case SIMPLE:
                JedisConnectSourceConfig simpleConfig = (JedisConnectSourceConfig) cacheConfig;
                config.useSingleServer()
                        .setAddress("redis://" + simpleConfig.getHost() + ":" + simpleConfig.getPort())
                        .setPassword(StringUtils.isNotBlank(simpleConfig.getPwd()) ? simpleConfig.getPwd() : null)
                        .setPingConnectionInterval(PING_TIME)
                        .setTimeout(TIMEOUT)
                        //增大Redisson订阅值
                        .setSubscriptionsPerConnection(SUB_SCRIPTIONS_PER_SIZE);
                break;
            case POOL:
                JedisConnectSourceConfig poolConfig = (JedisConnectSourceConfig) cacheConfig;
                config.useSingleServer()
                        .setAddress("redis://" + poolConfig.getHost() + ":" + poolConfig.getPort())
                        .setPassword(StringUtils.isNotBlank(poolConfig.getPwd()) ? poolConfig.getPwd() : null)
                        .setPingConnectionInterval(PING_TIME)
                        .setTimeout(TIMEOUT)
                        //增大Redisson订阅值
                        .setSubscriptionsPerConnection(SUB_SCRIPTIONS_PER_SIZE);
                break;
            case SHARDED:
                return null;
            case CLUSTER:
                JedisClusterConnectSourceConfig clusterConfig = (JedisClusterConnectSourceConfig) cacheConfig;
                for (HostAndPort hostAndPort : clusterConfig.getNodes()) {
                    config.useClusterServers().addNodeAddress("redis://" + hostAndPort.getHost() + ":" + hostAndPort.getPort());
                }
                config.useClusterServers()
                        // 集群状态扫描间隔时间，单位是毫秒
                        .setScanInterval(2000)
                        .setPassword(StringUtils.isNotBlank(clusterConfig.getPwd()) ? clusterConfig.getPwd() : null)
                        .setPingConnectionInterval(PING_TIME)
                        .setTimeout(TIMEOUT)
                        //增大Redisson订阅值
                        .setSubscriptionsPerConnection(SUB_SCRIPTIONS_PER_SIZE);
                break;
            case CLUSTER_POOL:
                JedisClusterConnectSourceConfig clusterPoolConfig = (JedisClusterConnectSourceConfig) cacheConfig;
                for (HostAndPort hostAndPort : clusterPoolConfig.getNodes()) {
                    config.useClusterServers().addNodeAddress("redis://" + hostAndPort.getHost() + ":" + hostAndPort.getPort());
                }
                config.useClusterServers()
                        // 集群状态扫描间隔时间，单位是毫秒
                        .setScanInterval(2000)
                        .setPassword(StringUtils.isNotBlank(clusterPoolConfig.getPwd()) ? clusterPoolConfig.getPwd() : null)
                        .setPingConnectionInterval(PING_TIME)
                        .setTimeout(TIMEOUT)
                        //增大Redisson订阅值
                        .setSubscriptionsPerConnection(SUB_SCRIPTIONS_PER_SIZE);
                break;
            default:
                return null;
        }
        return config;
    }

    /**
     * Lettuce配置转Redisson配置
     *
     * @param cacheConfigModel
     * @param cacheConfig
     * @return
     */
    private static Config getLettuceRedissonConfig(CacheConfigModel cacheConfigModel, BaseCacheConfig cacheConfig) {
        Config config = new Config();
        switch (cacheConfigModel.getConnectTypeEnum()) {
            case SIMPLE:
                LettuceConnectSourceConfig simpleConfig = (LettuceConnectSourceConfig) cacheConfig;
                config.useSingleServer()
                        .setAddress("redis://" + simpleConfig.getHost() + ":" + simpleConfig.getPort())
                        .setPassword(StringUtils.isNotBlank(simpleConfig.getPwd()) ? simpleConfig.getPwd() : null)
                        .setPingConnectionInterval(PING_TIME)
                        .setTimeout(TIMEOUT)
                        //增大Redisson订阅值
                        .setSubscriptionsPerConnection(SUB_SCRIPTIONS_PER_SIZE);
                break;
            case POOL:
                LettuceConnectSourceConfig poolConfig = (LettuceConnectSourceConfig) cacheConfig;
                config.useSingleServer()
                        .setAddress("redis://" + poolConfig.getHost() + ":" + poolConfig.getPort())
                        .setPassword(StringUtils.isNotBlank(poolConfig.getPwd()) ? poolConfig.getPwd() : null)
                        .setPingConnectionInterval(PING_TIME)
                        .setTimeout(TIMEOUT)
                        //增大Redisson订阅值
                        .setSubscriptionsPerConnection(SUB_SCRIPTIONS_PER_SIZE);
                break;
            case SHARDED:
                return null;
            case CLUSTER:
                LettuceClusterConnectSourceConfig clusterConfig = (LettuceClusterConnectSourceConfig) cacheConfig;
                String pwd = "";
                for (LettuceConnectSourceConfig hostAndPort : clusterConfig.getNodes()) {
                    config.useClusterServers().addNodeAddress("redis://" + hostAndPort.getHost() + ":" + hostAndPort.getPort());
                    pwd = hostAndPort.getPwd();
                }
                config.useClusterServers()
                        // 集群状态扫描间隔时间，单位是毫秒
                        .setScanInterval(2000)
                        .setPassword(StringUtils.isNotBlank(pwd) ? pwd : null)
                        .setPingConnectionInterval(PING_TIME)
                        .setTimeout(TIMEOUT)
                        //增大Redisson订阅值
                        .setSubscriptionsPerConnection(SUB_SCRIPTIONS_PER_SIZE);
                break;
            case CLUSTER_POOL:
                LettuceClusterConnectSourceConfig clusterPoolConfig = (LettuceClusterConnectSourceConfig) cacheConfig;
                String pwd1 = "";
                for (LettuceConnectSourceConfig hostAndPort : clusterPoolConfig.getNodes()) {
                    config.useClusterServers().addNodeAddress("redis://" + hostAndPort.getHost() + ":" + hostAndPort.getPort());
                    pwd1 = hostAndPort.getPwd();
                }
                config.useClusterServers()
                        // 集群状态扫描间隔时间，单位是毫秒
                        .setScanInterval(2000)
                        .setPassword(StringUtils.isNotBlank(pwd1) ? pwd1 : null)
                        .setPingConnectionInterval(PING_TIME)
                        .setTimeout(TIMEOUT)
                        //增大Redisson订阅值
                        .setSubscriptionsPerConnection(SUB_SCRIPTIONS_PER_SIZE);
                break;
            default:
                return null;
        }
        return config;
    }
}
