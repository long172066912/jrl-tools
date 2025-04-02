package org.jrl.tools.thread.core.factory;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import org.jrl.tools.spi.JrlSpiLoader;
import org.jrl.tools.thread.spi.DynamicThreadPool;
import org.jrl.tools.thread.spi.MonitorThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池工厂
 *
 * @author JerryLong
 */
public class JrlThreadPoolFactory {

    private static Logger LOGGER = LoggerFactory.getLogger(JrlThreadPoolFactory.class);

    private static final DynamicThreadPool DYNAMIC_THREAD_POOL = JrlSpiLoader.getInstanceOrDefault(DynamicThreadPool.class, DynamicThreadPool.DefaultDynamicThreadPool::new);
    private static final MonitorThreadPool MONITOR_THREAD_POOL = JrlSpiLoader.getInstanceOrDefault(MonitorThreadPool.class, MonitorThreadPool.DefaultMonitorThreadPool::new);
    /**
     * 所有线程池的管理
     */
    private static final Map<String, JrlThreadPool> poolMap = new ConcurrentHashMap<>();
    private static final String THREAD_NAME_PREFIX = "JrlThreadPool-";
    private static final String SCHEDULE_THREAD_NAME_PREFIX = "JrlScheduleThreadPool-";
    /**
     * 所有线程池shutdown时间
     */
    public static final int THREAD_POOL_SHUTDOWN_TIME = Integer.parseInt(System.getProperty("jrl.thread.shutdown.time", "30"));

    static {
        //增加所有线程的监控
        monitor();
        //添加优雅关闭
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            final long l = System.currentTimeMillis();
            try {
                Thread.sleep(THREAD_POOL_SHUTDOWN_TIME * 1000);
            } catch (InterruptedException e) {
            }
            LOGGER.info("jrl-thread shutdown ! poolSize : {}", poolMap.size());
            poolMap.values().stream()
                    .sorted((o1, o2) -> o2.getConfig().getShutdownOrder() - o1.getConfig().getShutdownOrder())
                    .forEach(JrlThreadPool::close);
            poolMap.values().stream()
                    .sorted((o1, o2) -> o2.getConfig().getShutdownOrder() - o1.getConfig().getShutdownOrder())
                    .forEach(pool -> {
                        pool.awaitClose();
                        poolMap.remove(pool.getName());
                    });
            LOGGER.info("jrl-thread shutdown success ! poolSize : {} , cost : {}", poolMap.size(), System.currentTimeMillis() - l);
        }));
    }

    /**
     * 获取线程池，如果不存在则创建
     *
     * @param name   线程池名称
     * @param config 线程池配置
     * @param <T>    线程池类型
     * @return 线程池
     */
    protected static <T extends ExecutorService> T computeIfAbsent(String name, JrlThreadPoolConfig config) {
        JrlThreadPool pool = poolMap.get(name);
        //此处不能用poolMap本身的computeIfAbsent，使用JrlNewThreadPoolRunsPolicyRejected 时，会持续死锁
        if (null == pool) {
            synchronized (JrlThreadPoolFactory.class) {
                pool = poolMap.get(name);
                if (pool == null) {
                    ThreadPoolExecutor executor;
                    if (config.isDynamicConfiguration()) {
                        config = DYNAMIC_THREAD_POOL.modifyConfig(name, config);
                    }
                    if (config.isSchedule()) {
                        executor = new ScheduledThreadPoolExecutor(config.getCorePoolSize(), new JrlDefaultThreadFactory(SCHEDULE_THREAD_NAME_PREFIX + name));
                    } else {
                        executor = new ThreadPoolExecutor(config.getCorePoolSize(), config.getMaxPoolSize(), config.getKeepAliveTime(), config.getUnit(), config.getWorkQueue().apply(config.getQueueSize()), new JrlDefaultThreadFactory(THREAD_NAME_PREFIX + name), config.getRejectedExecutionHandler().get());
                    }
                    if (config.isDynamicConfiguration()) {
                        executor = DYNAMIC_THREAD_POOL.dynamic(name, executor);
                    }
                    if (config.isMonitor()) {
                        executor = MONITOR_THREAD_POOL.monitor(name, executor);
                    }
                    pool = JrlThreadPool.create(name, config, executor);
                    poolMap.putIfAbsent(name, pool);
                }
            }
        }
        return (T) pool;
    }

    protected static void monitor() {
        Metrics.gaugeMapSize("jrl.thread.pool.size", Tags.empty(), poolMap);
        Metrics.gauge("jrl.thread.thread.size", poolMap, (pool) -> pool.values().stream().mapToInt(JrlThreadPool::getActiveThreadSize).sum());
        Metrics.gauge("jrl.thread.thread.max", poolMap, (pool) -> pool.values().stream().mapToInt(JrlThreadPool::getMaxThreadSize).sum());
        Metrics.gauge("jrl.thread.queue.size", poolMap, (pool) -> pool.values().stream().mapToInt(JrlThreadPool::getQueueSize).sum());
        Metrics.gauge("jrl.thread.queue.max", poolMap, (pool) -> pool.values().stream().mapToInt(JrlThreadPool::getMaxQueueSize).sum());
    }
}