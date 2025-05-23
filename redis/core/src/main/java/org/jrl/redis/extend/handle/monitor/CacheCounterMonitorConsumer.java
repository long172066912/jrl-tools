package org.jrl.redis.extend.handle.monitor;

import org.jrl.redis.core.constant.MonitorTypeEnum;
import org.jrl.redis.core.monitor.AbstractCacheMonitorConsumer;
import org.jrl.redis.core.monitor.MonitorFactory;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: TimerMonitor
 * @Description: 统计类监控实现
 * @date 2021/7/1 2:57 PM
 */
public class CacheCounterMonitorConsumer extends AbstractCacheMonitorConsumer {

    @Override
    public MonitorTypeEnum getType() {
        return MonitorTypeEnum.COUNT;
    }

    @Override
    public Object doMonitor(MonitorFactory.MonitorData monitorData) {
        return null;
    }
}
