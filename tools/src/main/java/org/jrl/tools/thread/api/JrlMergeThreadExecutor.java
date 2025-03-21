package org.jrl.tools.thread.api;

import java.util.List;
import java.util.function.Consumer;

/**
 * 合并任务执行器
 *
 * @author JerryLong
 */
public interface JrlMergeThreadExecutor<T> {
    /**
     * 合并任务
     *
     * @param task 任务信息
     * @return 返回合并后的任务
     */
    JrlMergeThreadExecutor<T> join(T task);

    /**
     * 获取当前未处理任务数量
     *
     * @return 数量
     */
    int size();

    /**
     * 分配规则接口
     */
    interface JrlMergeRule<T> {
        /**
         * 时间条件
         *
         * @param time 时间，毫秒
         * @return JrlMergeRule
         */
        JrlMergeRule<T> onTime(int time);

        /**
         * 次数条件
         *
         * @param count 次数
         * @return JrlMergeRule
         */
        JrlMergeRule<T> onCount(int count);

        /**
         * 如果队列满了
         * @param consumer 消费者
         * @return JrlMergeRule
         */
        JrlMergeRule<T> onQueueFull(Consumer<T> consumer);

        /**
         * 队列长度，默认10000
         *
         * @param length 队列长度
         */
        void queueLength(int length);

        default int getTime() {
            return 100;
        }

        default int getCount() {
            return 100;
        }

        default int getQueueLength() {
            return 10000;
        }

        default Consumer<T> getQueueFullConsumer() {
            return null;
        }
    }

    /**
     * 真正执行的任务接口
     */
    @FunctionalInterface
    interface JrlMergeConsumer<T> {
        /**
         * 执行
         *
         * @param value 值
         */
        void execute(List<T> value);
    }
}
