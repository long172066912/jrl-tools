package org.jrl.tools.thread.api.callback;

/**
 * 线程执行回调
 *
 * @author JerryLong
 */
public interface JrlThreadCallback<T> {

    /**
     * 成功
     *
     * @param result 任务执行返回结果
     */
    default void onSuccess(T result) {
    }

    /**
     * 失败
     *
     * @param throwable 异常
     */
    default void onError(Throwable throwable) {

    }
}
