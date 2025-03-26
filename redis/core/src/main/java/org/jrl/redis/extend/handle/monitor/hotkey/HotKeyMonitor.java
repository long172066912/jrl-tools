package org.jrl.redis.extend.handle.monitor.hotkey;

import org.jrl.redis.extend.handle.monitor.statistics.AbstractHotKeyStatistics;
import org.jrl.redis.extend.handle.monitor.statistics.SkipMapStatistics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: HotKeyMonitor
 * @Description: HotKeyMonitor
 * * 1、命令异步塞入队列，多个消费者消费队列中的key，保证命令不丢失
 * * 2、消费队列，将key进行热key探测，保留前？100个
 * * 3、通过定时，暂停消费，将热key集合推送，清除热key探测的数据，重新开始消费
 * @date 2021/7/2 11:18 AM
 */
public class HotKeyMonitor {
    public static final HotKeyMonitor SINGLETON = new HotKeyMonitor();
    /**
     * 热key统计map大小
     */
    public static final int HOT_KEY_MAP_SIZE = 10000;
    /**
     * 热key统计实现，key：cacheType
     */
    private static Map<String, AbstractHotKeyStatistics> statisticsMap = new ConcurrentHashMap<>();

    public Map<String, AbstractHotKeyStatistics> getHotkeyStatisticsMap() {
        return statisticsMap;
    }

    /**
     * 热key监控
     *
     * @param cacheType
     * @param key
     */
    public void doMonitor(String cacheType, String commands, String key) {
        statisticsMap.computeIfAbsent(cacheType, e -> new SkipMapStatistics("hot",HOT_KEY_MAP_SIZE)).hotKeyIncr(commands, key);
    }
}
