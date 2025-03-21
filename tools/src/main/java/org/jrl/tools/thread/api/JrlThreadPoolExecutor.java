package org.jrl.tools.thread.api;

import org.jrl.tools.thread.api.callback.JrlThreadCallback;
import org.jrl.tools.thread.core.JrlThreadResponse;
import org.jrl.tools.thread.core.factory.JrlThreadPool;
import org.jrl.tools.thread.core.factory.JrlThreadPoolConfig;
import org.jrl.tools.thread.core.lossless.LosslessTaskJrlThreadPoolExecutor;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 线程池接口
 * 执行任务、定时任务
 * 打包任务(多个任务，执行的时候判断是否到时间，到了直接抛弃)
 * 优先任务
 * 任务合并
 *
 * @author JerryLong
 */
public interface JrlThreadPoolExecutor {

    /**
     * 执行任务
     *
     * @param <T>  返回值类型
     * @param task 任务
     * @param pool 线程池
     * @return future
     */
    <T> Future<T> execute(JrlThreadPool pool, Callable<T> task);

    /**
     * 执行任务，无返回值
     *
     * @param task 任务
     * @param pool 线程池
     */
    void execute(JrlThreadPool pool, Runnable task);

    /**
     * 执行任务，无返回值
     *
     * @param task     任务
     * @param callback 回调
     * @param pool     线程池
     */
    void execute(JrlThreadPool pool, Runnable task, JrlThreadCallback callback);

    /**
     * 执行任务，延迟执行
     *
     * @param task     任务
     * @param delay    时间，毫秒
     * @param pool     线程池
     * @param timeUnit 时间类型
     */
    void execute(JrlThreadPool pool, Runnable task, int delay, TimeUnit timeUnit);

    /**
     * 执行任务，延迟执行
     *
     * @param pool         线程池
     * @param task         任务
     * @param delay        时间，毫秒
     * @param initialDelay 第一次初始化延迟时间
     * @param timeUnit     时间类型
     */
    void execute(JrlThreadPool pool, Runnable task, int initialDelay, int delay, TimeUnit timeUnit);

    /**
     * 执行批量任务
     *
     * @param pool    线程池
     * @param tasks   任务列表
     * @param timeout 超时时间，毫秒
     * @return 结果列表，如果有任务执行失败，返回null
     */
    List<JrlThreadResponse> execute(JrlThreadPool pool, int timeout, List<Callable<Object>> tasks);

    /**
     * 执行批量优先任务
     * 只能保证这一批任务的优先性，不能保证所有任务的优先性
     *
     * @param pool    线程池
     * @param timeout 超时时间，毫秒
     * @return 优先级线程执行器
     */
    JrlPriorityThreadExecutor priorityTask(JrlThreadPool pool, int timeout);

    /**
     * 执行兜底回调任务
     * 只执行线程池队列中没有执行完的任务
     *
     * @param pool             线程池
     * @param fallbackConsumer 任务shutdown失败时的处理接口，可以是发送到mq、发送到日志等
     * @param <T>              参数类型
     * @return 兜底线程执行器
     */
    <T> LosslessTaskJrlThreadPoolExecutor<T> losslessTask(JrlThreadPool pool, Consumer<T> fallbackConsumer);

    /**
     * 执行shard任务
     *
     * @param pool      线程池
     * @param name      shard任务名
     * @param count     shard数量
     * @param shardRule shard规则
     * @param <T>       泛型
     * @return 分片任务执行器
     */
    <T> JrlShardThreadExecutor<T> shard(JrlThreadPool pool, String name, int count, JrlShardThreadExecutor.JrlShardRule<T> shardRule);

    /**
     * 执行shard任务
     *
     * @param pool      线程池
     * @param name      shard任务名
     * @param count     shard数量
     * @param shardRule shard规则
     * @param config    自定义处理线程池配置
     * @param <T>       泛型类型
     * @return 分片任务执行器
     */
    <T> JrlShardThreadExecutor<T> shard(JrlThreadPool pool, String name, int count, JrlShardThreadExecutor.JrlShardRule<T> shardRule, JrlThreadPoolConfig config);

    /**
     * 执行shard任务
     *
     * @param pool      线程池
     * @param name      shard任务名
     * @param count     shard数量
     * @param shardRule shard规则
     * @param config    自定义处理线程池配置
     * @param preheat   是否预热
     * @param <T>       泛型类型
     * @return 分片任务执行器
     */
    <T> JrlShardThreadExecutor<T> shard(JrlThreadPool pool, String name, int count, JrlShardThreadExecutor.JrlShardRule<T> shardRule, JrlThreadPoolConfig config, boolean preheat);

    /**
     * 执行合并任务
     *
     * @param pool     线程池
     * @param name     合并任务名称
     * @param rule     合并规则
     * @param consumer 消费逻辑
     * @param <T>      任务对象类型
     * @return 合并任务执行器
     */
    <T> JrlMergeThreadExecutor<T> merge(JrlThreadPool pool, String name, JrlMergeThreadExecutor.JrlMergeRule<T> rule, JrlMergeThreadExecutor.JrlMergeConsumer<T> consumer);

    /**
     * 获取编排stream
     *
     * @param pool 线程池
     * @return {@link JrlThreadStream}
     */
    JrlThreadStream stream(JrlThreadPool pool);
}
