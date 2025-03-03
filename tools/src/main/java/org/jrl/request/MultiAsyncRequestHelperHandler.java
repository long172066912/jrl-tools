package org.jrl.request;

import org.apache.commons.collections4.CollectionUtils;
import org.jrl.request.impl.HttpAsyncRequestHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * 批量调用处理
 *
 * @author JerryLong
 */
public class MultiAsyncRequestHelperHandler {
    private final int timeout;
    private final List<JrlAsyncRequest<?>> asyncRequests;
    private final AtomicBoolean isDone = new AtomicBoolean(false);
    private final long startTime = System.currentTimeMillis();
    private final AtomicInteger awaitTime = new AtomicInteger(4);
    private static final int MAX_SLEEP_TIME = 128;
    private final Thread currentThread = Thread.currentThread();

    protected MultiAsyncRequestHelperHandler(int timeout, List<JrlAsyncRequest<?>> asyncRequests) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("timeout must > 0");
        }
        if (CollectionUtils.isEmpty(asyncRequests)) {
            throw new IllegalArgumentException("jrlRpcRequestList or futureFlowHandlers must not empty");
        }
        this.timeout = timeout;
        this.asyncRequests = asyncRequests;
    }

    /**
     * 发起请求
     */
    public void request() {
        AtomicInteger taskCount = new AtomicInteger(asyncRequests.size());
        while (!isDone.get() && System.currentTimeMillis() - startTime <= timeout) {
            //如果任务都完成了，直接返回
            if (taskCount.get() == 0) {
                break;
            }
            //检测回调是否完成，如果没完成，再睡眠一会
            for (JrlAsyncRequest<?> asyncRequest : asyncRequests) {
                asyncRequest.call(() -> {
                    if (asyncRequest.isDone() && taskCount.decrementAndGet() == 0) {
                        isDone.set(true);
                        //唤醒调用线程
                        LockSupport.unpark(currentThread);
                    }
                });
            }
            //如果任务都完成了，直接返回
            if (taskCount.get() == 0) {
                break;
            }
            final long awaitTime = getAwaitTime();
            if (!isDone.get() && awaitTime > 0) {
                //睡眠一会
                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(awaitTime));
                if (Thread.interrupted()) {
                    isDone.set(true);
                    break;
                }
            }
        }
        isDone.set(true);
    }

    private long getAwaitTime() {
        final long diffTime = System.currentTimeMillis() - startTime;
        if (diffTime >= timeout) {
            return 0;
        }
        int sleepTime = this.awaitTime.get();
        if (diffTime <= sleepTime) {
            return diffTime;
        }
        sleepTime = this.awaitTime.addAndGet(sleepTime);
        if (sleepTime < MAX_SLEEP_TIME) {
            return sleepTime;
        }
        return MAX_SLEEP_TIME;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int timeout;
        private List<HttpAsyncRequestHelper<?>> futureFlowHandlers;

        public Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder http(List<HttpAsyncRequestHelper<?>> futureFlowHandlers) {
            this.futureFlowHandlers = futureFlowHandlers;
            return this;
        }

        public MultiAsyncRequestHelperHandler build() {
            List<JrlAsyncRequest<?>> list = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(futureFlowHandlers)) {
                list.addAll(futureFlowHandlers);
            }
            return new MultiAsyncRequestHelperHandler(timeout, list);
        }

        public void request() {
            build().request();
        }
    }
}


