package org.jrl.tools.thread.api;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池关闭处理
 *
 * @author JerryLong
 */
public interface JrlThreadShutdownHandler {
    /**
     * shutdown前执行，必须关注此逻辑的实现，防止阻碍shutdown
     *
     * @param executor 线程池
     */
    void onShutdown(ThreadPoolExecutor executor);

    /**
     * shutdown失败时执行，必须关注此逻辑的实现
     *
     * @param executor 线程池
     */
    void onFail(ThreadPoolExecutor executor);
}
