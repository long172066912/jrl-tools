package org.jrl.tools.cache.spi.monitor;

import io.micrometer.core.instrument.Metrics;

import java.util.concurrent.TimeUnit;

/**
 * 缓存-监控-spi
 *
 * @author JerryLong
 */
public interface JrlCacheMonitorSpi {
    /**
     * 监控缓存命中率
     *
     * @param cache               缓存类型
     * @param type                缓存类型
     * @param command             缓存操作命令
     * @param executeMilliseconds 耗时(毫秒)
     * @param throwable           异常
     */
    void monitor(String cache, String type, String command, long executeMilliseconds, Throwable throwable);

    /**
     * 默认实现-Metrics
     */
    public static class DefaultJrlCacheMonitorSpi implements JrlCacheMonitorSpi {

        @Override
        public void monitor(String cache, String type, String command, long executeMilliseconds, Throwable throwable) {
            Metrics.globalRegistry.timer("jrl.cache.execute",
                    "name", cache, "type", type, "command", command, "result", throwable == null ? "SUCCESS" : "ERROR"
            ).record(executeMilliseconds, TimeUnit.MILLISECONDS);
        }
    }
}
