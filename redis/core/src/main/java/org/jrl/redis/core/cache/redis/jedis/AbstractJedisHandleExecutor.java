package org.jrl.redis.core.cache.redis.jedis;

import org.jrl.redis.connect.ConnectResource;
import org.jrl.redis.core.InterfaceCacheExecutor;
import org.jrl.redis.core.cache.redis.commands.RedisAsyncCommands;
import org.jrl.redis.core.cache.redis.lua.RedisLuaScripts;
import org.jrl.redis.core.constant.RedisClientConstants;
import org.jrl.redis.core.handle.AbstractCacheHandle;
import org.jrl.redis.core.model.LettucePipelineCommand;
import org.jrl.redis.core.model.CachePipeline;
import org.jrl.redis.exception.CacheExceptionFactory;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import org.jrl.redis.core.constant.ConnectTypeEnum;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.ShardedJedis;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: AbstractLettuceHandleExecutor
 * @Description: Jedis执行器
 * @date 2021/1/19 2:33 PM
 */
public abstract class AbstractJedisHandleExecutor extends AbstractCacheHandle implements InterfaceCacheExecutor {

    /**
     * 连接资源
     */
    private ConnectResource connectResource;

    private ThreadLocal<JedisCommands> resource = new ThreadLocal<>();

    @Override
    public void setConnectionResource(ConnectResource connectResource) {
        if (null == connectResource) {
            CacheExceptionFactory.throwException("AbstractJedisHandleExecutor->setConnectionResource connectResource is null !");
        }
        this.connectResource = connectResource;
    }

    @Override
    public JedisCommands getConnectResource() {
        if (this.getCacheConfigModel().isRetry()) {
            this.returnConnectResource();
            this.getCacheConfigModel().setRetry(false);
        }
        JedisCommands jedisCommands = null;
        //获取乐观读锁
        long stamp = connectResource.getStampedLock().tryOptimisticRead();
        jedisCommands = getConnectResourceNoLock();
        //判断是否需要加悲观读锁
        if (!connectResource.getStampedLock().validate(stamp)) {
            stamp = connectResource.getStampedLock().readLock();
            try {
                jedisCommands = getConnectResourceNoLock();
            } finally {
                connectResource.getStampedLock().unlockRead(stamp);
            }
        }
        return jedisCommands;
    }

    private boolean luaStatus = false;

    @Override
    public void loadLuaScripts() {
        if (!luaStatus) {
            luaStatus = true;
            try {
                RedisLuaScripts.getRedisLuaScripts().stream().forEach(e -> this.getLuaLoadsInfo().put(e, this.scriptLoad(e.getScripts())));
            } catch (Exception e) {
                CacheExceptionFactory.addErrorLog("Jedis loadLuaScripts fail", e);
            }
            luaStatus = false;
        }
    }

    private JedisCommands getConnectResourceNoLock() {
        JedisCommands localResource = resource.get();
        if (null != localResource) {
            return localResource;
        }
        switch (this.getCacheConfigModel().getConnectTypeEnum()) {
            case SHARDED:
                localResource = this.connectResource.getJedisConnectResource().getShardedJedis();
                break;
            case CLUSTER:
                localResource = this.connectResource.getJedisConnectResource().getJedisCluster();
                break;
            case POOL:
                try {
                    localResource = this.connectResource.getJedisConnectResource().getJedisPool().getResource();
                } catch (Exception e) {
                    CacheExceptionFactory.addErrorLog("AbstractJedisHandleExecutor->getJedisPool error once !", e);
                    try {
                        localResource = this.connectResource.getJedisConnectResource().getJedisPool().getResource();
                    } catch (Exception e1) {
                        CacheExceptionFactory.addErrorLog("AbstractJedisHandleExecutor->getJedisPool error twice !", e1);
                        throw e1;
                    }
                }
                break;
            default:
                localResource = this.connectResource.getJedisConnectResource().getJedis();
                break;
        }
        resource.set(localResource);
        return localResource;
    }

    @Override
    public Object getPool() {
        return this.getConnectResource();
    }

    @Override
    public void returnConnectResource() {
        //释放资源
        try {
            JedisCommands localResource = resource.get();
            if (null != localResource) {
                switch (this.getCacheConfigModel().getConnectTypeEnum()) {
                    case SIMPLE:
                        ((Jedis) localResource).close();
                        break;
                    case POOL:
                        ((Jedis) localResource).close();
                        break;
                    case SHARDED:
                        ((ShardedJedis) localResource).close();
                        break;
                    case CLUSTER:
                        ((JedisCluster) localResource).close();
                        break;
                    default:
                        break;
                }
                localResource = null;
                resource.remove();
            }
        } catch (Exception e) {
            CacheExceptionFactory.addErrorLog("AbstractJedisHandleExecutor", "returnConnectResource", "resourceReturn error", e);
        }
    }

    @Override
    public void close() {

    }

    @Override
    public void connectionClose() {

    }

    @Override
    public int getClientType() {
        return RedisClientConstants.JEDIS;
    }

    @Override
    public RedisAsyncCommands async() {
        CacheExceptionFactory.throwException("Jedis->async 不支持!");
        return null;
    }

    @Override
    public RedisClusterAsyncCommands asyncL() {
        CacheExceptionFactory.throwException("Jedis->asyncL 不支持!");
        return null;
    }

    @Override
    public List<Supplier<Object>> lettucePipeline(List<LettucePipelineCommand> lettucePipelineCommands) {
        CacheExceptionFactory.throwException("Jedis 不支持的命令 ： lettucePipeline !");
        return null;
    }

    @Override
    public List<Supplier<Object>> pipeline(List<CachePipeline> commands) {
        CacheExceptionFactory.throwException("Jedis 不支持的命令 ： pipeline !");
        return null;
    }

    @Override
    public CompletableFuture<List<Supplier<Object>>> lettucePipelineAsync(List<LettucePipelineCommand> lettucePipelineCommands) {
        CacheExceptionFactory.throwException("Jedis 不支持的命令 ： lettucePipeline !");
        return null;
    }

    @Override
    public CompletableFuture<List<Supplier<Object>>> pipelineAsync(List<CachePipeline> commands) {
        CacheExceptionFactory.throwException("Jedis 不支持的命令 ： pipelineAsync !");
        return null;
    }
}
