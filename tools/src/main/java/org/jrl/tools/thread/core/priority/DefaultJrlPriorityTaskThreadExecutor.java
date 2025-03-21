package org.jrl.tools.thread.core.priority;

import org.jrl.tools.thread.api.JrlPriorityThreadExecutor;
import org.jrl.tools.thread.core.JrlThreadResponse;
import org.jrl.tools.thread.core.factory.JrlThreadPool;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * 优先级任务执行器
 * 只能保证一批任务的优先性，不能保证所有任务的优先性
 *
 * @author JerryLong
 */
public class DefaultJrlPriorityTaskThreadExecutor implements JrlPriorityThreadExecutor {

    private final int timeout;
    private final JrlThreadPool pool;
    /**
     * 优先级队列，从小到大排列，优先获取最小即最优先的任务
     */
    private final PriorityQueue<JrlPriorityTask<Object>> workQueue = new PriorityQueue<>(1000, Comparator.comparingInt(JrlPriorityTask::getPriority));

    public DefaultJrlPriorityTaskThreadExecutor(int timeout, JrlThreadPool pool) {
        this.timeout = timeout;
        this.pool = pool;
    }

    @Override
    public JrlPriorityThreadExecutor add(int priority, Callable<Object> task) {
        workQueue.add(new JrlPriorityTask<>(task, priority));
        return this;
    }

    @Override
    public List<JrlThreadResponse> execute() {
        final int size = workQueue.size();
        List<Future<Object>> futures = new ArrayList<>(size);
        while (workQueue.size() > 0) {
            futures.add(this.pool.execute(workQueue.poll()));
        }
        return pool.getFutureResult(this.timeout, futures);
    }

    /**
     * 优先级任务
     */
    private static class JrlPriorityTask<T> implements Callable<T> {
        private final Callable<T> task;
        private final int priority;

        public JrlPriorityTask(Callable<T> task, int priority) {
            this.task = task;
            this.priority = priority;
        }

        @Override
        public T call() throws Exception {
            return task.call();
        }

        public int getPriority() {
            return priority;
        }
    }
}
