package org.jrl.tools.thread.api;

import org.jrl.tools.thread.core.JrlThreadResponse;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * 优先级线程
 *
 * @author JerryLong
 */
public interface JrlPriorityThreadExecutor {
    /**
     * 添加优先级批量任务
     *
     * @param priority 优先级
     * @param task     任务
     * @return this
     */
    JrlPriorityThreadExecutor add(int priority, Callable<Object> task);

    /**
     * 执行
     *
     * @return 结果集
     */
    List<JrlThreadResponse> execute();
}
