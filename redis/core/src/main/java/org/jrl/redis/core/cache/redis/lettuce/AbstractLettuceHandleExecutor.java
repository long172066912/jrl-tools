package org.jrl.redis.core.cache.redis.lettuce;

import org.jrl.redis.connect.ConnectResource;
import org.jrl.redis.core.InterfaceCacheExecutor;
import org.jrl.redis.core.cache.redis.commands.RedisAsyncCommands;
import org.jrl.redis.core.cache.redis.lettuce.commands.LettuceRedisAsyncCommandsImpl;
import org.jrl.redis.core.cache.redis.lettuce.config.LettuceClusterConnectSourceConfig;
import org.jrl.redis.core.cache.redis.lettuce.config.LettuceConnectSourceConfig;
import org.jrl.redis.core.cache.redis.lettuce.connect.LettuceConnectLoop;
import org.jrl.redis.core.cache.redis.lettuce.connect.LettuceConnectResource;
import org.jrl.redis.core.cache.redis.lettuce.connect.LettuceConnectionFactory;
import org.jrl.redis.core.cache.redis.lettuce.proxy.LettuceAsyncProxy;
import org.jrl.redis.core.cache.redis.lua.RedisLuaScripts;
import org.jrl.redis.core.constant.ConnectTypeEnum;
import org.jrl.redis.core.constant.RedisClientConstants;
import org.jrl.redis.core.constant.UseTypeEnum;
import org.jrl.redis.core.handle.AbstractCacheHandle;
import org.jrl.redis.core.model.CacheConfigModel;
import org.jrl.redis.core.model.LettucePipelineCommand;
import org.jrl.redis.core.model.CachePipeline;
import org.jrl.redis.exception.CacheExceptionConstants;
import org.jrl.redis.exception.CacheExceptionFactory;
import org.jrl.redis.exception.CacheKeyNotExistsException;
import org.jrl.redis.util.CacheCommonUtils;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandType;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: AbstractLettuceHandleExecutor
 * @Description: Lettuce命令执行器
 * @date 2021/1/19 2:33 PM
 */
public abstract class AbstractLettuceHandleExecutor extends AbstractCacheHandle implements InterfaceCacheExecutor {

    private static Logger LOGGER = JrlLoggerFactory.getLogger(AbstractLettuceHandleExecutor.class);

    protected static LettuceConnectionFactory lettuceConnectionFactory = LettuceConnectionFactory.SINGLETON;

    private LettuceRedisAsyncCommandsImpl lettuceRedisAsyncCommands;

    @Override
    public int getClientType() {
        return RedisClientConstants.LETTUCE;
    }

    /**
     * 链接资源
     */
    private ConnectResource connectResource;

    /**
     * 管道链接资源
     */
    private LettuceConnectResource pipelineConnectResource;
    /**
     * 资源缓存
     */
    private ThreadLocal<StatefulConnection> statefulConnection = new ThreadLocal<>();
    /**
     * 资源缓存
     */
    private ThreadLocal<LettuceConnectLoop.ResourceNode<StatefulRedisConnection>> loopStatefulConnection = new ThreadLocal<LettuceConnectLoop.ResourceNode<StatefulRedisConnection>>();
    /**
     * 异步链接资源
     */
    private RedisClusterAsyncCommands asyncCommands;

    /**
     * 放入资源
     *
     * @param connectResource
     */
    @Override
    public void setConnectionResource(ConnectResource connectResource) {
        if (null == connectResource) {
            CacheExceptionFactory.throwException("AbstractLettuceHandleExecutor->setConnectionResource connectResource is null !");
        }
        this.connectResource = connectResource;
        //创建异步代理
        this.asyncCommands = new LettuceAsyncProxy(this, this.async(this.getConnectResource())).getProxy();
    }

    /**
     * 获取资源
     *
     * @return
     */
    @Override
    public StatefulConnection getConnectResource() {
        StatefulConnection statefulConnection = null;
        long stamp = connectResource.getStampedLock().tryOptimisticRead();
        statefulConnection = this.getStatefulConnection();
        //判断是否需要加悲观读锁
        if (!connectResource.getStampedLock().validate(stamp)) {
            stamp = connectResource.getStampedLock().readLock();
            try {
                statefulConnection = this.getStatefulConnection();
            } finally {
                //释放读锁
                connectResource.getStampedLock().unlockRead(stamp);
            }
        }
        return statefulConnection;
    }

    /**
     * 获取多连接模式的资源
     *
     * @return
     */
    public LettuceConnectLoop.ResourceNode<StatefulRedisConnection> getLoopStatefulConnection() {
        LettuceConnectLoop.ResourceNode<StatefulRedisConnection> localResource = loopStatefulConnection.get();
        if (null != localResource) {
            return localResource;
        }
        final LettuceConnectLoop.ResourceNode<StatefulRedisConnection> resource = connectResource.getLettuceConnectResource().getLettuceConnectLoop().getResource();
        //放到threadLocal中
        loopStatefulConnection.set(resource);
        return resource;
    }

    @Override
    public void loadLuaScripts() {
        /**
         * 加写锁
         */
        long writeLock = connectResource.getStampedLock().writeLock();
        try {
            RedisLuaScripts.getRedisLuaScripts().stream().forEach(e -> this.getLuaLoadsInfo().put(e, this.sync(this.getStatefulConnection()).scriptLoad(e.getScripts())));
        } catch (Exception e) {
            CacheExceptionFactory.addErrorLog("Lettuce loadLuaScripts fail", e);
        } finally {
            this.returnConnectResource();
            connectResource.getStampedLock().unlockWrite(writeLock);
        }
    }

    private StatefulConnection getStatefulConnection() {
        StatefulConnection localResource;
        if (this.getCacheConfigModel().getConnectTypeEnum() == ConnectTypeEnum.POOL || this.getCacheConfigModel().getConnectTypeEnum() == ConnectTypeEnum.CLUSTER_POOL) {
            localResource = statefulConnection.get();
            if (null != localResource) {
                return localResource;
            }
            localResource = this.getStatefulConnection(this.connectResource, this.getCacheConfigModel());
            statefulConnection.set(localResource);
        } else {
            localResource = this.getStatefulConnection(this.connectResource, this.getCacheConfigModel());
        }
        return localResource;
    }

    protected ConnectTypeEnum getConnectTypeEnum() {
        return this.getCacheConfigModel().getConnectTypeEnum();
    }

    private StatefulConnection getStatefulConnection(ConnectResource connectResource, CacheConfigModel cacheConfigModel) {
        try {
            switch (cacheConfigModel.getConnectTypeEnum()) {
                case SHARDED:
                    CacheExceptionFactory.throwException("Lettuce 暂不支持Shard方式");
                    return null;
                case POOL:
                    try {
                        return ((StatefulRedisConnection) connectResource.getLettuceConnectResource().getGenericObjectPool().borrowObject(2000L));
                    } catch (Exception e) {
                        CacheExceptionFactory.addErrorLog("AbstractLettuceHandleExecutor->getLettucePool error once !", e);
                        try {
                            return ((StatefulRedisConnection) connectResource.getLettuceConnectResource().getGenericObjectPool().borrowObject(2000L));
                        } catch (Exception e1) {
                            CacheExceptionFactory.addErrorLog("AbstractLettuceHandleExecutor->getLettucePool error twice !", e1);
                            throw e1;
                        }
                    }
                case CLUSTER:
                    return connectResource.getLettuceConnectResource().getStatefulRedisClusterConnection();
                case CLUSTER_POOL:
                    return ((StatefulRedisClusterConnection) connectResource.getLettuceConnectResource().getGenericObjectPool().borrowObject(2000L));
                case LOOP:
                    return getLoopStatefulConnection().getResource();
                default:
                    return connectResource.getLettuceConnectResource().getStatefulRedisConnection();
            }
        } catch (Exception e) {
            CacheExceptionFactory.throwException(CacheExceptionConstants.CACHE_ERROR_CODE, "Lettuce getStatefulConnection error !", e);
            return null;
        }
    }

    @Override
    public void returnConnectResource() {
        //释放资源
        try {
            if (this.getCacheConfigModel().getConnectTypeEnum() == ConnectTypeEnum.POOL || this.getCacheConfigModel().getConnectTypeEnum() == ConnectTypeEnum.CLUSTER_POOL) {
                StatefulConnection localResource = statefulConnection.get();
                if (null != localResource) {
                    this.getPool().returnObject(localResource);
                }
                statefulConnection.remove();
            } else
                //如果是多连接模式
                if (this.getCacheConfigModel().getConnectTypeEnum() == ConnectTypeEnum.LOOP) {
                    loopStatefulConnection.remove();
                }
        } catch (Exception e) {
            CacheExceptionFactory.addErrorLog("AbstractLettuceHandleExecutor", "returnConnectResource", "resourceReturn error", e);
        }
    }

    @Override
    public void close() {

    }

    @Override
    public void connectionClose() {
        //关闭连接
        this.getConnectResource().close();
    }

    @Override
    public GenericObjectPool getPool() {
        switch (this.getCacheConfigModel().getConnectTypeEnum()) {
            case POOL:
                return this.connectResource.getLettuceConnectResource().getGenericObjectPool();
            case CLUSTER_POOL:
                return this.connectResource.getLettuceConnectResource().getGenericObjectPool();
            default:
                return null;
        }
    }

    /**
     * 获取同步执行器
     *
     * @return
     */
    public RedisClusterCommands sync() {
        return this.sync(this.getStatefulConnection());
    }

    private RedisClusterCommands sync(StatefulConnection statefulConnection) {
        switch (this.getCacheConfigModel().getConnectTypeEnum()) {
            case SHARDED:
                CacheExceptionFactory.throwException("Lettuce 暂不支持Shard方式");
                return null;
            case CLUSTER:
                return ((StatefulRedisClusterConnection) statefulConnection).sync();
            case CLUSTER_POOL:
                return ((StatefulRedisClusterConnection) statefulConnection).sync();
            default:
                return ((StatefulRedisConnection) statefulConnection).sync();
        }
    }

    /**
     * 异步
     *
     * @return
     */
    @Override
    public RedisAsyncCommands async() {
        if (null == lettuceRedisAsyncCommands) {
            synchronized (this) {
                if (null == lettuceRedisAsyncCommands) {
                    lettuceRedisAsyncCommands = new LettuceRedisAsyncCommandsImpl();
                    lettuceRedisAsyncCommands.setAsyncExeutor(this);
                }
            }
        }
        return lettuceRedisAsyncCommands;
    }

    @Override
    public RedisClusterAsyncCommands asyncL() {
        return this.asyncCommands;
    }

    /**
     * 异步
     *
     * @return
     */
    public RedisClusterAsyncCommands async(StatefulConnection statefulConnection) {
        switch (this.getCacheConfigModel().getConnectTypeEnum()) {
            case SHARDED:
                CacheExceptionFactory.throwException("Lettuce 暂不支持Shard方式");
                return null;
            case CLUSTER:
                return ((StatefulRedisClusterConnection) statefulConnection).async();
            case CLUSTER_POOL:
                return ((StatefulRedisClusterConnection) statefulConnection).async();
            default:
                return ((StatefulRedisConnection) statefulConnection).async();
        }
    }

    /**
     * 获取发布订阅连接
     *
     * @return
     */
    public StatefulRedisPubSubConnection getPubSubConnection() {
        switch (this.getCacheConfigModel().getConnectTypeEnum()) {
            case SHARDED:
                CacheExceptionFactory.throwException("Lettuce 暂不支持Shard方式");
                return null;
            case CLUSTER:
                return lettuceConnectionFactory.getLettuceClusterPubSubConnection((LettuceClusterConnectSourceConfig) this.getRedisSourceConfig());
            case CLUSTER_POOL:
                return lettuceConnectionFactory.getLettuceClusterPubSubConnection((LettuceClusterConnectSourceConfig) this.getRedisSourceConfig());
            default:
                return lettuceConnectionFactory.getLettucePubSubConnection((LettuceConnectSourceConfig) this.getRedisSourceConfig());
        }
    }

    private StatefulConnection pipelineStatefulConnection;

    /**
     * 获取管道链接资源
     *
     * @return
     */
    private synchronized LettuceConnectResource getPipelineConnectResource(CacheConfigModel pipelineCacheConfigModel) {
        if (null == pipelineConnectResource) {
            pipelineConnectResource = lettuceConnectionFactory.getLettuceConnectionResource(this.getRedisSourceConfig(), pipelineCacheConfigModel);
        }
        return pipelineConnectResource;
    }

    @Override
    public List<Supplier<Object>> lettucePipeline(List<LettucePipelineCommand> lettucePipelineCommands) {
        final long l = System.currentTimeMillis();
        //设置使用方式为管道
        final StatefulConnection pipelineConnection = getPipelineConnection();
        try {
            List<Supplier<Object>> res = new ArrayList<>();
            List<RedisFuture<Object>> redisFutures = new LettucePipelineExecutor(this.async(pipelineConnection)).pipeline(lettucePipelineCommands);
            for (int i = 0; i < redisFutures.size(); i++) {
                try {
                    Object resp = redisFutures.get(i).get();
                    res.add(() -> {
                        //如果resp是空key标识，抛异常
                        if (KEY_NOT_EXISTS.equals(resp)) {
                            throw new CacheKeyNotExistsException("key not exists !");
                        }
                        return resp;
                    });
                } catch (Exception e) {
                    CacheExceptionFactory.addErrorLog("AbstractLettuceHandleExecutor->lettucePipeline get error ！ size:" + i, e);
                    res.add(() -> {
                        CacheExceptionFactory.throwException("lettucePipeline execute error !", e);
                        return null;
                    });
                }
            }
            return res;
        } finally {
            final long l1 = System.currentTimeMillis();
            final long l2 = l1 - l;
            final int timeout = 200;
            if (l2 > timeout) {
                LOGGER.warn("JrlRedis lettucePipeline 耗时过长 ！ size: {} 耗时: {}", lettucePipelineCommands.size(), l2);
            }
        }
    }

    @Override
    public CompletableFuture<List<Supplier<Object>>> lettucePipelineAsync(List<LettucePipelineCommand> lettucePipelineCommands) {
        return CompletableFuture.supplyAsync(() -> this.lettucePipeline(lettucePipelineCommands), getExpireThreadPoolExecutor());
    }

    @Override
    public List<Supplier<Object>> pipeline(List<CachePipeline> commands) {
        return this.lettucePipeline(commands.stream()
                .map(c -> {
                    CommandArgs commandArgs;
                    CommandType command;
                    switch (c.getCommandType()) {
                        //lua脚本
                        case 2:
                            command = CommandType.EVALSHA;
                            //加载lua脚本
                            final RedisLuaScripts redisLuaScripts = RedisLuaScripts.valueOfCommand(c.getCommand());
                            //获取脚本的shaValue
                            commandArgs = new CommandArgs(StringCodec.UTF8).add(this.getLuaSha1(redisLuaScripts)).add(c.getKeys().length).addKeys(c.getKeys());
                            if (CollectionUtils.isNotEmpty(c.getArgs())) {
                                commandArgs.addValues(c.getArgs());
                            }
                            break;
                        case 1:
                        default:
                            command = CommandType.valueOf(c.getCommand().toUpperCase(Locale.ROOT));
                            commandArgs = new CommandArgs(StringCodec.UTF8).addKeys(c.getKeys());
                            if (CollectionUtils.isNotEmpty(c.getArgs())) {
                                commandArgs.addValues(c.getArgs());
                            }
                            break;
                    }
                    return new LettucePipelineCommand(command, commandArgs, c.getOutputType());
                })
                .collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<List<Supplier<Object>>> pipelineAsync(List<CachePipeline> commands) {
        return CompletableFuture.supplyAsync(() -> this.pipeline(commands), getExpireThreadPoolExecutor());
    }

    private StatefulConnection getPipelineConnection() {
        if (null == pipelineStatefulConnection) {
            synchronized (this) {
                if (null == pipelineStatefulConnection) {
                    CacheConfigModel pipelineCacheConfigModel = CacheCommonUtils.copy(this.getCacheConfigModel(), CacheConfigModel.class);
                    pipelineCacheConfigModel.setUseType(UseTypeEnum.PIPELINE);
                    pipelineCacheConfigModel.setConnectTypeEnum(ConnectTypeEnum.SIMPLE);
                    //获取管道专用链接
                    pipelineStatefulConnection = this.getStatefulConnection(new ConnectResource().setLettuceConnectResource(this.getPipelineConnectResource(pipelineCacheConfigModel)), pipelineCacheConfigModel);
                }
            }
        }
        return pipelineStatefulConnection;
    }
}
