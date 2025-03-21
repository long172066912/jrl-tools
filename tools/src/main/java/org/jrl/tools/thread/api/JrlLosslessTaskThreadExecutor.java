package org.jrl.tools.thread.api;

import java.util.concurrent.Callable;

/**
 * 无损任务线程池执行器接口
 *
 * @author gaojianqun
 */
public interface JrlLosslessTaskThreadExecutor<T> {

    /**
     * 执行任务
     *
     * @param <V>      对象返回类型
     * @param function 任务
     */
    <V> void execute(LossLessTaskFunction<T, V> function);

    interface LossLessTaskFunction<T, V> extends Callable<V> {
        /**
         * 获取上下文
         *
         * @return T
         */
        T context();
    }

}
