package org.jrl.redis.extend.handle.processor;

import org.jrl.redis.core.constant.HandlePostProcessorTypeEnum;
import org.jrl.redis.core.constant.RedisClientConstants;
import org.jrl.redis.core.model.CacheHandleProcessorModel;
import org.jrl.redis.core.processor.AbstractHandlePostProcessor;
import org.jrl.redis.core.processor.factory.HandlePostFactory;
import org.jrl.redis.executor.CacheExecutorFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.jrl.tools.json.JrlJsonNoExpUtil;
import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;
import redis.clients.jedis.exceptions.JedisNoScriptException;

import javax.annotation.PostConstruct;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: RedisHandlesPostProcessor
 * @Description: redis命令后置处理器实现类
 * @date 2021/1/18 9:05 PM
 */
public class JedisHandlesPostProcessor extends AbstractHandlePostProcessor {

    private static Logger LOGGER = JrlLoggerFactory.getLogger(JedisHandlesPostProcessor.class);
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
        return RedisClientConstants.JEDIS;
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
            if (LettuceHandlesPostProcessor.BRPOP.equals(cacheHandleProcessorModel.getCommands())) {
                return;
            }
            LOGGER.warn("redis execute time more than {}ms，耗时：{}ms ，命令：{} , key : {} , val : {}", LOG_TIME, cacheHandleProcessorModel.getExecuteTime(), cacheHandleProcessorModel.getCommands(),
                    CollectionUtils.isNotEmpty(cacheHandleProcessorModel.getKeys()) ? JrlJsonNoExpUtil.toJson(cacheHandleProcessorModel.getKeys()) : cacheHandleProcessorModel.getKey(), JrlJsonNoExpUtil.toJson(cacheHandleProcessorModel.getResult()));
        }
    }

    @Override
    public void onFail(CacheHandleProcessorModel cacheHandleProcessorModel) {
        /**
         * 如果是lua脚本异常，重新缓存
         */
        if (cacheHandleProcessorModel.getE() instanceof JedisNoScriptException) {
            //获取执行器调用脚本缓存操作
            CacheExecutorFactory.getCacheExecutor(null, cacheHandleProcessorModel.getCacheConfigModel()).loadLuaScripts();
        }
    }
}
