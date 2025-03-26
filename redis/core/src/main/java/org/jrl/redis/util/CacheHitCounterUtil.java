package org.jrl.redis.util;

import org.jrl.redis.core.BaseCacheExecutor;
import org.jrl.redis.core.CacheHitKeyConvertor;
import org.jrl.redis.core.constant.CommandsDataTypeEnum;
import org.jrl.redis.core.constant.CommandsReadWriteTypeEnum;
import org.jrl.redis.core.model.CacheHandleProcessorModel;
import org.jrl.redis.executor.CacheExecutorFactory;
import org.jrl.redis.extend.handle.monitor.hit.CacheHitCounter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: CacheHitCounterUtil
 * @Description: //TODO (用一句话描述该文件做什么)
 * @date 2024/1/15 16:17
 */
public class CacheHitCounterUtil {

    /**
     * 缓存命中率统计
     *
     * @param cacheType
     * @param commands
     * @param key
     * @param isHit
     */
    public static void hitCount(String cacheType, String commands, String hitKey, String key, boolean isHit) {
        if (!isNeedHitCount(commands)) {
            return;
        }
        try {
            doHit(hitKey, cacheType, key, isHit);
        } catch (Throwable e) {
        }
    }

    public static void hitCount(String cacheType, String commands, String hitKey, List<String> keys, boolean isHit) {
        if (!isNeedHitCount(commands)) {
            return;
        }
        try {
            if (CollectionUtils.isNotEmpty(keys)) {
                for (String key : keys) {
                    doHit(hitKey, cacheType, key, isHit);
                }
            }
        } catch (Throwable e) {
        }
    }

    /**
     * 缓存命中率统计
     *
     * @param cacheHandleProcessorModel
     */
    public static void hitCount(CacheHandleProcessorModel cacheHandleProcessorModel) {
        if (!isNeedHitCount(cacheHandleProcessorModel)) {
            return;
        }
        final BaseCacheExecutor cacheExecutor = CacheExecutorFactory.getCacheExecutor(null, cacheHandleProcessorModel.getCacheConfigModel());
        if (null == cacheExecutor) {
            return;
        }
        try {
            boolean isHit = cacheHandleProcessorModel.getResult() != null;
            final String cacheType = cacheHandleProcessorModel.getCacheConfigModel().getCacheType();
            if (CollectionUtils.isNotEmpty(cacheHandleProcessorModel.getKeys())) {
                for (String key : cacheHandleProcessorModel.getKeys()) {
                    doHit(cacheExecutor.getHitKeyThreadLocal(), cacheType, key, isHit);
                }
            } else {
                doHit(cacheExecutor.getHitKeyThreadLocal(), cacheType, cacheHandleProcessorModel.getKey(), isHit);
            }
        } catch (Throwable e) {
        } finally {
            cacheExecutor.cleanHitKey();
        }
    }

    private static void doHit(String hitKey, String cacheType, String key, boolean isHit) {
        if (StringUtils.isBlank(hitKey)) {
            final CacheHitKeyConvertor cacheHitKeyConvertor = BaseCacheExecutor.getCacheHitKeyConvertor(cacheType);
            if (null != cacheHitKeyConvertor) {
                hitKey = cacheHitKeyConvertor.convert(key);
            }
        }
        if (StringUtils.isNotBlank(hitKey)) {
            CacheHitCounter.count(cacheType, hitKey, isHit);
        }
    }

    /**
     * 判断是否需要统计命中率
     *
     * @param cacheHandleProcessorModel
     * @return
     */
    private static boolean isNeedHitCount(CacheHandleProcessorModel cacheHandleProcessorModel) {
        return isNeedHitCount(cacheHandleProcessorModel.getCommands());
    }

    private static boolean isNeedHitCount(String commands) {
        return CommandsDataTypeEnum.getCommandsReadWriteType(commands).equals(CommandsReadWriteTypeEnum.READ);
    }
}
