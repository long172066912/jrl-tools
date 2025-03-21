package org.jrl.tools.thread.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 实现线程Future，重写get方法，必须有超时时间
 *
 * @author JerryLong
 */
public class JrlFuture<T> extends CompletableFuture<T> {
    /**
     * 默认超时时间，2秒
     */
    public static final int TIMEOUT = 2;

    @Override
    public T get() throws InterruptedException, ExecutionException {
        try {
            return super.get(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
