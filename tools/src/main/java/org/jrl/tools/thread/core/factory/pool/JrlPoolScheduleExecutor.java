package org.jrl.tools.thread.core.factory.pool;

import org.jrl.tools.thread.core.factory.JrlThreadPoolConfig;

import java.util.List;
import java.util.concurrent.*;

/**
 * 线程池异步包装实现
 *
 * @author JerryLong
 */
public class JrlPoolScheduleExecutor extends JrlPoolExecutor implements ScheduledExecutorService {

    public JrlPoolScheduleExecutor(String name, JrlThreadPoolConfig config, ThreadPoolExecutor executor) {
        super(name, config, executor);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return ((ScheduledExecutorService) super.getExecutor()).schedule(() -> {
            try {
                command.run();
            } catch (Throwable e) {
                LOGGER.error("jrl-thread-schedule execute error !", e);
                //监控执行异常
                monitorFailTask("task");
            }
        }, delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return ((ScheduledExecutorService) super.getExecutor()).schedule(() -> {
            try {
                return callable.call();
            } catch (Throwable e) {
                LOGGER.error("jrl-thread-schedule execute error !", e);
                //监控执行异常
                monitorFailTask("task");
                throw e;
            }
        }, delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return ((ScheduledExecutorService) super.getExecutor()).scheduleAtFixedRate(() -> {
            try {
                command.run();
            } catch (Throwable e) {
                LOGGER.error("jrl-thread-schedule execute error ! poolName : {}", super.getName(), e);
                //监控执行异常
                monitorFailTask("task");
            }
        }, initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return ((ScheduledExecutorService) super.getExecutor()).scheduleWithFixedDelay(() -> {
            try {
                command.run();
            } catch (Throwable e) {
                LOGGER.error("jrl-thread-schedule execute error !", e);
                //监控执行异常
                monitorFailTask("task");
            }
        }, initialDelay, delay, unit);
    }

    @Override
    public void shutdown() {
        LOGGER.info("jrl-thread pool shutdown ! name : {} , time(s) : {}", super.getName(), super.getConfig().getShutdownWaitTime());
        super.awaitClose();
    }

    @Override
    public List<Runnable> shutdownNow() {
        LOGGER.info("jrl-thread pool shutdown now ! name : {} , time(s) : {}", super.getName(), super.getConfig().getShutdownWaitTime());
        return ((ScheduledExecutorService) super.getExecutor()).shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return ((ScheduledExecutorService) super.getExecutor()).isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return ((ScheduledExecutorService) super.getExecutor()).isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return ((ScheduledExecutorService) super.getExecutor()).awaitTermination(timeout, unit);
    }
}
