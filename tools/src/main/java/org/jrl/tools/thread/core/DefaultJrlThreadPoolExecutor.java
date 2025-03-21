package org.jrl.tools.thread.core;

import org.jrl.tools.thread.api.*;
import org.jrl.tools.thread.api.callback.JrlThreadCallback;
import org.jrl.tools.thread.core.factory.JrlThreadPool;
import org.jrl.tools.thread.core.factory.JrlThreadPoolConfig;
import org.jrl.tools.thread.core.lossless.LosslessTaskJrlThreadPoolExecutor;
import org.jrl.tools.thread.core.merge.DefaultJrlMergeThreadExecutor;
import org.jrl.tools.thread.core.priority.DefaultJrlPriorityTaskThreadExecutor;
import org.jrl.tools.thread.core.shard.DefaultJrlShardThreadExecutor;
import org.jrl.tools.thread.core.stream.DefaultJrlThreadStreamExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 默认线程执行器
 *
 * @author JerryLong
 */
public class DefaultJrlThreadPoolExecutor implements JrlThreadPoolExecutor {

    private static Logger LOGGER = LoggerFactory.getLogger(JrlThreadPoolExecutor.class);

    private static JrlThreadPoolExecutor EXECUTOR;
    /**
     * 分片处理器集合
     */
    private static Map<String, JrlShardThreadExecutor<?>> shardThreadExecutorMap = new ConcurrentHashMap<>();


    /**
     * 获取单例
     *
     * @return 线程池执行器
     */
    public static JrlThreadPoolExecutor getInstance() {
        if (EXECUTOR == null) {
            synchronized (DefaultJrlThreadPoolExecutor.class) {
                if (EXECUTOR == null) {
                    EXECUTOR = new DefaultJrlThreadPoolExecutor();
                }
            }
        }
        return EXECUTOR;
    }

    @Override
    public <T> Future<T> execute(JrlThreadPool pool, Callable<T> task) {
        return pool.execute(task);
    }

    @Override
    public void execute(JrlThreadPool pool, Runnable task) {
        pool.execute(task);
    }

    @Override
    public void execute(JrlThreadPool pool, Runnable task, JrlThreadCallback callback) {
        pool.execute(task, callback);
    }

    @Override
    public void execute(JrlThreadPool pool, Runnable task, int delay, TimeUnit timeUnit) {
        pool.scheduleAtFixedRate(task, delay, delay, timeUnit);
    }

    @Override
    public void execute(JrlThreadPool pool, Runnable task, int initialDelay, int delay, TimeUnit timeUnit) {
        pool.scheduleAtFixedRate(task, initialDelay, delay, timeUnit);
    }

    @Override
    public List<JrlThreadResponse> execute(JrlThreadPool pool, int timeout, List<Callable<Object>> tasks) {
        if (null == tasks || tasks.size() == 0) {
            return Collections.emptyList();
        }
        List<Future<Object>> futures = new ArrayList<>(tasks.size());
        for (Callable<Object> task : tasks) {
            futures.add(this.execute(pool, task));
        }
        return pool.getFutureResult(timeout, futures);
    }

    @Override
    public JrlPriorityThreadExecutor priorityTask(JrlThreadPool pool, int timeout) {
        return new DefaultJrlPriorityTaskThreadExecutor(timeout, pool);
    }

    @Override
    public <T> LosslessTaskJrlThreadPoolExecutor<T> losslessTask(JrlThreadPool pool, Consumer<T> fallbackConsumer) {
        return new LosslessTaskJrlThreadPoolExecutor<>(pool, fallbackConsumer);
    }

    @Override
    public <T> JrlShardThreadExecutor<T> shard(JrlThreadPool pool, String name, int count, JrlShardThreadExecutor.JrlShardRule<T> shardRule) {
        return (JrlShardThreadExecutor<T>) shardThreadExecutorMap.computeIfAbsent(name,
                e -> new DefaultJrlShardThreadExecutor<>(name, count, shardRule, JrlThreadPoolConfig.builder().build(), pool)
        );
    }

    @Override
    public <T> JrlShardThreadExecutor<T> shard(JrlThreadPool pool, String name, int count, JrlShardThreadExecutor.JrlShardRule<T> shardRule, JrlThreadPoolConfig config) {
        return (JrlShardThreadExecutor<T>) shardThreadExecutorMap.computeIfAbsent(name, e -> new DefaultJrlShardThreadExecutor<>(name, count, shardRule, config, pool));
    }

    @Override
    public <T> JrlShardThreadExecutor<T> shard(JrlThreadPool pool, String name, int count, JrlShardThreadExecutor.JrlShardRule<T> shardRule, JrlThreadPoolConfig config, boolean preheat) {
        return (JrlShardThreadExecutor<T>) shardThreadExecutorMap.computeIfAbsent(name, e -> new DefaultJrlShardThreadExecutor<>(name, count, shardRule, config, pool, preheat));
    }


    @Override
    public <T> JrlMergeThreadExecutor<T> merge(JrlThreadPool pool, String name, JrlMergeThreadExecutor.JrlMergeRule<T> rule, JrlMergeThreadExecutor.JrlMergeConsumer<T> consumer) {
        return new DefaultJrlMergeThreadExecutor<>(name, rule, consumer, pool);
    }

    @Override
    public JrlThreadStream stream(JrlThreadPool pool) {
        return new DefaultJrlThreadStreamExecutor(pool);
    }
}
