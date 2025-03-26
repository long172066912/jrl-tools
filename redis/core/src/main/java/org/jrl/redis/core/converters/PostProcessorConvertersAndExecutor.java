package org.jrl.redis.core.converters;

import com.google.common.collect.Sets;
import org.jrl.redis.core.constant.HandlePostProcessorTypeEnum;
import org.jrl.redis.core.model.CacheHandleProcessorModel;
import org.jrl.redis.core.monitor.MonitorFactory;
import org.jrl.redis.core.monitor.MonitorProducer;
import org.jrl.redis.core.processor.AbstractHandlePostProcessor;
import org.jrl.redis.core.processor.factory.HandlePostFactory;
import org.jrl.redis.exception.CacheExceptionConstants;
import org.jrl.redis.exception.CacheExceptionFactory;
import io.lettuce.core.RedisCommandTimeoutException;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Set;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: PostProcessorConvertersAndExecutor
 * @Description: 后置处理转换执行器
 * @date 2021/1/18 3:03 PM
 */
public class PostProcessorConvertersAndExecutor {
    private static final Set<String> WARN_COMMANDS = Sets.newHashSet("brpop", "blpop", "brpoplpush");

    /**
     * @param
     * @return 返回类型
     * @throws
     * @Title:
     * @Description: 根据后置类型获取执行链路
     * @author JerryLong
     * @date 2021/1/18 3:48 PM
     */
    public List<AbstractHandlePostProcessor> getHandlePostProcessors(HandlePostProcessorTypeEnum handlePostProcessorTypeEnum, int clientType) {
        return HandlePostFactory.getBeanPostProcessors(handlePostProcessorTypeEnum, clientType);
    }

    /**
     * 将命令转换成链式执行
     *
     * @param handleLinkList
     * @param cacheHandleProcessorModel
     * @return
     */
    public Object executeHandles(List<AbstractHandlePostProcessor> handleLinkList, CacheHandleProcessorModel cacheHandleProcessorModel) {
        try {
            if (CollectionUtils.isNotEmpty(handleLinkList)) {
                cacheHandleProcessorModel = this.executeByHandleLinkList(handleLinkList, cacheHandleProcessorModel);
            } else {
                cacheHandleProcessorModel = this.execute(cacheHandleProcessorModel);
            }

            //如果发生异常，抛出异常
            if (null != cacheHandleProcessorModel.getE()) {
                //如果是阻塞类型的命令，timeout不打error log
                if (cacheHandleProcessorModel.getE() instanceof RedisCommandTimeoutException && WARN_COMMANDS.contains(cacheHandleProcessorModel.getCommands())) {
                    //增加warn日志
                    CacheExceptionFactory.addWarnLog(" execute error ! command : {}", cacheHandleProcessorModel.getE(), cacheHandleProcessorModel.getCommands());
                    CacheExceptionFactory.throwExceptionNoLog(CacheExceptionConstants.CACHE_ERROR_CODE, "System error ! cache handle execute error !", null == cacheHandleProcessorModel.getCacheConfigModel() ? "" : cacheHandleProcessorModel.getCacheConfigModel().getCacheType() + " : " + cacheHandleProcessorModel.getCommands() + " : " + cacheHandleProcessorModel.getKey(), cacheHandleProcessorModel.getE());
                } else {
                    CacheExceptionFactory.throwException(CacheExceptionConstants.CACHE_ERROR_CODE, "System error ! cache handle execute error !", null == cacheHandleProcessorModel.getCacheConfigModel() ? "" : cacheHandleProcessorModel.getCacheConfigModel().getCacheType() + " : " + cacheHandleProcessorModel.getCommands() + " : " + cacheHandleProcessorModel.getKey(), cacheHandleProcessorModel.getE());
                }
            }
            return cacheHandleProcessorModel.getResult();
        } finally {
            //如果开启监控，生产监控消息
            if (null != cacheHandleProcessorModel.getCacheConfigModel() && cacheHandleProcessorModel.getCacheConfigModel().isOpenMonitor()) {
                MonitorProducer.addCommands(MonitorFactory.MonitorData.builder()
                        .cacheType(cacheHandleProcessorModel.getCacheConfigModel().getCacheType())
                        .commands(cacheHandleProcessorModel.getCommands())
                        .key(cacheHandleProcessorModel.getKey())
                        .hitKey(cacheHandleProcessorModel.getHitKey())
                        .isHit(cacheHandleProcessorModel.getResult() != null)
                        .keys(cacheHandleProcessorModel.getKeys())
                        .result(null == cacheHandleProcessorModel.getE())
                        .executeTime(cacheHandleProcessorModel.getExecuteTime().intValue())
                );
            }
        }
    }

    /**
     * 执行命令
     *
     * @param cacheHandleProcessorModel
     * @return
     */
    private CacheHandleProcessorModel execute(CacheHandleProcessorModel cacheHandleProcessorModel) {
        //执行命令
        long start = System.currentTimeMillis();
        try {
            cacheHandleProcessorModel.setResult(cacheHandleProcessorModel.getFunction().apply());
            cacheHandleProcessorModel.setExecuteTime(System.currentTimeMillis() - start);
        } catch (Exception e) {
            //此处不打印错误日志，
            cacheHandleProcessorModel.setE(e);
            cacheHandleProcessorModel.setExecuteTime(System.currentTimeMillis() - start);
        }
        return cacheHandleProcessorModel;
    }

    /**
     * 通过后置处理链路方式执行
     *
     * @param handleLinkList
     * @param cacheHandleProcessorModel
     * @return
     */
    private CacheHandleProcessorModel executeByHandleLinkList(List<AbstractHandlePostProcessor> handleLinkList, CacheHandleProcessorModel cacheHandleProcessorModel) {
        int i = 0;
        int mid = handleLinkList.size() >> 1;
        boolean isSpecifiedCommands = true;
        for (AbstractHandlePostProcessor abstractHandlePostProcessor : handleLinkList) {
            /**
             * 执行前置
             */
            //如果指定了命令，则使用限定命令
            isSpecifiedCommands = CollectionUtils.isEmpty(abstractHandlePostProcessor.specifiedCommands()) || abstractHandlePostProcessor.specifiedCommands().contains(cacheHandleProcessorModel.getCommands());
            i++;
            //命令前执行
            if (i <= mid && isSpecifiedCommands) {
                try {
                    abstractHandlePostProcessor.handleBefore(cacheHandleProcessorModel);
                } catch (Exception e) {
                    CacheExceptionFactory.addErrorLog("PostProcessorConvertersAndExecutor", "executeHandles->handleBefore", "processorType:[1],processorId:[2]", e, abstractHandlePostProcessor.getHandleType(), abstractHandlePostProcessor.getHandlePostId());
                }
            }

            //判断是否到中间点
            if (i == mid) {
                //执行命令
                cacheHandleProcessorModel = this.execute(cacheHandleProcessorModel);
            }

            //执行后置，命令后执行
            if (i > mid && isSpecifiedCommands) {
                if (null == cacheHandleProcessorModel.getE()) {
                    //命令后执行
                    try {
                        abstractHandlePostProcessor.handleAfter(cacheHandleProcessorModel);
                    } catch (Exception e) {
                        CacheExceptionFactory.addErrorLog("PostProcessorConvertersAndExecutor", "executeHandles->handleAfter", "processorType:{},processorId:{}", e, abstractHandlePostProcessor.getHandleType(), abstractHandlePostProcessor.getHandlePostId());
                    }
                    //命令成功执行
                    try {
                        abstractHandlePostProcessor.onSuccess(cacheHandleProcessorModel);
                    } catch (Exception e) {
                        CacheExceptionFactory.addErrorLog("PostProcessorConvertersAndExecutor", "executeHandles->onSuccess", "processorType:{},processorId:{}", e, abstractHandlePostProcessor.getHandleType(), abstractHandlePostProcessor.getHandlePostId());
                    }
                } else {
                    //命令失败执行
                    try {
                        abstractHandlePostProcessor.onFail(cacheHandleProcessorModel);
                    } catch (Exception e) {
                        CacheExceptionFactory.addErrorLog("PostProcessorConvertersAndExecutor", "executeHandles->onFail", "processorType:{},processorId:{}", e, abstractHandlePostProcessor.getHandleType(), abstractHandlePostProcessor.getHandlePostId());
                    }
                }
            }
        }
        return cacheHandleProcessorModel;
    }
}
