package org.jrl.tools.thread.core.factory.pool;

import org.jrl.tools.thread.api.callback.JrlThreadCallback;
import org.jrl.tools.thread.core.JrlFuture;
import org.jrl.tools.thread.core.factory.JrlThreadPool;
import org.jrl.tools.thread.core.factory.JrlThreadPoolConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 线程池包装类
 *
 * @author JerryLong
 */
public class JrlPoolExecutor extends JrlThreadPool implements ExecutorService {

    public JrlPoolExecutor(String name, JrlThreadPoolConfig config, ThreadPoolExecutor executor) {
        super(name, config, executor);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.execute(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        final JrlFuture<T> jrlFuture = new JrlFuture<>();
        super.execute(task, new JrlThreadCallback<Object>() {
            @Override
            public void onSuccess(Object obj) {
                jrlFuture.complete(result);
            }

            @Override
            public void onError(Throwable throwable) {
                jrlFuture.completeExceptionally(throwable);
            }
        });
        return jrlFuture;
    }

    @Override
    public Future<?> submit(Runnable task) {
        final JrlFuture<?> jrlFuture = new JrlFuture<>();
        super.execute(task, new JrlThreadCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                jrlFuture.complete(null);
            }

            @Override
            public void onError(Throwable throwable) {
                jrlFuture.completeExceptionally(throwable);
            }
        });
        return jrlFuture;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        List<Future<T>> futures = new ArrayList<>(tasks.size());
        for (Callable<T> task : tasks) {
            futures.add(super.execute(task));
        }
        return futures;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return super.getExecutor().invokeAll(
                tasks.stream().map(task -> (Callable<T>) () -> {
                    try {
                        return task.call();
                    } catch (Throwable e) {
                        LOGGER.error("jrl-thread pool invokeAll error !", e);
                        monitorFailTask("task");
                        throw e;
                    }
                }).collect(Collectors.toList()), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return super.getExecutor().invokeAny(
                tasks.stream().map(task -> (Callable<T>) () -> {
                    try {
                        return task.call();
                    } catch (Throwable e) {
                        LOGGER.error("jrl-thread pool invokeAll error !", e);
                        monitorFailTask("task");
                        throw e;
                    }
                }).collect(Collectors.toList()));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return super.getExecutor().invokeAny(
                tasks.stream().map(task -> (Callable<T>) () -> {
                    try {
                        return task.call();
                    } catch (Throwable e) {
                        LOGGER.error("jrl-thread pool invokeAll error !", e);
                        monitorFailTask("task");
                        throw e;
                    }
                }).collect(Collectors.toList()), timeout, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        throw new UnsupportedOperationException("不支持该方法");
    }

    @Override
    public void shutdown() {
        super.awaitClose();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return super.getExecutor().shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return super.getExecutor().isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return super.getExecutor().isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return super.getExecutor().awaitTermination(timeout, unit);
    }
}
