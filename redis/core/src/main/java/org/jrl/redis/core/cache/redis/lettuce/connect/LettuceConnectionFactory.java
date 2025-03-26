package org.jrl.redis.core.cache.redis.lettuce.connect;

import org.jrl.redis.config.InterfaceCacheConfig;
import org.jrl.redis.core.cache.redis.lettuce.codec.JrlLettuceKeyCodec;
import org.jrl.redis.core.cache.redis.lettuce.config.LettuceClusterConnectSourceConfig;
import org.jrl.redis.core.cache.redis.lettuce.config.LettuceConnectSourceConfig;
import org.jrl.redis.core.constant.RedisClientConstants;
import org.jrl.redis.core.handle.AbstractConnectHandle;
import org.jrl.redis.core.model.CacheConfigModel;
import org.jrl.redis.exception.CacheExceptionConstants;
import org.jrl.redis.exception.CacheExceptionFactory;
import org.jrl.redis.util.CacheConfigBuildUtils;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.pubsub.StatefulRedisClusterPubSubConnection;
import io.lettuce.core.metrics.CommandLatencyCollector;
import io.lettuce.core.metrics.CommandLatencyCollectorOptions;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.jrl.redis.core.constant.ConnectTypeEnum;
import org.jrl.tools.json.JrlJsonNoExpUtil;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: LettuceConnectionFactory
 * @Description: Lettuce连接工厂
 * @date 2021/1/27 5:15 PM
 */
public class LettuceConnectionFactory extends AbstractConnectHandle {
    private LettuceConnectionFactory(){}

    public static final LettuceConnectionFactory SINGLETON = new LettuceConnectionFactory();

    @Override
    public int getClientType() {
        return RedisClientConstants.LETTUCE;
    }

    /**
     * 获取Lettuce连接
     *
     * @param redisSourceConfig
     * @param redisSourceConfig
     * @return
     */
    public LettuceConnectResource getLettuceConnectionResource(InterfaceCacheConfig redisSourceConfig, CacheConfigModel cacheConfigModel) {
        try {
            switch (cacheConfigModel.getConnectTypeEnum()) {
                case SIMPLE:
                    return new LettuceConnectResource().setStatefulRedisConnection(this.getLettuceConnection((LettuceConnectSourceConfig) redisSourceConfig));
                case POOL:
                    return new LettuceConnectResource().setGenericObjectPool(this.getLettuceConnectionByPool((LettuceConnectSourceConfig) redisSourceConfig));
                case SHARDED:
                    return null;
                case CLUSTER:
                    return new LettuceConnectResource().setStatefulRedisClusterConnection(this.getLettuceClusterConnection((LettuceClusterConnectSourceConfig) redisSourceConfig));
                case CLUSTER_POOL:
                    return new LettuceConnectResource().setGenericObjectPool(this.getLettuceClusterPoolConnection((LettuceClusterConnectSourceConfig) redisSourceConfig));
                case LOOP:
                    //多连接模式
                    //初始化
                    final LettuceConnectSourceConfig config = (LettuceConnectSourceConfig) redisSourceConfig;
                    final LettuceConnectLoop<StatefulRedisConnection> lettuceConnectLoop = new LettuceConnectLoop<>(config.getLoopLength(), () -> this.getLettuceConnection(config));
                    return new LettuceConnectResource().setLettuceConnectLoop(lettuceConnectLoop);
                default:
                    return null;
            }
        } catch (Exception e) {
            CacheExceptionFactory.throwException(CacheExceptionConstants.CACHE_ERROR_CODE, "RedisConnectionManager->getLettuceConnectionResource", JrlJsonNoExpUtil.toJson(cacheConfigModel), e);
            return null;
        }
    }

    /**
     * 获取Lettuce连接
     *
     * @param redisSourceConfig
     * @return
     */
    public StatefulRedisConnection<String, String> getLettuceConnection(LettuceConnectSourceConfig redisSourceConfig) {
        //设置线程
        DefaultClientResources res = this.getDefaultClientResources(redisSourceConfig.getHost(), redisSourceConfig.getPort(), redisSourceConfig.getCommonCacheConfig().getIoThreadPoolSize(), redisSourceConfig.getCommonCacheConfig().getComputationThreadPoolSize());
        //构建连接
        RedisURI uri = this.getRedisUri(redisSourceConfig.getHost(), redisSourceConfig.getPort(), redisSourceConfig.getPwd(), redisSourceConfig.getDatabase(), redisSourceConfig.getTimeout());
        //创建资源对象
        RedisClient client = RedisClient.create(res, uri);
        client.setOptions(CacheConfigBuildUtils.getClientOptions(redisSourceConfig));
        client.setDefaultTimeout(Duration.ofMillis(redisSourceConfig.getSoTimeout()));
        return (StatefulRedisConnection<String, String>) this.execute(() -> {
            return client.connect(JrlLettuceKeyCodec.UTF8);
        });
    }

    /**
     * 获取Simple方式发布订阅连接
     *
     * @param redisSourceConfig
     * @return
     */
    public StatefulRedisPubSubConnection<String, String> getLettucePubSubConnection(LettuceConnectSourceConfig redisSourceConfig) {
        //设置线程
        DefaultClientResources res = this.getDefaultClientResources(redisSourceConfig.getHost(), redisSourceConfig.getPort(), redisSourceConfig.getCommonCacheConfig().getIoThreadPoolSize(), redisSourceConfig.getCommonCacheConfig().getComputationThreadPoolSize());
        RedisClient client = RedisClient.create(res, this.getRedisUri(redisSourceConfig.getHost(), redisSourceConfig.getPort(), redisSourceConfig.getPwd(), redisSourceConfig.getDatabase(), redisSourceConfig.getTimeout()));
        client.setOptions(CacheConfigBuildUtils.getClientOptions(redisSourceConfig));
        client.setDefaultTimeout(Duration.ofMillis(redisSourceConfig.getSoTimeout()));
        return (StatefulRedisPubSubConnection<String, String>) this.execute(() -> {
            return client.connectPubSub(JrlLettuceKeyCodec.UTF8);
        });
    }

    /**
     * 获取Lettuce连接池
     *
     * @param redisSourceConfig
     * @return
     */
    public GenericObjectPool getLettuceConnectionByPool(LettuceConnectSourceConfig redisSourceConfig) {
        //设置线程
        DefaultClientResources res = this.getDefaultClientResources(redisSourceConfig.getHost(), redisSourceConfig.getPort(), redisSourceConfig.getCommonCacheConfig().getIoThreadPoolSize(), redisSourceConfig.getCommonCacheConfig().getComputationThreadPoolSize());
        RedisClient client = RedisClient.create(res, this.getRedisUri(redisSourceConfig.getHost(), redisSourceConfig.getPort(), redisSourceConfig.getPwd(), redisSourceConfig.getDatabase(), redisSourceConfig.getTimeout()));
        client.setOptions(CacheConfigBuildUtils.getClientOptions(redisSourceConfig));
        client.setDefaultTimeout(Duration.ofMillis(redisSourceConfig.getSoTimeout()));
        final GenericObjectPool pool = (GenericObjectPool) this.execute(() -> {
            return ConnectionPoolSupport.createGenericObjectPool(() -> getConnectionWithSpan(redisSourceConfig, client), CacheConfigBuildUtils.getJedisPoolConfig(redisSourceConfig.getCommonCacheConfig()));
        });
        //增加连接池监控
//        JrlMonitor.sampler("cacheConnectPoolMax", pool, GenericObjectPool::getMaxTotal, "host", redisSourceConfig.getHost());
        //当前最小变成1
//        JrlMonitor.sampler10s("cacheConnectPoolActive", pool, p -> p.getNumActive() > 0 ? p.getNumActive() : 1, "host", redisSourceConfig.getHost());
        return pool;
    }

    private StatefulRedisConnection<String, String> getConnectionWithSpan(LettuceConnectSourceConfig redisSourceConfig, RedisClient client) {
        return client.connect(JrlLettuceKeyCodec.UTF8);
    }

    /**
     * 获取Lettuce集群
     *
     * @param lettuceClusterConnectSourceConfig
     * @return
     */
    public StatefulRedisClusterConnection<String, String> getLettuceClusterConnection(LettuceClusterConnectSourceConfig lettuceClusterConnectSourceConfig) {
        //设置线程
        DefaultClientResources res = this.getDefaultClientResources(lettuceClusterConnectSourceConfig.getHosts().toString(), 0, lettuceClusterConnectSourceConfig.getCommonCacheConfig().getIoThreadPoolSize(), lettuceClusterConnectSourceConfig.getCommonCacheConfig().getComputationThreadPoolSize());
        List<RedisURI> nodeConfigs = new ArrayList<>(lettuceClusterConnectSourceConfig.getNodes().size());
        for (LettuceConnectSourceConfig redisSourceConfig : lettuceClusterConnectSourceConfig.getNodes()) {
            nodeConfigs.add(this.getRedisUri(redisSourceConfig.getHost(), redisSourceConfig.getPort(), redisSourceConfig.getPwd(), redisSourceConfig.getDatabase(), redisSourceConfig.getTimeout()));
        }
        RedisClusterClient client = RedisClusterClient.create(res, nodeConfigs);
        client.setDefaultTimeout(Duration.ofMillis(lettuceClusterConnectSourceConfig.getSoTimeout()));
        client.setOptions(CacheConfigBuildUtils.getClusterClientOptions(lettuceClusterConnectSourceConfig));
        return (StatefulRedisClusterConnection<String, String>) this.execute(() -> {
            return client.connect(JrlLettuceKeyCodec.UTF8);
        });
    }

    /**
     * 获取集群方式发布订阅连接
     *
     * @param lettuceClusterConnectSourceConfig
     * @return
     */
    public StatefulRedisClusterPubSubConnection<String, String> getLettuceClusterPubSubConnection(LettuceClusterConnectSourceConfig lettuceClusterConnectSourceConfig) {
        //设置线程
        DefaultClientResources res = this.getDefaultClientResources(lettuceClusterConnectSourceConfig.getHosts().toString(), 0, lettuceClusterConnectSourceConfig.getCommonCacheConfig().getIoThreadPoolSize(), lettuceClusterConnectSourceConfig.getCommonCacheConfig().getComputationThreadPoolSize());
        List<RedisURI> nodeConfigs = new ArrayList<>(lettuceClusterConnectSourceConfig.getNodes().size());
        for (LettuceConnectSourceConfig redisSourceConfig : lettuceClusterConnectSourceConfig.getNodes()) {
            nodeConfigs.add(this.getRedisUri(redisSourceConfig.getHost(), redisSourceConfig.getPort(), redisSourceConfig.getPwd(), redisSourceConfig.getDatabase(), redisSourceConfig.getTimeout()));
        }
        RedisClusterClient client = RedisClusterClient.create(res, nodeConfigs);
        client.setDefaultTimeout(Duration.ofMillis(lettuceClusterConnectSourceConfig.getSoTimeout()));
        client.setOptions(CacheConfigBuildUtils.getClusterClientOptions(lettuceClusterConnectSourceConfig));
        return (StatefulRedisClusterPubSubConnection<String, String>) this.execute(() -> {
            return client.connectPubSub(JrlLettuceKeyCodec.UTF8);
        });
    }

    /**
     * 获取Lettuce集群连接池
     *
     * @param lettuceClusterConnectSourceConfig
     * @return
     */
    public GenericObjectPool getLettuceClusterPoolConnection(LettuceClusterConnectSourceConfig lettuceClusterConnectSourceConfig) {
        //设置线程
        DefaultClientResources res = this.getDefaultClientResources(lettuceClusterConnectSourceConfig.getHosts().toString(), 0, lettuceClusterConnectSourceConfig.getCommonCacheConfig().getIoThreadPoolSize(), lettuceClusterConnectSourceConfig.getCommonCacheConfig().getComputationThreadPoolSize());
        List<RedisURI> nodeConfigs = new ArrayList<>(lettuceClusterConnectSourceConfig.getNodes().size());
        for (LettuceConnectSourceConfig redisSourceConfig : lettuceClusterConnectSourceConfig.getNodes()) {
            nodeConfigs.add(this.getRedisUri(redisSourceConfig.getHost(), redisSourceConfig.getPort(), redisSourceConfig.getPwd(), redisSourceConfig.getDatabase(), redisSourceConfig.getTimeout()));
        }
        RedisClusterClient client = RedisClusterClient.create(res, nodeConfigs);
        client.setDefaultTimeout(Duration.ofMillis(lettuceClusterConnectSourceConfig.getSoTimeout()));
        client.setOptions(CacheConfigBuildUtils.getClusterClientOptions(lettuceClusterConnectSourceConfig));
        return (GenericObjectPool) this.execute(() -> {
            return ConnectionPoolSupport.createGenericObjectPool(() -> client.connect(JrlLettuceKeyCodec.UTF8), CacheConfigBuildUtils.getJedisPoolConfig(lettuceClusterConnectSourceConfig.getCommonCacheConfig()));
        });
    }

    private static Map<String, DefaultClientResources> defaultClientResources = new ConcurrentHashMap<>();

    /**
     * 设置线程
     *
     * @param ioThreadPoolSize
     * @param computationThreadPoolSize
     * @return
     */
    private synchronized DefaultClientResources getDefaultClientResources(String host, int port, int ioThreadPoolSize, int computationThreadPoolSize) {
        return defaultClientResources.computeIfAbsent(host + port
                , e -> DefaultClientResources.builder()
                        .ioThreadPoolSize(ioThreadPoolSize)
                        .computationThreadPoolSize(computationThreadPoolSize)
                        .commandLatencyRecorder(CommandLatencyCollector.create(CommandLatencyCollectorOptions.disabled()))
                        .build());
    }

    /**
     * 初始化连接对象
     *
     * @param host
     * @param port
     * @param pwd
     * @param database
     * @param timeout
     * @return
     */
    private RedisURI getRedisUri(String host, int port, String pwd, int database, int timeout) {
        RedisURI build = RedisURI.builder()
                .withHost(host)
                .withPort(port)
                .withDatabase(database)
                .withTimeout(Duration.ofMillis(timeout)).build();
        if (StringUtils.isNotBlank(pwd)) {
            build.setPassword(pwd.toCharArray());
        }
        return build;
    }
}
