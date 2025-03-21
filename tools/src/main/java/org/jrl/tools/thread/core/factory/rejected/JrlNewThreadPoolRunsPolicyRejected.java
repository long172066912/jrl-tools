package org.jrl.tools.thread.core.factory.rejected;

import org.jrl.tools.thread.core.factory.JrlThreadPool;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 拒绝策略-使用新的线程池程执行
 *
 * @author JerryLong
 */
public class JrlNewThreadPoolRunsPolicyRejected implements RejectedExecutionHandler {
    /**
     * 新的线程池，作为兜底使用
     */
    private final JrlThreadPool pool;

    public JrlNewThreadPoolRunsPolicyRejected(JrlThreadPool pool) {
        this.pool = pool;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        try {
            pool.execute(r);
        } catch (Throwable e) {
            throw new RejectedExecutionException(
                    "Failed to start by new thread pool execute !", e);
        }
    }
}