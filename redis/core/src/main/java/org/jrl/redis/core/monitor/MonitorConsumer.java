package org.jrl.redis.core.monitor;

import org.jrl.redis.exception.CacheExceptionFactory;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: MonitorConsumer
 * @Description: 监控消费
 * @date 2021/7/13 4:00 PM
 */
public class MonitorConsumer {

    /**
     * 消费
     */
    public void onEvent(MonitorFactory.MonitorData monitorData) throws Exception {
        if (null != monitorData) {
            MonitorFactory.MONITOR_MAP.forEach((key, value) -> {
                try {
                    value.doMonitor(monitorData);
                } catch (Exception e1) {
                    CacheExceptionFactory.addWarnLog("MonitorConsumer error ! monitorType:{}", e1, key.name());
                }
            });
        }
    }
}
