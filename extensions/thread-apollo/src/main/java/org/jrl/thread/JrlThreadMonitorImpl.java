package org.jrl.thread;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import org.jrl.tools.thread.spi.MonitorThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池队列长度监控
 *
 * @author JerryLong
 */
public class JrlThreadMonitorImpl implements MonitorThreadPool {

    private static Logger LOGGER = LoggerFactory.getLogger(JrlThreadMonitorImpl.class);

    public static final String EXECUTOR_QUEUE_MAX_MONITOR_KEY = "executor.queue.max";

    @Override
    public <T extends ThreadPoolExecutor> T monitor(String name, T executor) {
        LOGGER.info("jrl-thread monitor init ! pool : {}", name);
        //监控队列最大长度
        Metrics.globalRegistry.gauge(EXECUTOR_QUEUE_MAX_MONITOR_KEY, Collections.singletonList(Tag.of("name", name)), executor, value -> executor.getQueue().size());
        return executor;
    }
}
