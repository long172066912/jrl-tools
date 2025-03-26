package org.jrl.redis.core.monitor;

import org.jrl.redis.config.CacheBasicConfig;
import org.jrl.redis.core.constant.MonitorTypeEnum;
import org.jrl.redis.exception.CacheExceptionFactory;
import org.jrl.redis.extend.handle.monitor.CacheHotKeyMonitorConsumer;
import org.jrl.redis.extend.handle.monitor.CacheKeyHitMonitorConsumer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: MonitorFactory
 * @Description: 监控数据工厂
 * @date 2021/7/5 3:21 PM
 */
public class MonitorFactory {
    /**
     * Monitor实现类集合，一种类型支持一个
     */
    protected static Map<MonitorTypeEnum, AbstractCacheMonitorConsumer> MONITOR_MAP = new ConcurrentHashMap<>();

    /**
     * 监控命令队列
     */
    protected static LinkedBlockingQueue<MonitorData> MONITOR_QUEUE = new LinkedBlockingQueue<>(CacheBasicConfig.MONITOR_QUEUE_SIZE);

    static {
        final CacheHotKeyMonitorConsumer cacheHotKeyMonitorConsumer = new CacheHotKeyMonitorConsumer();
        final CacheKeyHitMonitorConsumer cacheKeyHitMonitorConsumer = new CacheKeyHitMonitorConsumer();
        cacheHotKeyMonitorConsumer.regester();
        cacheKeyHitMonitorConsumer.regester();
        new Thread(() -> {
            final MonitorConsumer monitorConsumer = new MonitorConsumer();
            while (true) {
                try {
                    monitorConsumer.onEvent(MONITOR_QUEUE.take());
                } catch (Exception e) {
                    CacheExceptionFactory.addWarnLog("MonitorConsumer take error!", e);
                }
            }
        }, "JrlRedis-monitor-consumer").start();
    }

    /**
     * 注册消费者实现类
     *
     * @param type
     * @param monitorConsumer
     */
    public static void monitorRegist(MonitorTypeEnum type, AbstractCacheMonitorConsumer monitorConsumer) {
        MONITOR_MAP.put(type, monitorConsumer);
    }

    /**
     * 队列中添加消息
     *
     * @param monitorData
     */
    public static void addMonitorMsg(MonitorData monitorData) {
        try {
            MONITOR_QUEUE.put(monitorData);
        } catch (Exception e) {
            CacheExceptionFactory.addWarnLog("MonitorProducer addCommands error ! monitorData:{}", e, monitorData.toString());
        }
    }

    public static class MonitorDataEvent {
        private MonitorData monitorData;

        public MonitorData getMonitorData() {
            return monitorData;
        }

        public void setMonitorData(MonitorData monitorData) {
            this.monitorData = monitorData;
        }
    }

    /**
     * @author JerryLong
     * @version V1.0
     * @Title: MonitorFactory
     * @Description: 监控实体信息
     * @date 2021/7/5 3:56 PM
     */
    public static class MonitorData {
        private String cacheType;
        private String commands;
        private String key;
        private List<String> keys;
        private String hitKey;
        private boolean isHit;
        private int executeTime;
        private boolean result;

        public static MonitorData builder() {
            return new MonitorData();
        }

        public MonitorData build() {
            return this;
        }

        public String getCacheType() {
            return cacheType;
        }

        public MonitorData cacheType(String cacheType) {
            this.cacheType = cacheType;
            return this;
        }

        public String getCommands() {
            return commands;
        }

        public MonitorData commands(String commands) {
            this.commands = commands;
            return this;
        }

        public String getKey() {
            return key;
        }

        public MonitorData key(String key) {
            this.key = key;
            return this;
        }

        public List<String> getKeys() {
            return keys;
        }

        public MonitorData keys(List<String> keys) {
            this.keys = keys;
            return this;
        }

        public boolean getResult() {
            return result;
        }

        public MonitorData result(boolean result) {
            this.result = result;
            return this;
        }

        public int getExecuteTime() {
            return executeTime;
        }

        public MonitorData executeTime(int executeTimes) {
            this.executeTime = executeTimes;
            return this;
        }

        public String getHitKey() {
            return hitKey;
        }

        public MonitorData hitKey(String hitKey) {
            this.hitKey = hitKey;
            return this;
        }

        @Override
        public String toString() {
            return "MonitorData{" +
                    ", cacheType='" + cacheType + '\'' +
                    ", commands='" + commands + '\'' +
                    ", key='" + key + '\'' +
                    ", keys=" + keys +
                    ", executeTime=" + executeTime +
                    ", result='" + result + '\'' +
                    '}';
        }

        public MonitorData isHit(boolean isHit) {
            this.isHit = isHit;
            return this;
        }

        public boolean isHit() {
            return isHit;
        }
    }
}
