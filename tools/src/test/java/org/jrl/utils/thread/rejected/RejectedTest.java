package org.jrl.utils.thread.rejected;

import org.jrl.tools.thread.JrlThreadUtil;
import org.jrl.tools.thread.core.exception.JrlThreadRetryException;
import org.jrl.tools.thread.core.factory.JrlThreadPool;
import org.jrl.tools.thread.core.factory.JrlThreadPoolBuilder;
import org.jrl.tools.thread.core.factory.JrlThreadPoolConfig;
import org.jrl.tools.thread.core.factory.rejected.JrlDiscardRejected;
import org.jrl.tools.thread.core.factory.rejected.JrlNewThreadPoolRunsPolicyRejected;
import org.jrl.tools.thread.core.factory.rejected.JrlRetryRejected;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

class RejectedTest {

    @Test
    public void retryTest() throws ExecutionException, InterruptedException {
        String name = "testRetry";
        final JrlThreadPoolConfig config = JrlThreadPoolConfig.builder()
                .corePoolSize(1)
                .maxPoolSize(1)
                .workQueue((i) -> new SynchronousQueue<>())
                .rejectedExecutionHandler(() -> new JrlRetryRejected(name))
                .build();
        final JrlThreadPool pool = JrlThreadPoolBuilder.builder(name, config).build();
        final Future<Integer> execute = pool.execute(() -> {
            try {
                Thread.sleep(8L);
            } catch (InterruptedException e) {
            }
            return 1;
        });
        Thread.sleep(5);
        final Future<Integer> execute1 = pool.execute(() -> 2);
        Assertions.assertEquals(execute.get(), 1);
        Assertions.assertEquals(execute1.get(), 2);
        //再试一次
        final Future<Integer> execute2 = pool.execute(() -> {
            try {
                Thread.sleep(8L);
            } catch (InterruptedException e) {
            }
            return 1;
        });
        Thread.sleep(5);
        final Future<Integer> execute3 = pool.execute(() -> 2);
        Assertions.assertEquals(execute2.get(), 1);
        Assertions.assertEquals(execute3.get(), 2);
    }

    @Test
    public void retryMaxTest() throws ExecutionException, InterruptedException, TimeoutException {
        String name = "testRetry";
        final JrlThreadPoolConfig config = JrlThreadPoolConfig.builder()
                .corePoolSize(1)
                .maxPoolSize(1)
                .workQueue((i) -> new SynchronousQueue<>())
                .rejectedExecutionHandler(() -> new JrlRetryRejected(name))
                .build();
        final JrlThreadPool pool = JrlThreadPoolBuilder.builder(name, config).build();
        pool.execute(() -> {
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
            }
            System.out.println("a执行完成");
        });
        Thread.sleep(5);
        Assertions.assertThrows(JrlThreadRetryException.class, () -> pool.execute(() -> 2));
        Assertions.assertThrows(JrlThreadRetryException.class, () -> pool.execute(() -> System.out.println(111)));
        Thread.sleep(50L);
        Assertions.assertEquals(2, pool.execute(() -> 2).get(10, TimeUnit.MILLISECONDS));
    }

    @Test
    public void jdkDiscardTest() throws ExecutionException, InterruptedException {
        String name = "testDiscard";
        final JrlThreadPoolConfig config = JrlThreadPoolConfig.builder()
                .corePoolSize(1)
                .maxPoolSize(1)
                .workQueue((i) -> new SynchronousQueue<>())
                .rejectedExecutionHandler(ThreadPoolExecutor.DiscardPolicy::new)
                .build();
        final JrlThreadPool pool = JrlThreadPoolBuilder.builder(name, config).build();
        final Future<Integer> execute = pool.execute(() -> {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
            }
            return 1;
        });
        Thread.sleep(5);
        final Future<Integer> execute1 = pool.execute(() -> 2);
        Assertions.assertEquals(execute.get(), 1);
        Assertions.assertThrows(TimeoutException.class, () -> execute1.get(10, TimeUnit.MILLISECONDS));
    }

    @Test
    public void discardTest() throws ExecutionException, InterruptedException {
        String name = "testDiscard";
        final JrlThreadPoolConfig config = JrlThreadPoolConfig.builder()
                .corePoolSize(1)
                .maxPoolSize(1)
                .workQueue((i) -> new SynchronousQueue<>())
                .rejectedExecutionHandler(() -> new JrlDiscardRejected(name))
                .build();
        final JrlThreadPool pool = JrlThreadPoolBuilder.builder(name, config).build();
        final Future<Integer> execute = pool.execute(() -> {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
            }
            return 1;
        });
        Thread.sleep(5);
        final Future<Integer> execute1 = pool.execute(() -> 2);
        Assertions.assertEquals(execute.get(), 1);
        Assertions.assertThrows(Exception.class, execute1::get);
    }

    @Test
    public void newThreadPoolRunsPolicyRejectedTest() throws InterruptedException, ExecutionException {
        final JrlThreadPool pool = JrlThreadPoolBuilder.builder("testNewPoolRuns", JrlThreadPoolConfig.builder()
                .corePoolSize(1)
                .maxPoolSize(1)
                .workQueue((i) -> new SynchronousQueue<>())
                .rejectedExecutionHandler(() -> new JrlNewThreadPoolRunsPolicyRejected(JrlThreadUtil.getDefaultPool()))
                .build()).build();
        final Future<Integer> execute = pool.execute(() -> {
            try {
                Thread.sleep(8L);
            } catch (InterruptedException e) {
            }
            return 1;
        });
        final Future<Integer> execute1 = pool.execute(() -> 2);
        Assertions.assertEquals(execute.get(), 1);
        Assertions.assertEquals(execute1.get(), 2);
    }
}