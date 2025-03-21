package org.jrl.tools.thread.core.factory.rejected;

import org.jrl.tools.thread.core.exception.JrlThreadRetryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 拒绝策略-重试
 *
 * @author JerryLong
 */
public class JrlRetryRejected implements RejectedExecutionHandler {

    private static Logger LOGGER = LoggerFactory.getLogger(JrlRetryRejected.class);
    /**
     * 线程池名称
     */
    private final String name;
    /**
     * 等待时间
     */
    private int sleepTime = 1;
    /**
     * 最大重试次数
     */
    private int maxRetry = 3;

    public JrlRetryRejected(String poolName) {
        this.name = poolName;
    }

    public JrlRetryRejected(String poolName, int sleepTime) {
        this.name = poolName;
        this.sleepTime = sleepTime;
    }

    public JrlRetryRejected(String poolName, int sleepTime, int maxRetry) {
        this.name = poolName;
        this.sleepTime = sleepTime;
        this.maxRetry = maxRetry;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        //等待一下，重新执行
        LOGGER.warn("ResetRejectedExecutionHandler ! pool : {} , core : {} , max : {} , queue : {} , 触发拒绝策略，等待后将重新执行，请调整相关线程池配置 or 业务辑逻 !",
                name, executor.getCorePoolSize(), executor.getMaximumPoolSize(), executor.getQueue().size());

        try {
            Thread.sleep(sleepTime);
            if (r instanceof RetryRunnable) {
                ((RetryRunnable) r).retry(executor);
            } else {
                executor.execute(new RetryRunnable(maxRetry, r));
            }
        } catch (JrlThreadRetryException ex) {
            throw ex;
        } catch (Throwable e) {
            LOGGER.error("ResetRejectedExecutionHandler ! pool : {} , execute error !", name, e);
        }
    }

    class RetryRunnable implements Runnable {

        private int retryCount;
        private Runnable runnable;

        RetryRunnable(int retryCount, Runnable runnable) {
            this.retryCount = retryCount;
            this.runnable = runnable;
        }

        @Override
        public void run() {
            runnable.run();
        }

        public void retry(ThreadPoolExecutor executor) {
            retryCount--;
            if (retryCount <= 0) {
                //重试次数不够了，抛异常
                throw new JrlThreadRetryException("jrl-thread retry  " + maxRetry + " fail ! name:" + name);
            }
            executor.execute(this);
        }
    }
}
