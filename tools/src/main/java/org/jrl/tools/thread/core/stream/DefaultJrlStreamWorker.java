package org.jrl.tools.thread.core.stream;

import org.jrl.tools.thread.api.task.JrlStreamWorker;
import org.jrl.tools.thread.core.JrlFuture;
import org.jrl.tools.thread.core.factory.JrlThreadPool;
import org.jrl.tools.utils.function.JrlRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * 流量编排默认实现，使用{@link CompletableFuture}实现
 *
 * @author JerryLong
 */
public class DefaultJrlStreamWorker<T> implements JrlStreamWorker<T> {

    private static Logger LOGGER = LoggerFactory.getLogger(DefaultJrlStreamWorker.class);

    private final JrlThreadPool pool;
    private final CompletableFuture<T> completableFuture;

    public DefaultJrlStreamWorker(JrlThreadPool pool, Callable<T> callable) {
        this.pool = pool;
        completableFuture = (CompletableFuture<T>) pool.execute(callable);
    }

    protected DefaultJrlStreamWorker(JrlThreadPool pool, CompletableFuture<T> completableFuture) {
        this.pool = pool;
        this.completableFuture = completableFuture;
    }

    @Override
    public void thenRun(JrlRunnable<T> task) {
        this.completableFuture.thenRunAsync(() -> {
            try {
                task.run(completableFuture.get());
            } catch (Throwable e) {
                LOGGER.error("jrl-thread JrlStreamWorker thenRun error ! poolName : {}", pool.getName(), e);
            }
        }, pool);
    }

    @Override
    public void thenRun(BaJrlStreamRunnable<T> tasks) {
        if (null == tasks || null == tasks.getRunnables() || tasks.getRunnables().size() == 0) {
            return;
        }
        completableFuture.thenRun(() -> {
            try {
                final T t = completableFuture.get();
                tasks.getRunnables().forEach(task -> CompletableFuture.runAsync(() -> task.run(t), pool));
            } catch (Throwable e) {
                LOGGER.error("jrl-thread JrlStreamWorker thenRun tasks {} error ! poolName : {}", tasks.getRunnables().size(), pool.getName(), e);
            }
        });
    }

    @Override
    public <R> JrlStreamWorker<R> thenSupply(Function<T, R> task) {
        return new DefaultJrlStreamWorker<>(pool, completableFuture.thenApplyAsync(task, pool));
    }

    @Override
    public <R> JrlStreamWorker<List<R>> thenSupply(BaJrlStreamWorker<T, R> tasks) {
        if (null == tasks || null == tasks.getWorkers() || tasks.getWorkers().size() == 0) {
            return new DefaultJrlStreamWorker<>(pool, CompletableFuture.completedFuture(null));
        }
        final CompletableFuture<List<R>> listCompletableFuture = completableFuture.thenApplyAsync(t -> {
            final CompletableFuture<R>[] completableFutures = new CompletableFuture[tasks.getWorkers().size()];
            for (int i = 0; i < tasks.getWorkers().size(); i++) {
                final Function<T, R> trFunction = tasks.getWorkers().get(i);
                completableFutures[i] = CompletableFuture.supplyAsync(() -> trFunction.apply(t), pool);
            }
            CompletableFuture.allOf(completableFutures).join();
            List<R> result = new ArrayList<>(tasks.getWorkers().size());
            for (CompletableFuture<R> future : completableFutures) {
                try {
                    result.add(future.get());
                } catch (Throwable e) {
                    LOGGER.error("jrl-thread JrlStreamWorker thenSupply error ! poolName : {}", pool.getName(), e);
                }
            }
            return result;
        }, pool);
        return new DefaultJrlStreamWorker<>(pool, listCompletableFuture);

    }

    @Override
    public <R, V> BiJrlStreamRes<R, V> thenSupply(BiJrlStreamCallable<T, R, V> task) {
        return new BiJrlStreamRes<>(this.thenSupply(task.getLeft()), this.thenSupply(task.getRight()));
    }

    @Override
    public <R, V> JrlStreamWorker<V> combine(JrlStreamWorker<R> r, JrlStreamCombine<T, R, V> combineTask) {
        final CompletableFuture<V> vCompletableFuture = completableFuture.thenCombineAsync(((DefaultJrlStreamWorker) r).getCompletableFuture(), combineTask::run, pool);
        return new DefaultJrlStreamWorker<V>(pool, vCompletableFuture);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return completableFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return completableFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return completableFuture.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        try {
            return completableFuture.get(JrlFuture.TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return completableFuture.get(timeout, unit);
    }

    public CompletableFuture<T> getCompletableFuture() {
        return this.completableFuture;
    }
}
