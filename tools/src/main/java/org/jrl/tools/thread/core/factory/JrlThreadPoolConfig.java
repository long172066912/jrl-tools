package org.jrl.tools.thread.core.factory;

import org.jrl.tools.thread.api.JrlThreadShutdownHandler;
import org.jrl.tools.thread.core.VariableLinkedBlockingQueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 线程池配置
 *
 * @author JerryLong
 */
public class JrlThreadPoolConfig {
    private JrlThreadPoolConfig(){}
    /**
     * 核心线程数
     */
    private int corePoolSize = 5;
    /**
     * 最大线程数
     */
    private int maxPoolSize = 10;
    /**
     * 线程存活时间
     */
    private int keepAliveTime = 30;
    /**
     * 时间单位
     */
    private TimeUnit unit = TimeUnit.SECONDS;
    /**
     * 队列大小
     */
    private int queueSize = 1024;
    /**
     * 阻塞队列，支持动态变更
     */
    private Function<Integer, BlockingQueue<Runnable>> workQueue = JrlDynamicQueue::new;
    /**
     * 拒绝策略，默认使用主线程执行
     */
    private Supplier<RejectedExecutionHandler> rejectedExecutionHandler = ThreadPoolExecutor.CallerRunsPolicy::new;
    /**
     * 是否监控
     */
    private boolean isMonitor = true;
    /**
     * 是否支持动态配置
     */
    private boolean isDynamicConfiguration = true;
    /**
     * 是否定时线程池
     */
    private boolean isSchedule = false;
    /**
     * 是否预创建核心线程
     */
    private boolean isPreheat = false;
    /**
     * 关闭等待时间，秒
     */
    private int shutdownWaitTime = JrlThreadPoolFactory.THREAD_POOL_SHUTDOWN_TIME;
    /**
     * 关闭优先级，约大越优先
     */
    private int shutdownOrder = 0;
    /**
     * shutdown失败处理器，默认不处理，打印错误日志
     */
    private Supplier<JrlThreadShutdownHandler> shutdownFailHandler = () -> null;
    /**
     * 是否线程优先
     * 如果设置为true，将开启JrlDynamicQueue的线程优先
     */
    private boolean isThreadPriority = false;

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public int getKeepAliveTime() {
        return keepAliveTime;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public Function<Integer, BlockingQueue<Runnable>> getWorkQueue() {
        return workQueue;
    }

    public Supplier<RejectedExecutionHandler> getRejectedExecutionHandler() {
        return rejectedExecutionHandler;
    }

    public boolean isMonitor() {
        return isMonitor;
    }

    public boolean isDynamicConfiguration() {
        return isDynamicConfiguration;
    }

    public boolean isSchedule() {
        return isSchedule;
    }

    public boolean isPreheat() {
        return isPreheat;
    }

    public int getShutdownWaitTime() {
        return shutdownWaitTime;
    }

    public int getShutdownOrder() {
        return shutdownOrder;
    }

    public Supplier<JrlThreadShutdownHandler> getShutdownFailHandler() {
        return shutdownFailHandler;
    }

    public boolean isThreadPriority() {
        return isThreadPriority;
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private JrlThreadPoolConfig config;

        public Builder() {
            config = new JrlThreadPoolConfig();
        }

        protected Builder(JrlThreadPoolConfig config) {
            this.config = config;
        }

        public Builder corePoolSize(int corePoolSize) {
            config.corePoolSize = corePoolSize;
            return this;
        }

        public Builder maxPoolSize(int maxPoolSize) {
            config.maxPoolSize = maxPoolSize;
            return this;
        }

        public Builder keepAliveTime(int keepAliveTime) {
            config.keepAliveTime = keepAliveTime;
            return this;
        }

        public Builder timeunit(TimeUnit unit) {
            config.unit = unit;
            return this;
        }

        public Builder queueSize(Integer queueSize) {
            config.queueSize = queueSize;
            return this;
        }

        /**
         * 阻塞队列，使用 {@link VariableLinkedBlockingQueue} 支持动态变更
         * 其他的不支持动态变更
         *
         * @param workQueue 工作队列
         * @return Builder
         */
        public Builder workQueue(Function<Integer, BlockingQueue<Runnable>> workQueue) {
            config.workQueue = workQueue;
            return this;
        }

        public Builder rejectedExecutionHandler(Supplier<RejectedExecutionHandler> rejectedExecutionHandler) {
            config.rejectedExecutionHandler = rejectedExecutionHandler;
            return this;
        }

        public Builder mointor() {
            config.isMonitor = true;
            return this;
        }

        public Builder dynamic() {
            config.isDynamicConfiguration = true;
            return this;
        }

        public Builder schedule() {
            config.isSchedule = true;
            return this;
        }

        public Builder preheat() {
            config.isPreheat = true;
            return this;
        }

        /**
         * 优雅关闭时间，建议不设置，使用默认的{@link JrlThreadPoolFactory#THREAD_POOL_SHUTDOWN_TIME}
         *
         * @param shutdownWaitTime 不能小于10秒，不能大于30秒
         * @return {@link Builder}
         */
        public Builder shutdownWaitTime(int shutdownWaitTime) {
            if (shutdownWaitTime <= 10 || shutdownWaitTime > JrlThreadPoolFactory.THREAD_POOL_SHUTDOWN_TIME) {
                shutdownWaitTime = JrlThreadPoolFactory.THREAD_POOL_SHUTDOWN_TIME;
            }
            config.shutdownWaitTime = shutdownWaitTime;
            return this;
        }

        public Builder shutdownOrder(int shutdownOrder) {
            config.shutdownOrder = shutdownOrder;
            return this;
        }

        public Builder shutdownFailHandler(Supplier<JrlThreadShutdownHandler> shutdownFailHandler) {
            config.shutdownFailHandler = shutdownFailHandler;
            return this;
        }

        public Builder threadPriority() {
            //创建队列，队列必须是JrlDynamicQueue才支持线程优先
            if (config.getWorkQueue().apply(config.getQueueSize()).getClass() != JrlDynamicQueue.class) {
                throw new IllegalArgumentException("threadPriority only support JrlDynamicQueue !");
            }
            config.isThreadPriority = true;
            return this;
        }

        public JrlThreadPoolConfig build() {
            return config;
        }
    }
}
