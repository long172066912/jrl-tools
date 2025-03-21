package org.jrl.tools.thread.core.factory.rejected;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 拒绝策略-丢弃老的并打日志
 *
 * @author JerryLong
 */
public class JrlDiscardOldestTaskRejected extends ThreadPoolExecutor.DiscardOldestPolicy {

    private static Logger LOGGER = LoggerFactory.getLogger(JrlDiscardOldestTaskRejected.class);

    /**
     * 线程池名称
     */
    private final String name;

    public JrlDiscardOldestTaskRejected(String name) {
        this.name = name;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        LOGGER.error("jrl-thread JrlDiscardOldestTaskRejected pool : {} , core : {} , max : {} , queue : {}",
                name, executor.getCorePoolSize(), executor.getMaximumPoolSize(), executor.getQueue().size());
        super.rejectedExecution(r, executor);
    }
}
