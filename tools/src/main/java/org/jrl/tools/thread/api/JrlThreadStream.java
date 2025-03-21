package org.jrl.tools.thread.api;

import org.jrl.tools.thread.api.task.JrlStreamWorker;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * 线程流程编排，未实现
 * 请使用{@link CompletableFuture}
 *
 * @author JerryLong
 */
public interface JrlThreadStream {
    /**
     * 执行
     *
     * @param callable 任务
     * @param <T>      返回类型
     * @return this
     */
    <T> JrlStreamWorker<T> execute(Callable<T> callable);

    /**
     * 合并任务
     *
     * @param biJrlStreamRes 任务
     * @param combineTask     合并逻辑
     * @param <L>             第一个任务泛型类型
     * @param <R>             第二个任务泛型类型
     * @param <V>             返回值泛型类型
     * @return JrlStreamWorker
     */
    <L, R, V> JrlStreamWorker<V> combine(JrlStreamWorker.BiJrlStreamRes<L, R> biJrlStreamRes, JrlStreamWorker.JrlStreamCombine<L, R, V> combineTask);
}
