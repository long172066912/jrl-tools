package org.jrl.tools.thread.api.task;

/**
 * 任务包装
 *
 * @author JerryLong
 */
public interface JrlTaskWrapper<T> extends Runnable {
    /**
     * 执行任务
     */
    @Override
    void run();

    /**
     * 获取任务本身
     *
     * @return T
     */
    T getTask();
}
