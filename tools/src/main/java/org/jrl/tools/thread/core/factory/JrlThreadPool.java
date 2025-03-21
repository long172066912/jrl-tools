package org.jrl.tools.thread.core.factory;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import org.jrl.tools.thread.api.JrlThreadShutdownHandler;
import org.jrl.tools.thread.api.JrlThreadStream;
import org.jrl.tools.thread.api.callback.JrlThreadCallback;
import org.jrl.tools.thread.api.task.JrlTaskWrapper;
import org.jrl.tools.thread.core.JrlFuture;
import org.jrl.tools.thread.core.JrlThreadResponse;
import org.jrl.tools.thread.core.factory.pool.JrlPoolExecutor;
import org.jrl.tools.thread.core.factory.pool.JrlPoolScheduleExecutor;
import org.jrl.tools.thread.core.factory.rejected.JrlDiscardRejected;
import org.jrl.tools.thread.core.stream.DefaultJrlThreadStreamExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * 线程池包装类，必须通过 JrlThreadPoolBuilder 创建
 * JrlThreadPoolBuilder.builder("poolName").build()
 *
 * @author JerryLong
 */
public abstract class JrlThreadPool implements ExecutorService {

    protected static Logger LOGGER = LoggerFactory.getLogger(JrlThreadPool.class);

    private final String name;
    private final ThreadPoolExecutor threadPoolExecutor;
    private final Executor monitorExecutor;
    private final JrlThreadPoolConfig config;
    private JrlThreadShutdownHandler shutdownFailHandler;
    private static final JrlThreadCallback<Object> DEFAULT_CALLBACK = new JrlThreadCallback<Object>() {
    };
    private static final Tag JRL_MONITOR_TAG = Tag.of("type", "jrl");
    private Long shutdownTime;

    protected JrlThreadPool(String name, JrlThreadPoolConfig config, ThreadPoolExecutor executor) {
        this.name = name;
        this.config = config;
        this.threadPoolExecutor = executor;
        shutdownFailHandler = Optional.ofNullable(config.getShutdownFailHandler()).map(Supplier::get).orElse(null);
        if (config.isPreheat()) {
            this.threadPoolExecutor.prestartAllCoreThreads();
        }
        if (config.isMonitor()) {
            this.monitorExecutor = ExecutorServiceMetrics.monitor(Metrics.globalRegistry, this.threadPoolExecutor, name, JRL_MONITOR_TAG);
        } else {
            this.monitorExecutor = this.threadPoolExecutor;
        }
        //如果是线程优先
        if (config.isThreadPriority() && this.threadPoolExecutor.getQueue() instanceof JrlDynamicQueue) {
            ((JrlDynamicQueue<?>) this.threadPoolExecutor.getQueue()).setThreadPriority(true);
            ((JrlDynamicQueue<?>) this.threadPoolExecutor.getQueue()).setPoolExecutor(this.getExecutor());
        }
    }

    /**
     * 创建线程池
     *
     * @param name     线程池名称
     * @param config   配置
     * @param executor 真正的执行器
     * @return AbstractJrlThreadPool
     */
    protected static JrlThreadPool create(String name, JrlThreadPoolConfig config, ThreadPoolExecutor executor) {
        if (config.isSchedule()) {
            return new JrlPoolScheduleExecutor(name, config, executor);
        } else {
            return new JrlPoolExecutor(name, config, executor);
        }
    }

    public String getName() {
        return name;
    }

    public boolean isSchedule() {
        return config.isSchedule();
    }

    public ThreadPoolExecutor getExecutor() {
        return threadPoolExecutor;
    }

    /**
     * 执行任务
     *
     * @param task 任务
     * @param <T>  任务返回类型
     * @return future
     */
    public <T> Future<T> execute(Callable<T> task) {
        final JrlFuture<T> future = new JrlFuture<>();
        this.executeTask(task, new JrlThreadCallback<T>() {
            @Override
            public void onSuccess(T result) {
                future.complete(result);
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        return future;
    }

    /**
     * 执行任务
     *
     * @param task 任务
     */
    @Override
    public void execute(Runnable task) {
        this.execute(task, DEFAULT_CALLBACK);
    }

    /**
     * 执行任务
     *
     * @param task     任务
     * @param callback 回调
     */
    public void execute(Runnable task, JrlThreadCallback<Object> callback) {
        this.executeTask(() -> {
            task.run();
            return 1;
        }, callback);
    }

    /**
     * 执行任务
     *
     * @param task     任务
     * @param callback 回调
     * @param <T>      对象类型
     */
    public <T> void execute(Callable<T> task, JrlThreadCallback<T> callback) {
        this.executeTask(task, callback);
    }

    public void setShutdownFailHandler(JrlThreadShutdownHandler shutdownFailHandler) {
        this.shutdownFailHandler = shutdownFailHandler;
    }

    /**
     * 执行任务，核心处理方法，通过注入callback处理后续逻辑
     *
     * @param task     任务
     * @param callback 回调
     * @param <T>      任务返回类型
     */
    protected <T> void executeTask(Callable<T> task, JrlThreadCallback<T> callback) {
        try {
            this.monitorExecutor.execute(new JrlTaskWrapper<Callable<T>>() {
                @Override
                public void run() {
                    try {
                        callback.onSuccess(task.call());
                    } catch (Throwable e) {
                        LOGGER.error("jrl-thread execute error ! poolName : {}", name, e);
                        //监控执行异常
                        monitorFailTask("task");
                        callback.onError(e);
                    }
                }

                @Override
                public Callable<T> getTask() {
                    return task;
                }
            });
        } catch (Throwable e) {
            //监控执行异常
            monitorFailTask("rejected");
            //如果是丢弃的拒绝策略，直接返回null
            if (e instanceof JrlDiscardRejected.JrlThreadDiscardException) {
                //不做任何处理
            } else {
                throw e;
            }
        }
    }

    /**
     * 定时任务
     *
     * @param command      任务
     * @param initialDelay 第一次初始化时间
     * @param period       间隔时间
     * @param unit         时间单位
     * @return {@link ScheduledFuture}
     */
    public abstract ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit);

    /**
     * 获取任务执行结果
     *
     * @param timeout 超时时间
     * @param futures 异步Future集合
     * @return 集合
     */
    public List<JrlThreadResponse> getFutureResult(int timeout, List<Future<Object>> futures) {
        List<JrlThreadResponse> list = new ArrayList<>(futures.size());
        int time = timeout;
        long l = System.currentTimeMillis();
        for (int i = 0; i < futures.size(); i++) {
            final JrlThreadResponse jrlThreadResponse = new JrlThreadResponse();
            try {
                list.add(i, jrlThreadResponse.setResult(futures.get(i).get(time, TimeUnit.MILLISECONDS)));
            } catch (Throwable e) {
                list.add(i, jrlThreadResponse.setThrowable(e));
            }
            //扣减时间
            time = time > 0 ? timeout - (int) (System.currentTimeMillis() - l) : 0;
        }
        return list;
    }

    /**
     * 优雅停止 shutdown
     */
    protected void close() {
        LOGGER.info("jrl-thread pool shutdown begin ! name : {} , time(s) : {} , queueLength : {}", name, config.getShutdownWaitTime(), this.threadPoolExecutor.getQueue().size());
        shutdownTime = System.currentTimeMillis();
        this.threadPoolExecutor.shutdown();
    }

    /**
     * 关闭线程池
     */
    protected void awaitClose() {
        if (null == shutdownTime) {
            close();
        }
        try {
            //开始shutdown
            Optional.ofNullable(shutdownFailHandler).ifPresent(jrlThreadShutdownHandler -> {
                try {
                    jrlThreadShutdownHandler.onShutdown(this.threadPoolExecutor);
                } catch (Throwable e) {
                    LOGGER.error("jrl-thread shutdown onShutdown error !", e);
                }
            });
            if (!this.threadPoolExecutor.awaitTermination(config.getShutdownWaitTime(), TimeUnit.SECONDS)) {
                //如果shutdown未完成，业务扩展处理
                Optional.ofNullable(shutdownFailHandler).ifPresent(jrlThreadShutdownHandler -> {
                    try {
                        jrlThreadShutdownHandler.onFail(this.threadPoolExecutor);
                    } catch (Throwable e) {
                        LOGGER.error("jrl-thread shutdown onFail error !", e);
                    }
                });
                this.threadPoolExecutor.shutdownNow();
            }
            LOGGER.info("jrl-thread pool shutdown success ! name : {} , time : {}", name, System.currentTimeMillis() - shutdownTime);
        } catch (InterruptedException e) {
            LOGGER.error("jrl-thread pool shutdown awaitTermination error ! name : {} , hopeTime : {} , 实际shutdown毫秒 : {}", name, config.getShutdownWaitTime(), System.currentTimeMillis() - shutdownTime);
        }
    }

    protected void monitorFailTask(String reason) {
        if (!config.isMonitor()) {
            return;
        }
        //监控执行异常
        try {
            Metrics.counter("executor.fail", Arrays.asList(JRL_MONITOR_TAG, Tag.of("name", this.name), Tag.of("reason", reason))).increment();
        } catch (Exception e) {
            //百分之一概率打印错误日志
            if (ThreadLocalRandom.current().nextInt(100) == 1) {
                LOGGER.error("jrl-thread monitor fail error !", e);
            }
        }
    }

    protected JrlThreadPoolConfig getConfig() {
        return this.config;
    }

    /**
     * 获取线程流
     *
     * @return 线程流
     */
    public JrlThreadStream stream() {
        return new DefaultJrlThreadStreamExecutor(this);
    }

    public int getQueueSize() {
        return this.threadPoolExecutor.getQueue().size();
    }

    public int getMaxQueueSize() {
        if (this.threadPoolExecutor.getQueue() instanceof JrlDynamicQueue) {
            return ((JrlDynamicQueue<?>) this.threadPoolExecutor.getQueue()).getCapacity();
        }
        return this.threadPoolExecutor.getQueue().size();
    }

    public int getActiveThreadSize() {
        return this.threadPoolExecutor.getActiveCount();
    }

    public int getMaxThreadSize() {
        return this.threadPoolExecutor.getMaximumPoolSize();
    }
}
