package org.jrl.redis.connect.scheduled;

import org.jrl.redis.config.CacheBasicConfig;
import org.jrl.redis.executor.CacheExecutorFactory;
import org.jrl.redis.connect.RedisConnectionManager;
import org.jrl.redis.core.BaseCacheExecutor;
import org.jrl.redis.core.constant.UseTypeEnum;
import org.jrl.redis.exception.CacheExceptionFactory;
import org.jrl.redis.util.async.AsyncExecutorUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: HeartCheckScheduled
 * @Description: 连接心跳检测，防止redis长期不操作导致请求timeout异常,https://github.com/lettuce-io/lettuce-core/issues/1260
 * @date 2021/1/28 2:12 PM
 */
public class HeartCheckScheduled extends CacheExecutorFactory {

    private static final String CONNECTION_CLOSE_ERROR = "Connection is closed";

    static {

        /**
         * 拿到所有连接，发送一条心跳命令，如果失败则重连
         * 30秒1次
         */
        AsyncExecutorUtils.submitScheduledTask(() -> {
            //获取当前配置
            for (BaseCacheExecutor baseCacheExecutor : executorMap.values()) {
                if (baseCacheExecutor.getCacheConfigModel().getUseType().equals(UseTypeEnum.PUBSUB)) {
                    break;
                }
                try {
                    baseCacheExecutor.setex("cache:heart", 60, "1");
                } catch (Exception e) {
                    CacheExceptionFactory.addWarnLog("HeartCheckScheduled", e, "心跳检测失败");
                    if(e.getMessage().contains(CONNECTION_CLOSE_ERROR)){
                        //重新创建连接
                        try {
                            RedisConnectionManager.resetConnectionResource(baseCacheExecutor.getCacheConfigModel(), baseCacheExecutor.getRedisSourceConfig());
                        } catch (Exception e1) {
                            CacheExceptionFactory.addErrorLog("DbSourceScheduled", "dbConfigChange", "连接资源替换异常！", e1);
                        }
                    }
                }
            }
        }, 300, CacheBasicConfig.HEART_CHECK_lINTERVAL_SECONDS, TimeUnit.SECONDS);
    }
}
