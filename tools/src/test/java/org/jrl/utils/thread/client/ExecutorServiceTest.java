package org.jrl.utils.thread.client;

import org.jrl.tools.thread.JrlThreadUtil;
import org.jrl.tools.thread.core.factory.JrlDynamicQueue;
import org.jrl.tools.thread.core.factory.JrlThreadPoolConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池-优先线程测试
 * 基于JrlDynamicQueue动态队列实现
 *
 * @author JerryLong
 */
public class ExecutorServiceTest {
    @Test
    public void test() throws ExecutionException, InterruptedException {
        final ExecutorService executor = new ThreadPoolExecutor(10, 20, 30, TimeUnit.SECONDS, new JrlDynamicQueue(1024));
        final ExecutorService executor1 = JrlThreadUtil.newPool("testExecutorService", JrlThreadPoolConfig.builder().build());
        AtomicInteger i = new AtomicInteger();
        AtomicInteger i1 = new AtomicInteger();
        executor.execute(i::incrementAndGet);
        executor1.execute(i1::incrementAndGet);
        Thread.sleep(10L);
        Assertions.assertEquals(i.get(), 1);
        Assertions.assertEquals(executor.submit(() -> 1).get(), executor1.submit(() -> 1).get());
        executor.submit(i::incrementAndGet);
        executor1.submit(i1::incrementAndGet);
        Thread.sleep(10L);
        Assertions.assertEquals(i.get(), i1.get());
    }
}
