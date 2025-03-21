package org.jrl.tools.thread.core.shard;

import org.jrl.tools.thread.api.JrlShardThreadExecutor;
import org.jrl.tools.thread.core.factory.JrlThreadPool;
import org.jrl.tools.thread.core.factory.JrlThreadPoolBuilder;
import org.jrl.tools.thread.core.factory.JrlThreadPoolConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 分片线程池实现
 * 根据分片规则，将任务分发到多个线程池
 *
 * @author JerryLong
 */
public class DefaultJrlShardThreadExecutor<T> implements JrlShardThreadExecutor<T> {

    private final String name;
    private final int count;
    private final JrlShardRule<T> shardRule;
    /**
     * 分片线程池集合
     */
    private final Map<Integer, JrlThreadPool> shardPools = new ConcurrentHashMap<>();
    /**
     * 线程配置
     */
    private final JrlThreadPoolConfig config;
    /**
     * 调度线程池
     */
    private final JrlThreadPool pool;

    public DefaultJrlShardThreadExecutor(String name, int count, JrlShardRule<T> shardRule, JrlThreadPoolConfig config, JrlThreadPool pool) {
        this.name = name;
        this.count = count;
        this.shardRule = shardRule;
        this.config = config;
        this.pool = pool;
    }

    public DefaultJrlShardThreadExecutor(String name, int count, JrlShardRule<T> shardRule, JrlThreadPoolConfig config, JrlThreadPool pool, boolean preheat) {
        this.name = name;
        this.count = count;
        this.shardRule = shardRule;
        this.config = config;
        this.pool = pool;
        if (preheat) {
            for (int i = 0; i < count; i++) {
                int finalI = i;
                shardPools.computeIfAbsent(finalI, e -> JrlThreadPoolBuilder.builder(name + finalI, config).build());
            }
        }
    }

    @Override
    public void execute(T shardValue, JrlShardTask<T> task) {
        this.pool.execute(() -> {
            //分片
            final int shard = shardRule.shard(shardValue);
            /**
             * 将任务分发到多个线程池
             */
            final JrlThreadPool shardPool = shardPools.computeIfAbsent(shard, e -> JrlThreadPoolBuilder.builder(name + shard, config).build());
            //执行任务
            shardPool.execute(() -> task.run(shardPool.getName(), shard, shardValue));
        });
    }
}
