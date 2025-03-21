package org.jrl.tools.thread.core.factory;

import org.jrl.tools.thread.core.VariableLinkedBlockingQueue;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 动态队列
 *
 * @author JerryLong
 */
public class JrlDynamicQueue<E> extends VariableLinkedBlockingQueue<E> {
    /**
     * 是否动态增加队列容量
     */
    private boolean isDynamicUp = false;
    /**
     * 是否动态减少队列容量
     */
    private boolean isDynamicDown = false;
    /**
     * 最大容量
     */
    private final int maxCapacity = 100000;
    /**
     * 增加队列扩张因子
     */
    private final double upFactor = 0.75;
    /**
     * 减少队列收缩因子
     */
    private final double downFactor = 0.25;
    /**
     * 队列容量变化比例
     */
    private final int ratio = 2;
    /**
     * 是否是线程优先
     */
    private boolean threadPriority = false;
    /**
     * 线程池
     */
    private ThreadPoolExecutor poolExecutor;

    public JrlDynamicQueue(int capacity) {
        super(capacity);
    }

    /**
     * 动态变更队列容量
     *
     * @param capacity      容量
     * @param isDynamicUp   是否动态增加队列容量
     * @param isDynamicDown 是否动态减少队列容量
     */
    public JrlDynamicQueue(int capacity, boolean isDynamicUp, boolean isDynamicDown) {
        super(capacity);
        this.isDynamicUp = isDynamicUp;
        this.isDynamicDown = isDynamicDown;
    }

    /**
     * 动态变更队列容量
     *
     * @param capacity       容量
     * @param threadPriority 是否是线程优先
     * @param poolExecutor   线程池
     */
    private JrlDynamicQueue(int capacity, boolean threadPriority, ThreadPoolExecutor poolExecutor) {
        super(capacity);
        this.threadPriority = threadPriority;
        this.poolExecutor = poolExecutor;
    }

    /**
     * 动态变更队列容量
     *
     * @param capacity       容量
     * @param isDynamicUp    是否动态增加队列容量
     * @param isDynamicDown  是否动态减少队列容量
     * @param threadPriority 是否是线程优先
     * @param poolExecutor   线程池
     */
    private JrlDynamicQueue(int capacity, boolean isDynamicUp, boolean isDynamicDown, boolean threadPriority, ThreadPoolExecutor poolExecutor) {
        super(capacity);
        this.isDynamicUp = isDynamicUp;
        this.isDynamicDown = isDynamicDown;
        this.threadPriority = threadPriority;
        this.poolExecutor = poolExecutor;
    }

    /**
     * 动态变更队列容量
     */
    public void dynamicChange() {
        if (!isDynamicUp && !isDynamicDown) {
            return;
        }
        //计算当前队列容量
        final double i = (double) size() / getCapacity();
        if (i > upFactor) {
            if (isDynamicUp) {
                //扩容
                int newCapacity = getCapacity() * ratio;
                if (newCapacity < maxCapacity && newCapacity > 0) {
                    synchronized (this) {
                        setCapacity(newCapacity);
                    }
                }
            }
        } else if (i < downFactor) {
            if (isDynamicDown && getCapacity() > 0) {
                //缩容
                int newCapacity = getCapacity() / ratio;
                if (newCapacity > 0) {
                    synchronized (this) {
                        setCapacity(newCapacity);
                    }
                }
            }
        }
    }

    @Override
    public boolean offer(E o) {
        //线程优先
        if (threadPriority && null != poolExecutor) {
            //如果线程没满，线程优先
            if (poolExecutor.getActiveCount() < poolExecutor.getMaximumPoolSize() - 1) {
                return false;
            }
        }
        //动态变更队列容量
        dynamicChange();
        return super.offer(o);
    }

    /**
     * 设置是否是线程优先
     *
     * @param threadPriority 是否是线程优先
     */
    public void setThreadPriority(boolean threadPriority) {
        this.threadPriority = threadPriority;
    }

    public void setPoolExecutor(ThreadPoolExecutor poolExecutor) {
        this.poolExecutor = poolExecutor;
    }
}
