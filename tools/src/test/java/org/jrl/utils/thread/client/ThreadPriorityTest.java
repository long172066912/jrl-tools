package org.jrl.utils.thread.client;

import org.jrl.tools.thread.JrlThreadUtil;
import org.jrl.tools.thread.core.factory.JrlDynamicQueue;
import org.jrl.tools.thread.core.factory.JrlThreadPool;
import org.jrl.tools.thread.core.factory.JrlThreadPoolConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 线程池-优先线程测试
 * 基于JrlDynamicQueue动态队列实现
 *
 * @author JerryLong
 */
public class ThreadPriorityTest {
    @Test
    public void test() throws ExecutionException, InterruptedException {
        JrlDynamicQueue<Runnable> queue = new JrlDynamicQueue<>(1024);
        final JrlThreadPool pool = JrlThreadUtil.newPool("threadPriority", JrlThreadPoolConfig.builder()
                .corePoolSize(1)
                .maxPoolSize(3)
                .workQueue((size) -> queue)
                .threadPriority().build());
        final Future<Integer> future = pool.execute(() -> {
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
            }
            return 1;
        });
        final Future<Integer> future1 = pool.execute(() -> 2);
        Assertions.assertEquals(0, queue.size());
        Assertions.assertEquals(2, future1.get());
        //第二轮测试，锁住2个线程，队列里应该有10个任务
        final Future<Integer> future2 = pool.execute(() -> {
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
            }
            return 3;
        });
        for (int i = 0; i < 10; i++) {
            pool.execute(() -> {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        //有一个主线程执行
        Assertions.assertEquals(12, queue.size() + pool.getActiveThreadSize());
//        System.out.println(pool.getActiveThreadSize());
        Assertions.assertEquals(3, future2.get());
        Assertions.assertEquals(1, future.get());
    }
}
