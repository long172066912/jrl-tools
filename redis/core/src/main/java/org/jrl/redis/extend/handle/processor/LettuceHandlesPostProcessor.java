package org.jrl.redis.extend.handle.processor;

import org.jrl.redis.config.BaseCacheConfig;
import org.jrl.redis.connect.RedisConnectionManager;
import org.jrl.redis.core.cache.redis.lettuce.AbstractLettuceHandleExecutor;
import org.jrl.redis.core.cache.redis.lettuce.config.LettuceConnectSourceConfig;
import org.jrl.redis.core.cache.redis.lettuce.connect.LettuceConnectLoop;
import org.jrl.redis.core.cache.redis.lettuce.connect.LettuceConnectResource;
import org.jrl.redis.core.cache.redis.lettuce.connect.LettuceConnectionFactory;
import org.jrl.redis.core.constant.ConnectTypeEnum;
import org.jrl.redis.core.constant.HandlePostProcessorTypeEnum;
import org.jrl.redis.core.constant.RedisClientConstants;
import org.jrl.redis.core.model.CacheHandleProcessorModel;
import org.jrl.redis.core.processor.AbstractHandlePostProcessor;
import org.jrl.redis.core.processor.factory.HandlePostFactory;
import org.jrl.redis.exception.CacheExceptionFactory;
import org.jrl.redis.executor.CacheExecutorFactory;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.RedisNoScriptException;
import io.lettuce.core.api.StatefulRedisConnection;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jrl.tools.json.JrlJsonNoExpUtil;
import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: RedisHandlesPostProcessor
 * @Description: redis命令后置处理器实现类
 * @date 2021/1/18 9:05 PM
 */
public class LettuceHandlesPostProcessor extends AbstractHandlePostProcessor {

    public static final String BRPOP = "brpop";
    private static Logger LOGGER = JrlLoggerFactory.getLogger(LettuceHandlesPostProcessor.class);

    private static LettuceConnectionFactory lettuceConnectionFactory = LettuceConnectionFactory.SINGLETON;

    private static final String CONNECTION_CLOSE_ERROR = "Connection is closed";
    private static final int LOG_TIME = 30;

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public int getHandlePostId() {
        return -1;
    }

    @Override
    public HandlePostProcessorTypeEnum getHandleType() {
        return HandlePostProcessorTypeEnum.HANDLE;
    }

    @Override
    public int getClientType() {
        return RedisClientConstants.LETTUCE;
    }

    @Override
    @PostConstruct
    public void registerIntoPostFactory() {
        HandlePostFactory.addBeanPostProcessor(this);
    }

    @Override
    public void handleBefore(CacheHandleProcessorModel cacheHandleProcessorModel) {
    }

    @Override
    public void onSuccess(CacheHandleProcessorModel cacheHandleProcessorModel) {
        if (null != cacheHandleProcessorModel.getExecuteTime() && cacheHandleProcessorModel.getExecuteTime() > LOG_TIME) {
            if (BRPOP.equals(cacheHandleProcessorModel.getCommands())) {
                return;
            }
            LOGGER.warn("redis execute time more than {}ms，耗时：{}ms ，命令：{} , key : {}", LOG_TIME, cacheHandleProcessorModel.getExecuteTime(), cacheHandleProcessorModel.getCommands(),
                    CollectionUtils.isNotEmpty(cacheHandleProcessorModel.getKeys()) ? JrlJsonNoExpUtil.toJson(cacheHandleProcessorModel.getKeys()) : cacheHandleProcessorModel.getKey());
        }
    }

    @Override
    public void onFail(CacheHandleProcessorModel cacheHandleProcessorModel) {
        /**
         * 如果是lua脚本异常，重新缓存
         */
        if (cacheHandleProcessorModel.getE() instanceof RedisNoScriptException) {
            //获取执行器调用脚本缓存操作
            CacheExecutorFactory.getCacheExecutor(null, cacheHandleProcessorModel.getCacheConfigModel()).loadLuaScripts();
        }
        if (cacheHandleProcessorModel.getE() instanceof RedisCommandExecutionException) {
            //redis分片存储时，可能会报MOVED错误，需要重新建立连接
            if (cacheHandleProcessorModel.getE().getMessage().contains("MOVED")) {
                reConnect(cacheHandleProcessorModel, cacheHandleProcessorModel.getCacheConfigModel().getConnectTypeEnum());
            }
        }
        //Lettuce非连接池方式增加连接重置功能
        boolean isNeedReConnect = cacheHandleProcessorModel.getCacheConfigModel().getConnectTypeEnum().equals(ConnectTypeEnum.SIMPLE)
                || cacheHandleProcessorModel.getCacheConfigModel().getConnectTypeEnum().equals(ConnectTypeEnum.LOOP);
        if (isNeedReConnect
                && StringUtils.isNotBlank(cacheHandleProcessorModel.getE().getMessage())
                && cacheHandleProcessorModel.getE().getMessage().contains(CONNECTION_CLOSE_ERROR)) {
            reConnect(cacheHandleProcessorModel, cacheHandleProcessorModel.getCacheConfigModel().getConnectTypeEnum());
        }
    }

    private void reConnect(CacheHandleProcessorModel cacheHandleProcessorModel, ConnectTypeEnum connectTypeEnum) {
        //重置连接
        try {
            synchronized (cacheHandleProcessorModel.getCacheConfigModel()) {
                //获取配置
                BaseCacheConfig config = CacheExecutorFactory.getRedisSourceConfig(cacheHandleProcessorModel.getCacheConfigModel());
                //判断连接是否有效
                final LettuceConnectResource lettuceConnectResource = RedisConnectionManager.getConnectionResource(cacheHandleProcessorModel.getCacheConfigModel(), config).getLettuceConnectResource();
                //如果是单链接模式
                if (connectTypeEnum.equals(ConnectTypeEnum.SIMPLE)) {
                    if (!lettuceConnectResource.getStatefulRedisConnection().isOpen()) {
                        //重置连接
                        RedisConnectionManager.resetConnectionResource(cacheHandleProcessorModel.getCacheConfigModel(), config);
                    }
                } else {
                    //如果是多连接模式
                    //获取执行器，获取到当前使用的连接，重置连接
                    final AbstractLettuceHandleExecutor cacheExecutor = (AbstractLettuceHandleExecutor) CacheExecutorFactory.getCacheExecutor(config, cacheHandleProcessorModel.getCacheConfigModel());
                    final LettuceConnectLoop.ResourceNode<StatefulRedisConnection> loopStatefulConnection = cacheExecutor.getLoopStatefulConnection();
                    //重新获取一个连接，重置到loop中
                    if (!loopStatefulConnection.getResource().isOpen()) {
                        final StatefulRedisConnection<String, String> lettuceConnection = lettuceConnectionFactory.getLettuceConnection((LettuceConnectSourceConfig) config);
                        lettuceConnectResource.getLettuceConnectLoop().resetResource(loopStatefulConnection.getIndex(), lettuceConnection);
                    }
                }
            }
        } catch (Exception e) {
            CacheExceptionFactory.addErrorLog("LettuceHandlesPostProcessor reset connection fail !", e);
        }
    }
}
