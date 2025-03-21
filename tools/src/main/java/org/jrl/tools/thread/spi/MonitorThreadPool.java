package org.jrl.tools.thread.spi;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * MonitorThreadPool
 * base-component通过spi方式扩展出线程池监控
 *
 * @author JerryLong
 */
public interface MonitorThreadPool {

    /**
     * 监控线程池
     *
     * @param name     线程池名称
     * @param executor 线程池
     * @param <T>      线程池，必须继承自{@link ThreadPoolExecutor}
     * @return 监控后的线程池
     */
    <T extends ThreadPoolExecutor> T monitor(String name, T executor);

    class DefaultMonitorThreadPool implements MonitorThreadPool {

        @Override
        public <T extends ThreadPoolExecutor> T monitor(String name, T executor) {
            return executor;
        }
    }
}
