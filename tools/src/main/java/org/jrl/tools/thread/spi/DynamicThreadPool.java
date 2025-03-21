package org.jrl.tools.thread.spi;

import org.jrl.tools.thread.core.factory.JrlThreadPoolConfig;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 对线程池进行动态配置
 * base-component通过spi方式扩展出Apollo方式的动态线程池
 *
 * @author JerryLong
 */
public interface DynamicThreadPool {

    /**
     * 修改线程池配置
     *
     * @param name     线程池名称
     * @param config   线程池配置
     * @return 返回处理过的线程池配置
     */
    JrlThreadPoolConfig modifyConfig(String name, JrlThreadPoolConfig config);

    /**
     * 动态线程池
     *
     * @param name     线程池名称
     * @param executor 线程池
     * @param <T>      线程池，必须继承自{@link ThreadPoolExecutor}
     * @return 返回处理过的动态线程池
     */
    <T extends ThreadPoolExecutor> T dynamic(String name, T executor);

    class DefaultDynamicThreadPool implements DynamicThreadPool {

        @Override
        public JrlThreadPoolConfig modifyConfig(String name, JrlThreadPoolConfig config) {
            return config;
        }

        @Override
        public <T extends ThreadPoolExecutor> T dynamic(String name, T executor) {
            return executor;
        }
    }
}
