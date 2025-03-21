package org.jrl.tools.thread.core.factory.rejected;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 拒绝策略-丢弃并打日志
 *
 * @author JerryLong
 */
public class JrlDiscardRejected implements RejectedExecutionHandler {

    private static Logger LOGGER = LoggerFactory.getLogger(JrlDiscardRejected.class);

    /**
     * 线程池名称
     */
    private final String name;

    public JrlDiscardRejected(String name) {
        this.name = name;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        LOGGER.error("DiscardLogRejectedExecutionHandler pool : {} , core : {} , max : {} , queue : {}",
                name, executor.getCorePoolSize(), executor.getMaximumPoolSize(), executor.getQueue().size());
        throw new JrlThreadDiscardException();
    }

    /**
     * 拒绝策略专用异常
     */
    public class JrlThreadDiscardException extends RuntimeException {
    }
}
