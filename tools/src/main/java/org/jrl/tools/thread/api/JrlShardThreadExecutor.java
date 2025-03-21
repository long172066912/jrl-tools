package org.jrl.tools.thread.api;

/**
 * 分片线程执行器
 *
 * @author JerryLong
 */
@FunctionalInterface
public interface JrlShardThreadExecutor<V> {
    /**
     * 执行分片任务
     *
     * @param shardValue 分片值
     * @param task       任务
     */
    void execute(V shardValue, JrlShardTask<V> task);

    /**
     * 分配规则接口
     */
    @FunctionalInterface
    interface JrlShardRule<V> {
        /**
         * 分配规则
         *
         * @param shardValue 分片规则value
         * @return 分片数字
         */
        int shard(V shardValue);
    }

    @FunctionalInterface
    interface JrlShardTask<V> {
        /**
         * 执行分片任务
         *
         * @param poolName   线程池名称
         * @param shardValue 分片值
         * @param v          参数
         */
        void run(String poolName, int shardValue, V v);
    }
}
