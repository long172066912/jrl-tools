package org.jrl.redis.extend.handle.monitor;

import org.jrl.redis.core.constant.MonitorTypeEnum;
import org.jrl.redis.core.monitor.AbstractCacheMonitorConsumer;
import org.jrl.redis.core.monitor.MonitorFactory;
import org.jrl.redis.exception.CacheExceptionFactory;
import org.jrl.redis.extend.handle.monitor.hotkey.HotKeyMonitor;
import org.jrl.redis.extend.handle.monitor.hotkey.model.HotKeyItem;
import org.jrl.redis.util.async.AsyncExecutorUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: TimerMonitor
 * @Description: 时间监控实现
 * @date 2021/7/1 2:57 PM
 */
public class CacheHotKeyMonitorConsumer extends AbstractCacheMonitorConsumer {

    private static Logger LOGGER = JrlLoggerFactory.getLogger(CacheHotKeyMonitorConsumer.class);

    private static HotKeyMonitor hotKeyMonitor = HotKeyMonitor.SINGLETON;

    public CacheHotKeyMonitorConsumer() {
        /**
         * 定时30秒调用一次
         */
        AsyncExecutorUtils.submitScheduledTask(() -> {
            try {
                hotKeyMonitor.getHotkeyStatisticsMap().forEach((key1, value) -> {
                    try {
                        List<HotKeyItem> hotkeys = value.getHotKeysAndClean();
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("JrlRedisCacheHotKeyMonitorConsumer cacheType : {} , hotkey size : {}", key1, hotkeys.size());
                        }
                        if (CollectionUtils.isNotEmpty(hotkeys)) {
                            //monitor-count todo
//                            hotkeys.forEach(key -> JrlMonitor.count("monitorKey", key.getCount().intValue(), "host", CacheExecutorFactory.getDefaultHost(key1), "cacheType", key1, "command", key.getCommands(), "hotKey", key.getKey()));
                        }
                    } catch (Exception e) {
                        CacheExceptionFactory.addErrorLog("HotKeyMonitor synchronization error ! cacheType:{}", key1, e);
                    }
                });
            } catch (Exception e) {
                CacheExceptionFactory.addErrorLog("HotKeyMonitor synchronization error !", e);
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    @Override
    public MonitorTypeEnum getType() {
        return MonitorTypeEnum.HOTKEY;
    }

    @Override
    public Object doMonitor(MonitorFactory.MonitorData monitorData) {
        if (StringUtils.isNotBlank(monitorData.getKey())) {
            hotKeyMonitor.doMonitor(monitorData.getCacheType(), monitorData.getCommands(), monitorData.getKey());
        } else if (CollectionUtils.isNotEmpty(monitorData.getKeys())) {
            monitorData.getKeys().forEach(e -> hotKeyMonitor.doMonitor(monitorData.getCacheType(), monitorData.getCommands(), e));
        }
        return null;
    }
}
