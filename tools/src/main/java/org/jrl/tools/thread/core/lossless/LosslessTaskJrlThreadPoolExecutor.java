package org.jrl.tools.thread.core.lossless;

import io.micrometer.core.instrument.internal.TimedRunnable;
import org.jrl.tools.thread.api.JrlLosslessTaskThreadExecutor;
import org.jrl.tools.thread.api.JrlThreadShutdownHandler;
import org.jrl.tools.thread.api.task.JrlTaskWrapper;
import org.jrl.tools.thread.core.factory.JrlThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;

/**
 * 无损任务线程池执行器
 *
 * @author gaojianqun
 */
public class LosslessTaskJrlThreadPoolExecutor<T> implements JrlLosslessTaskThreadExecutor<T> {

    private static Logger LOGGER = LoggerFactory.getLogger(LosslessTaskJrlThreadPoolExecutor.class);

    private final JrlThreadPool pool;

    public LosslessTaskJrlThreadPoolExecutor(JrlThreadPool pool, Consumer<T> rollBackConsumer) {
        this.pool = pool;
        this.pool.setShutdownFailHandler(new JrlThreadShutdownHandler() {
            @Override
            public void onShutdown(ThreadPoolExecutor executor) {
            }

            @Override
            public void onFail(ThreadPoolExecutor executor) {
                BlockingQueue<Runnable> workQueue = executor.getQueue();
                LOGGER.error("jrl-thread LosslessTaskJrlThreadPoolExecutor shutdown fail ! lossless begin ! name : {} , queue size : {}", pool.getName(), workQueue.size());
                while (!workQueue.isEmpty()) {
                    final Runnable runnable = workQueue.poll();
                    JrlTaskWrapper<T> poll = null;
                    if (runnable instanceof TimedRunnable) {
                        poll = (JrlTaskWrapper<T>) ((TimedRunnable) runnable).getCommand();
                    } else {
                        poll = (JrlTaskWrapper<T>) runnable;
                    }
                    rollBackConsumer.accept(((LossLessTaskFunction<T, ?>) poll.getTask()).context());
                }
                LOGGER.error("jrl-thread LosslessTaskJrlThreadPoolExecutor shutdown fail ! lossless end ! name : {} , queue size : {}", pool.getName(), workQueue.size());
            }
        });
    }

    @Override
    public <V> void execute(LossLessTaskFunction<T, V> function) {
        pool.execute(function);
    }
}
