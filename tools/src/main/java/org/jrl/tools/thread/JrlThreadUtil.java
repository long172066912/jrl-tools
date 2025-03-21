package org.jrl.tools.thread;

import org.jrl.tools.thread.api.JrlMergeThreadExecutor;
import org.jrl.tools.thread.api.JrlPriorityThreadExecutor;
import org.jrl.tools.thread.api.JrlShardThreadExecutor;
import org.jrl.tools.thread.api.JrlThreadStream;
import org.jrl.tools.thread.api.callback.JrlThreadCallback;
import org.jrl.tools.thread.core.DefaultJrlThreadPoolExecutor;
import org.jrl.tools.thread.core.JrlThreadResponse;
import org.jrl.tools.thread.core.factory.JrlThreadPool;
import org.jrl.tools.thread.core.factory.JrlThreadPoolBuilder;
import org.jrl.tools.thread.core.factory.JrlThreadPoolConfig;
import org.jrl.tools.thread.core.factory.pool.JrlPoolScheduleExecutor;
import org.jrl.tools.thread.core.lossless.LosslessTaskJrlThreadPoolExecutor;
import org.jrl.tools.thread.core.merge.JrlMergeRuleBuilder;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * 线程工具
 * 执行任务、定时任务
 * 打包任务(多个任务，执行的时候判断是否到时间，到了直接抛弃)
 * 优先任务
 *
 * @author JerryLong
 */
public class JrlThreadUtil {

    /**
     * 执行任务
     *
     * @param <T>  返回值类型
     * @param task 任务
     * @param pool 线程池
     * @return 执行结果
     */
    public static <T> Future<T> execute(Callable<T> task, JrlThreadPool pool) {
        return DefaultJrlThreadPoolExecutor.getInstance().execute(pool, task);
    }

    /**
     * 执行任务，无返回值
     *
     * @param task 任务
     * @param pool 线程池
     */
    public static void execute(Runnable task, JrlThreadPool pool) {
        DefaultJrlThreadPoolExecutor.getInstance().execute(pool, task);
    }

    /**
     * 执行任务，无返回值
     *
     * @param task     任务
     * @param callback 回调
     * @param pool     线程池
     * @param <T>      回调对象类型
     */
    public static <T> void execute(Runnable task, JrlThreadCallback<T> callback, JrlThreadPool pool) {
        DefaultJrlThreadPoolExecutor.getInstance().execute(pool, task, callback);
    }

    /**
     * 执行任务，延迟执行
     *
     * @param task     任务
     * @param delay    时间，毫秒
     * @param timeUnit 时间单位
     * @param pool     线程池
     */
    public static void schedule(Runnable task, int delay, TimeUnit timeUnit, JrlThreadPool pool) {
        DefaultJrlThreadPoolExecutor.getInstance().execute(pool, task, delay, timeUnit);
    }

    /**
     * 执行任务，延迟执行
     *
     * @param task         任务
     * @param delay        时间，毫秒
     * @param initialDelay 第一次初始化延迟时间，毫秒
     * @param timeUnit     时间单位
     * @param pool         线程池
     */
    public static void schedule(Runnable task, int delay, int initialDelay, TimeUnit timeUnit, JrlThreadPool pool) {
        if (!pool.isSchedule()) {
            throw new RuntimeException("pool is not schedule pool ! " + pool.getName());
        }
        DefaultJrlThreadPoolExecutor.getInstance().execute(pool, task, delay, initialDelay, timeUnit);
    }

    /**
     * 执行批量任务
     *
     * @param tasks   任务列表
     * @param timeout 超时时间，毫秒
     * @param pool    线程池
     * @return 结果列表，如果有任务执行失败，返回null
     */
    public static List<JrlThreadResponse> executeTasks(int timeout, List<Callable<Object>> tasks, JrlThreadPool pool) {
        return DefaultJrlThreadPoolExecutor.getInstance().execute(pool, timeout, tasks);
    }

    /**
     * 执行批量优先任务
     * 只能保证这一批任务的优先性，不能保证所有任务的优先性
     *
     * @param timeout 超时时间，毫秒
     * @return 优先任务执行器
     */
    private static JrlPriorityThreadExecutor priorityTask(int timeout, JrlThreadPool pool) {
        return DefaultJrlThreadPoolExecutor.getInstance().priorityTask(pool, timeout);
    }

    /**
     * 构建无损任务执行器
     *
     * @param fallbackConsumer 任务shutdown失败时的处理接口，可以是发送到mq、发送到日志等
     * @param <T>              参数类型
     * @param pool             线程池
     * @return 队列中待执行任务的执行器
     */
    public static <T> LosslessTaskJrlThreadPoolExecutor<T> losslessTask(Consumer<T> fallbackConsumer, JrlThreadPool pool) {
        return DefaultJrlThreadPoolExecutor.getInstance().losslessTask(pool, fallbackConsumer);
    }

    /**
     * 执行shard任务
     *
     * @param name      shard任务名
     * @param count     shard数量
     * @param shardRule shard规则
     * @param pool      线程池
     * @param <T>       泛型类型
     * @return 分片执行器
     */
    public static <T> JrlShardThreadExecutor<T> shard(String name, int count, JrlShardThreadExecutor.JrlShardRule<T> shardRule, JrlThreadPool pool) {
        return DefaultJrlThreadPoolExecutor.getInstance().shard(pool, name, count, shardRule);
    }

    /**
     * 执行shard任务
     *
     * @param name      shard任务名
     * @param count     shard数量
     * @param shardRule shard规则
     * @param config    自定义处理线程池配置
     * @param pool      线程池
     * @param <T>       泛型类型
     * @return 分片执行器
     */
    public static <T> JrlShardThreadExecutor<T> shard(String name, int count, JrlShardThreadExecutor.JrlShardRule<T> shardRule, JrlThreadPoolConfig config, JrlThreadPool pool) {
        return DefaultJrlThreadPoolExecutor.getInstance().shard(pool, name, count, shardRule, config);
    }

    /**
     * 执行shard任务，预热提前创建好count数量的线程池
     *
     * @param name      shard任务名
     * @param count     shard数量
     * @param shardRule shard规则
     * @param config    自定义处理线程池配置
     * @param pool      线程池
     * @param <T>       泛型类型
     * @return 分片执行器
     */
    public static <T> JrlShardThreadExecutor<T> shardAndPreheat(String name, int count, JrlShardThreadExecutor.JrlShardRule<T> shardRule, JrlThreadPoolConfig config, JrlThreadPool pool) {
        return DefaultJrlThreadPoolExecutor.getInstance().shard(pool, name, count, shardRule, config, true);
    }

    /**
     * 执行合并任务
     *
     * @param name     合并任务名称
     * @param rule     合并规则
     * @param consumer 消费逻辑
     * @param pool     线程池
     * @param <T>      任务对象类型
     * @return 合并任务执行器
     */
    public static <T> JrlMergeThreadExecutor<T> merge(String name, JrlMergeThreadExecutor.JrlMergeRule<T> rule, JrlMergeThreadExecutor.JrlMergeConsumer<T> consumer, JrlThreadPool pool) {
        return DefaultJrlThreadPoolExecutor.getInstance().merge(pool, name, rule, consumer);
    }

    /**
     * 执行合并任务
     *
     * @param name     合并任务名称
     * @param consumer 消费逻辑
     * @param pool     线程池
     * @param <T>      任务对象类型
     * @return 合并任务执行器
     */
    public static <T> JrlMergeThreadExecutor<T> merge(String name, JrlMergeThreadExecutor.JrlMergeConsumer<T> consumer, JrlThreadPool pool) {
        return DefaultJrlThreadPoolExecutor.getInstance().merge(pool, name, JrlMergeRuleBuilder.<T>builder().onCount(100).onTime(100).build(), consumer);
    }

    /**
     * 构建线程流
     *
     * @return {@link JrlThreadStream}
     */
    public static JrlThreadStream stream(JrlThreadPool pool) {
        return DefaultJrlThreadPoolExecutor.getInstance().stream(pool);
    }

    /**
     * 构建新的线程池，使用默认线程池配置
     *
     * @param name 线程池名称
     * @param <T>  具体实现
     * @return 线程池构建器
     */
    public static <T extends ExecutorService> T newPool(String name) {
        return JrlThreadPoolBuilder.builder(name).build();
    }

    /**
     * 构建新的线程池
     *
     * @param name   线程池名称
     * @param config 自定义配置
     * @param <T>    具体实现
     * @return 线程池构建器
     */
    public static <T extends ExecutorService> T newPool(String name, JrlThreadPoolConfig config) {
        return JrlThreadPoolBuilder.builder(name, config).build();
    }

    /**
     * 构建新的定时任务线程池
     *
     * @param name   线程池名称
     * @param config 自定义配置
     * @param <T>    具体实现
     * @return 线程池构建器
     */
    public static <T extends ScheduledExecutorService> T newSchedulePool(String name, JrlThreadPoolConfig config) {
        config.toBuilder().schedule();
        return JrlThreadPoolBuilder.builder(name, config).build();
    }

    public static <T extends ScheduledExecutorService> T newSchedulePool(String name, int corePoolSize) {
        return JrlThreadPoolBuilder.builder(name, JrlThreadPoolConfig.builder().corePoolSize(corePoolSize).schedule().build()).build();
    }

    /**
     * 提取 CompletableFuture 异常
     *
     * @param throwable 异常
     * @return 异常
     */
    public static Throwable extractCompletableFutureRealException(Throwable throwable) {
        //这里判断异常类型是否为CompletionException、ExecutionException，如果是则进行提取，否则直接返回。
        if (throwable instanceof CompletionException || throwable instanceof ExecutionException) {
            if (throwable.getCause() != null) {
                return throwable.getCause();
            }
        }
        return throwable;
    }

    /**
     * 进程内全局使用一个默认线程池
     */
    private static volatile JrlThreadPool defaultPool = null;
    /**
     * 进程内全局使用一个定时任务默认线程池
     */
    private static volatile JrlThreadPool defaultSchedulePool = null;


    /**
     * 获取线程池，如果ThreadLocal中没拿到，则使用全局默认线程池
     *
     * @return JrlThreadPool
     */
    public static JrlThreadPool getDefaultPool() {
        if (null == defaultPool) {
            synchronized (JrlThreadUtil.class) {
                if (null == defaultPool) {
                    final String name = "jrl-default-pool";
                    /**
                     * 创建默认线程池
                     */
                    defaultPool = JrlThreadPoolBuilder
                            .builder(name, JrlThreadPoolConfig.builder()
                                    .corePoolSize(20)
                                    .maxPoolSize(100)
                                    .build())
                            .build();
                }
            }
        }
        return defaultPool;
    }

    /**
     * 获取定时任务线程池，如果ThreadLocal中没拿到，则使用全局默认线程池
     *
     * @return JrlThreadPool
     */
    public static JrlThreadPool getDefaultSchedulePool() {
        if (null == defaultSchedulePool) {
            synchronized (JrlThreadUtil.class) {
                if (null == defaultSchedulePool) {
                    final String name = "jrl-default-schedule-pool";
                    /**
                     * 创建默认线程池
                     */
                    defaultSchedulePool = JrlThreadPoolBuilder
                            .builder(name, JrlThreadPoolConfig.builder().corePoolSize(5).schedule().build())
                            .build();
                }
            }
        }
        return defaultSchedulePool;
    }

    /**
     * 延迟执行任务
     *
     * @param delay 延迟时间
     * @param unit  时间单位
     * @param task  任务
     */
    public static void executeDelay(long delay, TimeUnit unit, Runnable task) {
        ((JrlPoolScheduleExecutor) getDefaultSchedulePool()).schedule(task, delay, unit);
    }

    /**
     * 延迟执行任务
     *
     * @param delay 延迟时间
     * @param unit  时间单位
     * @param task  任务
     */
    public static void executeDelay(long delay, TimeUnit unit, Runnable task, ScheduledExecutorService scheduledExecutorService) {
        scheduledExecutorService.schedule(task, delay, unit);
    }
}
