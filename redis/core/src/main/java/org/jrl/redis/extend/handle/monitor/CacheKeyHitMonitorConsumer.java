package org.jrl.redis.extend.handle.monitor;

import org.jrl.redis.core.constant.MonitorTypeEnum;
import org.jrl.redis.core.monitor.AbstractCacheMonitorConsumer;
import org.jrl.redis.core.monitor.MonitorFactory;
import org.jrl.redis.util.CacheHitCounterUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 缓存命中率消费
 *
 * @author JerryLong
 */
public class CacheKeyHitMonitorConsumer extends AbstractCacheMonitorConsumer {

    @Override
    public MonitorTypeEnum getType() {
        return MonitorTypeEnum.HITKEY;
    }

    @Override
    public Object doMonitor(MonitorFactory.MonitorData monitorData) {
        if (StringUtils.isNotBlank(monitorData.getKey())) {
            CacheHitCounterUtil.hitCount(monitorData.getCacheType(), monitorData.getCommands(), monitorData.getHitKey(), monitorData.getKey(), monitorData.isHit());
        } else if (CollectionUtils.isNotEmpty(monitorData.getKeys())) {
            CacheHitCounterUtil.hitCount(monitorData.getCacheType(), monitorData.getCommands(), monitorData.getHitKey(), monitorData.getKeys(), monitorData.isHit());
        }
        return null;
    }
}
