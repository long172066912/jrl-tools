package org.jrl.thread;

import org.jrl.tools.thread.core.VariableLinkedBlockingQueue;
import org.jrl.tools.thread.core.factory.JrlThreadPoolConfig;
import org.jrl.tools.thread.core.factory.rejected.JrlDiscardRejected;
import org.jrl.tools.thread.core.factory.rejected.JrlRetryRejected;
import org.jrl.tools.thread.spi.DynamicThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 动态线程池，Apollo配置方式实现
 *
 * @author JerryLong
 */
public class JrlApolloDynamicThreadPool implements DynamicThreadPool {

    public static final int MIN_QUEUE_SIZE = 10;
    private static Logger LOGGER = LoggerFactory.getLogger(JrlApolloDynamicThreadPool.class);

    public static final String JRL_THREAD_POOL_CONFIG = "jrlThreadPoolConfig";
    /**
     * 线程池集合
     */
    private static final Map<String, JrlDynamicThreadPool> DYNAMIC_THREAD_POOL_MAP = new ConcurrentHashMap<>();

    @Override
    public JrlThreadPoolConfig modifyConfig(String name, JrlThreadPoolConfig config) {
        //获取配置，如果有配置，则用配置内的，此处的配置，是启动生效的
        final JrlThreadPoolApolloConfig apolloConfig = getThreadPoolConfig(name);
        if (null != apolloConfig) {
            final JrlThreadPoolConfig.Builder builder = config.toBuilder();
            //队列长度，小于等于0，用SynchronousQueue
            if (null != apolloConfig.getQueueSize() && apolloConfig.getQueueSize() <= 0) {
                LOGGER.info("jrl-thread : {} use SynchronousQueue !", name);
                builder.workQueue((s) -> new SynchronousQueue<>());
            }
            //线程优先
            if (null != apolloConfig.getThreadPriority() && apolloConfig.getThreadPriority() == 1) {
                LOGGER.info("jrl-thread : {} threadPriority !", name);
                builder.threadPriority();
            }
            return builder.build();
        }
        return config;
    }

    @Override
    public <T extends ThreadPoolExecutor> T dynamic(String name, T executor) {
        //线程池添加到集合中
        DYNAMIC_THREAD_POOL_MAP.put(name, new JrlDynamicThreadPool(executor));
        //获取配置，如果有配置，则用配置内的
        final JrlThreadPoolApolloConfig threadPoolConfig = getThreadPoolConfig(name);
        if (null != threadPoolConfig) {
            changePool(name, threadPoolConfig, executor);
        }
        //开启动态变更
        openDynamicChange();
        LOGGER.info("jrl-thread ApolloDynamicThreadPool Config ! name : {}, core : {} , max : {} , keepAliveTime : {} , queueSize : {} , rejected : {}",
                name, executor.getCorePoolSize(), executor.getMaximumPoolSize(),
                executor.getKeepAliveTime(TimeUnit.SECONDS), executor.getQueue().size(),
                executor.getRejectedExecutionHandler().getClass().getSimpleName());
        return executor;
    }


    private static JrlThreadPoolApolloConfig getThreadPoolConfig(String name) {
        //todo 从Apollo中获取线程配置
        return null;
    }

    /**
     * 添加listener，动态变更线程池
     */
    private static void openDynamicChange() {
        //todo 增加Apollo listener，动态变更线程池配置
        // DYNAMIC_THREAD_POOL_MAP.get
        // changePool(name, jrlThreadPoolConfig, executor);
    }

    private static void changePool(String changedKey, JrlThreadPoolApolloConfig jrlThreadPoolApolloConfig, ThreadPoolExecutor executor) {
        if (null != jrlThreadPoolApolloConfig) {
            if (checkSize(jrlThreadPoolApolloConfig.getCore())) {
                executor.setCorePoolSize(jrlThreadPoolApolloConfig.getCore());
            }
            if (checkSize(jrlThreadPoolApolloConfig.getMax()) && jrlThreadPoolApolloConfig.getMax() >= executor.getCorePoolSize()) {
                executor.setMaximumPoolSize(jrlThreadPoolApolloConfig.getMax());
            }
            if (checkSize(jrlThreadPoolApolloConfig.getKeepAliveTime())) {
                executor.setKeepAliveTime(jrlThreadPoolApolloConfig.getKeepAliveTime(), TimeUnit.SECONDS);
            }
            //设置队列长度
            if (checkSize(jrlThreadPoolApolloConfig.getQueueSize()) && executor.getQueue() instanceof VariableLinkedBlockingQueue) {
                //如果是动态可调整的，最小必须是10
                if (jrlThreadPoolApolloConfig.getQueueSize() < MIN_QUEUE_SIZE) {
                    jrlThreadPoolApolloConfig.setQueueSize(MIN_QUEUE_SIZE);
                }
                ((VariableLinkedBlockingQueue<?>) executor.getQueue()).setCapacity(jrlThreadPoolApolloConfig.getQueueSize());
            }
            //拒绝策略，1：默认策略（主线程执行），2：丢弃并记录策略，3：丢弃最老任务策略，4：直接抛出异常策略，5：重试策略
            if (checkSize(jrlThreadPoolApolloConfig.getRejectType())) {
                switch (jrlThreadPoolApolloConfig.getRejectType()) {
                    case 1:
                        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
                        break;
                    case 2:
                        executor.setRejectedExecutionHandler(new JrlDiscardRejected(changedKey));
                        break;
                    case 3:
                        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
                        break;
                    case 4:
                        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
                        break;
                    case 5:
                    default:
                        if (checkSize(jrlThreadPoolApolloConfig.getRetryCount())) {
                            executor.setRejectedExecutionHandler(new JrlRetryRejected(changedKey, 1, jrlThreadPoolApolloConfig.getRetryCount()));
                        } else {
                            executor.setRejectedExecutionHandler(new JrlRetryRejected(changedKey));
                        }
                }
            }
            LOGGER.info("jrl-thread pool change success ! name : {}, core : {} , max : {} , keepAliveTime : {} , queueMaxSize : {} , queueSize : {} , rejected : {}",
                    changedKey, executor.getCorePoolSize(), executor.getMaximumPoolSize(),
                    executor.getKeepAliveTime(TimeUnit.SECONDS), jrlThreadPoolApolloConfig.getQueueSize(), executor.getQueue().size(),
                    executor.getRejectedExecutionHandler().getClass().getSimpleName());
        }
    }

    private static boolean checkSize(Integer size) {
        return null != size && size > 0;
    }

    /**
     * 动态线程池
     */
    private static class JrlDynamicThreadPool {
        private final ThreadPoolExecutor executor;

        private JrlDynamicThreadPool(ThreadPoolExecutor executor) {
            this.executor = executor;
        }

        public ThreadPoolExecutor getExecutor() {
            return executor;
        }
    }

    private static class JrlThreadPoolApolloConfig {
        private Integer core;
        private Integer max;
        /**
         * 线程池中线程空闲时间，单位秒
         */
        private Integer keepAliveTime;
        /**
         * 队列大小，使用 VariableLinkedBlockingQueue时生效
         */
        private Integer queueSize;
        /**
         * 拒绝策略，1：默认策略（主线程执行），2：丢弃并记录策略，3：丢弃最老任务策略，4：直接抛出异常策略，5：重试策略
         */
        private Integer rejectType;
        /**
         * 重试次数，rejectType = 5 生效
         */
        private Integer retryCount;
        /**
         * 1、线程优先，0、队列优先
         */
        private Integer threadPriority;

        public JrlThreadPoolApolloConfig() {
        }

        public JrlThreadPoolApolloConfig(Integer core, Integer max, Integer keepAliveTime, Integer queueSize, Integer rejectType, Integer retryCount, Integer threadPriority) {
            this.core = core;
            this.max = max;
            this.keepAliveTime = keepAliveTime;
            this.queueSize = queueSize;
            this.rejectType = rejectType;
            this.retryCount = retryCount;
            this.threadPriority = threadPriority;
        }

        public Integer getCore() {
            return core;
        }

        public void setCore(Integer core) {
            this.core = core;
        }

        public Integer getMax() {
            return max;
        }

        public void setMax(Integer max) {
            this.max = max;
        }

        public Integer getKeepAliveTime() {
            return keepAliveTime;
        }

        public void setKeepAliveTime(Integer keepAliveTime) {
            this.keepAliveTime = keepAliveTime;
        }

        public Integer getQueueSize() {
            return queueSize;
        }

        public void setQueueSize(Integer queueSize) {
            this.queueSize = queueSize;
        }

        public Integer getRejectType() {
            return rejectType;
        }

        public void setRejectType(Integer rejectType) {
            this.rejectType = rejectType;
        }

        public Integer getRetryCount() {
            return retryCount;
        }

        public void setRetryCount(Integer retryCount) {
            this.retryCount = retryCount;
        }

        public Integer getThreadPriority() {
            return threadPriority;
        }

        public void setThreadPriority(Integer threadPriority) {
            this.threadPriority = threadPriority;
        }
    }
}
