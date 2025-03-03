package org.jrl.request;

public interface JrlAsyncRequest<V> {
    /**
     * 异步调用，并执行runnable
     *
     * @param runnable
     */
    void call(Runnable runnable);

    /**
     * 判断是否执行完成
     *
     * @return boolean
     */
    boolean isDone();

    /**
     * 结束异步调用处理
     */
    void end();
}
