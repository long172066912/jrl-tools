package org.jrl.tools.utils.function;

/**
 * 执行任务，有传入值
 *
 * @author JerryLong
 */
@FunctionalInterface
public interface JrlRunnable<T> {
    /**
     * 执行任务
     *
     * @param t 任务参数
     */
    void run(T t);
}
