package org.jrl.tools.thread.api.task;

import org.jrl.tools.utils.function.JrlRunnable;

import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * 线程编排工作单元
 *
 * @author JerryLong
 */
public interface JrlStreamWorker<T> extends Future<T> {
    /**
     * 执行完成结果传入执行第二个任务
     *
     * @param task 任务
     */
    void thenRun(JrlRunnable<T> task);

    /**
     * 将执行结果传入批量任务
     *
     * @param tasks 任务列表
     */
    void thenRun(BaJrlStreamRunnable<T> tasks);

    /**
     * 执行完成结果传入执行第二个任务
     *
     * @param task 任务
     * @param <R> 返回类型
     * @return T 返回对象
     */
    <R> JrlStreamWorker<R> thenSupply(Function<T, R> task);

    /**
     * 执行完成后执行批量任务，返回合并结果集处理结果
     *
     * @param task 任务
     * @param <R> 返回类型
     * @return T 返回对象
     */
    <R> JrlStreamWorker<List<R>> thenSupply(BaJrlStreamWorker<T, R> task);

    /**
     * 执行完成后，执行分叉任务
     *
     * @param task 任务
     * @param <R> 第一个参数返回类型
     * @param <V> 第二个参数返回类型
     * @return BiJrlStreamRes
     */
    <R, V> BiJrlStreamRes<R, V> thenSupply(BiJrlStreamCallable<T, R, V> task);

    /**
     * 任务流合并
     *
     * @param combineTask 合并任务处理
     * @param r           另一个参数
     * @param <R>         第二个参数类型
     * @param <V>         返回值
     * @return JrlStreamWorker
     */
    <R, V> JrlStreamWorker<V> combine(JrlStreamWorker<R> r, JrlStreamCombine<T, R, V> combineTask);

    @FunctionalInterface
    interface JrlStreamCombine<L, R, V> {
        /**
         * 执行任务
         *
         * @param l 第一个参数
         * @param r 第二个参数
         * @return V 返回值
         */
        V run(L l, R r);
    }

    /**
     * 批量任务，无返回
     */
    class BaJrlStreamRunnable<T> {
        private final List<JrlRunnable<T>> jrlRunnables;

        public BaJrlStreamRunnable(List<JrlRunnable<T>> workers) {
            this.jrlRunnables = workers;
        }

        public List<JrlRunnable<T>> getRunnables() {
            return jrlRunnables;
        }
    }

    /**
     * 批量任务，有返回值
     */
    class BaJrlStreamWorker<T, R> {
        private final List<Function<T, R>> workers;

        public BaJrlStreamWorker(List<Function<T, R>> workers) {
            this.workers = workers;
        }

        public List<Function<T, R>> getWorkers() {
            return workers;
        }
    }

    /**
     * 分叉任务
     */
    class BiJrlStreamCallable<T, R, V> {
        private final Function<T, R> left;
        private final Function<T, V> right;

        public BiJrlStreamCallable(Function<T, R> left, Function<T, V> right) {
            this.left = left;
            this.right = right;
        }

        public Function<T, R> getLeft() {
            return left;
        }

        public Function<T, V> getRight() {
            return right;
        }
    }

    class BiJrlStreamRes<R, V> {
        private final JrlStreamWorker<R> leftWorker;
        private final JrlStreamWorker<V> rightWorker;

        public BiJrlStreamRes(JrlStreamWorker<R> leftWorker, JrlStreamWorker<V> rightWorker) {
            this.leftWorker = leftWorker;
            this.rightWorker = rightWorker;
        }

        public JrlStreamWorker<R> getLeftWorker() {
            return leftWorker;
        }

        public JrlStreamWorker<V> getRightWorker() {
            return rightWorker;
        }
    }
}
