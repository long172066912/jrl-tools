package org.jrl.tools.thread.core.stream;

import org.jrl.tools.thread.api.JrlThreadStream;
import org.jrl.tools.thread.api.task.JrlStreamWorker;
import org.jrl.tools.thread.core.factory.JrlThreadPool;

import java.util.concurrent.Callable;

/**
 * 默认线程编排执行器
 *
 * @author JerryLong
 */
public class DefaultJrlThreadStreamExecutor implements JrlThreadStream {

    private final JrlThreadPool pool;

    public DefaultJrlThreadStreamExecutor(JrlThreadPool pool) {
        this.pool = pool;
    }

    @Override
    public <T> JrlStreamWorker<T> execute(Callable<T> callable) {
        return new DefaultJrlStreamWorker<>(pool, callable);
    }

    @Override
    public <L, R, V> JrlStreamWorker<V> combine(JrlStreamWorker.BiJrlStreamRes<L, R> biJrlStreamRes, JrlStreamWorker.JrlStreamCombine<L, R, V> combineTask) {
        return biJrlStreamRes.getLeftWorker().combine(biJrlStreamRes.getRightWorker(), combineTask);
    }
}
