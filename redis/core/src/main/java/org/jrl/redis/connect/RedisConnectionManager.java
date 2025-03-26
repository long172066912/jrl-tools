package org.jrl.redis.connect;

import org.jrl.redis.config.InterfaceCacheConfig;
import org.jrl.redis.core.cache.redis.jedis.connect.JedisConnectionFactory;
import org.jrl.redis.core.cache.redis.lettuce.connect.LettuceConnectionFactory;
import org.jrl.redis.core.constant.RedisClientConstants;
import org.jrl.redis.core.model.CacheConfigModel;
import org.jrl.redis.exception.CacheExceptionFactory;
import org.jrl.redis.util.CacheConfigUtils;
import org.jrl.tools.json.JrlJsonNoExpUtil;
import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: RedisConnectionManager
 * @Description: 连接池获取与创建
 * @date 2021/1/19 5:34 PM
 */
public class RedisConnectionManager {

    private static final Logger LOGGER = JrlLoggerFactory.getLogger(RedisConnectionManager.class);

    private static LettuceConnectionFactory lettuceConnectionFactory = LettuceConnectionFactory.SINGLETON;

    private static JedisConnectionFactory jedisConnectionFactory = JedisConnectionFactory.SINGLETON;
    /**
     * 连接缓存，避免重复创建连接
     */
    protected static Map<String, ConnectResource> connectionMap = new ConcurrentHashMap<>();

    /**
     * 重置连接资源
     *
     * @param cacheConfigModel
     * @param redisSourceConfig
     */
    public static void resetConnectionResource(CacheConfigModel cacheConfigModel, Object redisSourceConfig) {
        if (null != redisSourceConfig) {
            String hashKey = CacheConfigUtils.modelToHashKey(cacheConfigModel);
            ConnectResource connection = connectionMap.get(hashKey);
            LOGGER.info("RedisConnectionManager->resetConnectionResource begin ! cacheConfigModel:[{}] , hashKey : {} , connection : {}", JrlJsonNoExpUtil.toJson(cacheConfigModel), hashKey, null == connection);
            if (null != connection) {
                //加写锁
                final long writeLock = connection.getStampedLock().writeLock();
                try {
                    //释放旧连接资源
                    connection.getResource().close();
                    //重新获取连接
                    getConnectionResourceByCacheConfigModel(cacheConfigModel, (InterfaceCacheConfig) redisSourceConfig, connection);
                    LOGGER.info("RedisConnectionManager->resetConnectionResource end ! cacheConfigModel:[{}]", JrlJsonNoExpUtil.toJson(cacheConfigModel));
                } catch (Exception e) {
                    CacheExceptionFactory.addErrorLog("RedisConnectionManager->resetConnectionResource reset error ！", e);
                } finally {
                    connection.getStampedLock().unlockWrite(writeLock);
                }
            }
        }
    }

    /**
     * 获取连接资源
     *
     * @param cacheConfigModel
     * @param redisSourceConfig
     * @return
     */
    public synchronized static ConnectResource getConnectionResource(CacheConfigModel cacheConfigModel, InterfaceCacheConfig redisSourceConfig) {
        if (null == redisSourceConfig) {
            CacheExceptionFactory.throwException("RedisConnectionManager->getConnectionResource redisSourceConfig is empty !");
            return null;
        }
        //获取已有的连接
        String hashKey = CacheConfigUtils.modelToHashKey(cacheConfigModel);
        ConnectResource connectionResource = connectionMap.get(hashKey);
        //没拿到创建新连接
        if (null == connectionResource) {
            connectionResource = getConnectionResourceByCacheConfigModel(cacheConfigModel, redisSourceConfig, null);
            if (null != connectionResource) {
                connectionMap.put(hashKey, connectionResource);
            }
        }
        return connectionResource;
    }

    /**
     * 获取连接资源
     *
     * @param cacheConfigModel
     * @param redisSourceConfig
     * @return
     */
    private synchronized static ConnectResource getConnectionResourceByCacheConfigModel(CacheConfigModel cacheConfigModel, InterfaceCacheConfig redisSourceConfig, ConnectResource connectResource) {
        if (null == connectResource) {
            connectResource = new ConnectResource();
        }
        switch (cacheConfigModel.getClientType()) {
            case RedisClientConstants.JEDIS:
                return connectResource.setJedisConnectResource(jedisConnectionFactory.getJedisConnectionResource(redisSourceConfig, cacheConfigModel));
            case RedisClientConstants.LETTUCE:
                return connectResource.setLettuceConnectResource(lettuceConnectionFactory.getLettuceConnectionResource(redisSourceConfig, cacheConfigModel));
            default:
                return null;
        }
    }


}
