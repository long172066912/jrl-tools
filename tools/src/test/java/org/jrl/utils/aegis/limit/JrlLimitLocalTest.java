package org.jrl.utils.aegis.limit;

import org.jrl.tools.aegis.JrlAegis;
import org.jrl.tools.aegis.JrlAegisContext;
import org.jrl.tools.aegis.JrlAegisUtil;
import org.jrl.tools.aegis.core.AbstractJrlAegis;
import org.jrl.tools.aegis.core.rule.JrlAegisLimitRule;
import org.jrl.tools.aegis.exception.JrlAegisLimitException;
import org.jrl.tools.aegis.model.JrlAegisLimitType;
import org.jrl.tools.aegis.model.JrlAegisScope;
import org.jrl.tools.condition.JrlCondition;
import org.jrl.tools.condition.JrlConditionItem;
import org.jrl.tools.utils.function.AbstractJrlCallable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class JrlLimitLocalTest {

    @Test
    public void testLocal() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> JrlAegisUtil.limit().local("test").build());
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            JrlAegisUtil.limit().local("test")
                    .addRule(JrlAegisLimitRule.builder().count(10).build())
                    .addRule(JrlAegisLimitRule.builder().count(5).build())
                    .build();
        });
    }

    @Test
    public void testQps() throws InterruptedException {
        final JrlAegis limiter = JrlAegisUtil.limit().local("testQps")
                .addRule(JrlAegisLimitRule.builder().count(10).type(JrlAegisLimitType.QPS).build())
                .build();

        Assertions.assertEquals(limiter, JrlAegisUtil.getAegis("testQps"));
        for (int i = 0; i < 10; i++) {
            Assertions.assertTrue(limiter.tryAcquire());
        }
        Assertions.assertFalse(limiter.tryAcquire());
        Thread.sleep(1000L);
        for (int i = 0; i < 10; i++) {
            Assertions.assertTrue(limiter.tryAcquire());
        }
        Assertions.assertFalse(limiter.tryAcquire());
    }

    @Test
    public void testQps2() throws InterruptedException {
        final JrlAegis limiter = JrlAegisUtil.limit().local("testQps2")
                .addRule(JrlAegisLimitRule.builder().count(10).type(JrlAegisLimitType.QPS).build())
                .addRule(JrlAegisLimitRule.builder().count(5).type(JrlAegisLimitType.QPS).scope(new JrlAegisScope(1, "testTime")).endTime(System.currentTimeMillis() + 1000).build())
                .build();
        final long l = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            Assertions.assertTrue(limiter.tryAcquire());
        }
        Assertions.assertTrue(System.currentTimeMillis() - l < 100, "时间错误" + (System.currentTimeMillis() - l));
        Assertions.assertFalse(limiter.tryAcquire());
        Thread.sleep(1000L);
        for (int i = 0; i < 10; i++) {
            Assertions.assertTrue(limiter.tryAcquire());
        }
        Assertions.assertFalse(limiter.tryAcquire());
    }

    @Test
    public void testQps3() throws InterruptedException {
        final JrlAegis limiter = JrlAegisUtil.limit().local("testQps3")
                .addRule(JrlAegisLimitRule.builder().count(10).type(JrlAegisLimitType.QPS).build())
                .addRule(JrlAegisLimitRule.builder().count(5).type(JrlAegisLimitType.QPS).scope(new JrlAegisScope(1, "testTime")).endTime(System.currentTimeMillis() + 1000).build())
                .build();
        final long l = System.currentTimeMillis();
        AtomicInteger n = new AtomicInteger();
        for (int i = 0; i < 5; i++) {
            Assertions.assertEquals(i, (int) limiter.tryAcquire(new AbstractJrlCallable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return n.getAndIncrement();
                }
            }));
        }
        Assertions.assertTrue(System.currentTimeMillis() - l < 100, "时间错误" + (System.currentTimeMillis() - l));
        Assertions.assertThrows(JrlAegisLimitException.class, () -> limiter.tryAcquire(new AbstractJrlCallable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return n.getAndIncrement();
            }
        }));
        Thread.sleep(1000L);
        for (int i = 0; i < 10; i++) {
            Assertions.assertEquals(i + 5, (int) limiter.tryAcquire(new AbstractJrlCallable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return n.getAndIncrement();
                }
            }));
        }
        Assertions.assertThrows(JrlAegisLimitException.class, () -> limiter.tryAcquire(new AbstractJrlCallable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return n.getAndIncrement();
            }
        }));
    }

    @Test
    public void testTimeWindow() throws InterruptedException {
        final JrlAegis limiter = JrlAegisUtil.limit().local("testTimeWindow")
                .addRule(JrlAegisLimitRule.builder().count(10).timeWindow(2).type(JrlAegisLimitType.TIME_WINDOW).build())
                .build();
        Assertions.assertEquals(limiter, JrlAegisUtil.getAegis("testTimeWindow"));
        for (int i = 0; i < 10; i++) {
            Thread.sleep(200L);
            Assertions.assertTrue(limiter.tryAcquire());
        }
        Assertions.assertFalse(limiter.tryAcquire());
        Thread.sleep(1001L);
        for (int i = 0; i < 5; i++) {
            Assertions.assertTrue(limiter.tryAcquire());
        }
        Assertions.assertFalse(limiter.tryAcquire());
    }

    @Test
    public void testThread() throws InterruptedException {
        final JrlAegis limiter = JrlAegisUtil.limit().local("testThread")
                .addRule(JrlAegisLimitRule.builder().count(10).type(JrlAegisLimitType.THREAD).build())
                .build();
        AtomicInteger n = new AtomicInteger();
        CountDownLatch countDownLatch = new CountDownLatch(10);
        final ExecutorService pool = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            CompletableFuture.runAsync(() -> {
                limiter.tryAcquire(new AbstractJrlCallable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        countDownLatch.countDown();
                        System.out.println(n.incrementAndGet());
                        Thread.sleep(500L);
                        return n.get();
                    }
                });
            }, pool);
        }
        final boolean await = countDownLatch.await(1, TimeUnit.SECONDS);
        Assertions.assertEquals(10, n.get());
        Assertions.assertFalse(limiter.tryAcquire());
        Assertions.assertThrows(JrlAegisLimitException.class, () -> limiter.tryAcquire(new AbstractJrlCallable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return n.getAndIncrement();
            }
        }));
        Thread.sleep(500L);
        Assertions.assertEquals(11, limiter.tryAcquire(new AbstractJrlCallable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return n.incrementAndGet();
            }
        }));
    }

    @Test
    public void testThread2() throws InterruptedException {
        final JrlAegis limiter = JrlAegisUtil.limit().local("testThread2")
                .addRule(JrlAegisLimitRule.builder().count(10).id(0).type(JrlAegisLimitType.THREAD).build())
                .addRule(JrlAegisLimitRule.builder().count(5).id(1).type(JrlAegisLimitType.THREAD).build())
                .build();
        AtomicInteger n = new AtomicInteger();
        CountDownLatch countDownLatch = new CountDownLatch(5);
        final ExecutorService pool = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            CompletableFuture.runAsync(() -> {
                limiter.tryAcquire(new AbstractJrlCallable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        countDownLatch.countDown();
                        System.out.println(n.incrementAndGet());
                        Thread.sleep(500L);
                        return n.get();
                    }
                });
            }, pool);
        }
        final boolean await = countDownLatch.await(1, TimeUnit.SECONDS);
        Assertions.assertEquals(5, n.get());
        Assertions.assertFalse(limiter.tryAcquire());
        Assertions.assertThrows(JrlAegisLimitException.class, () -> limiter.tryAcquire(new AbstractJrlCallable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return n.getAndIncrement();
            }
        }));
        Thread.sleep(500L);
        Assertions.assertEquals(6, limiter.tryAcquire(new AbstractJrlCallable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return n.incrementAndGet();
            }
        }));
    }

    @Test
    public void testCondition() throws InterruptedException {
        final JrlAegis limiter = JrlAegisUtil.limit().local("testCondition")
                .addRule(JrlAegisLimitRule.builder().count(10)
                        .condition(JrlCondition.builder()
                             .and()
                                .item(JrlConditionItem.builder().key("name").values("wb", "wanba").must().build())
                                .item(JrlConditionItem.builder().key("age").values(18, 19).must().build())
                                .build())
                        .build())
                .build();
        Assertions.assertEquals(limiter, JrlAegisUtil.getAegis("testCondition"));
        for (int i = 0; i < 20; i++) {
            Assertions.assertTrue(limiter.tryAcquire());
        }
        JrlAegisContext.getContext().enter("name", "wb").enter("age", 18);
        for (int i = 0; i < 10; i++) {
            Assertions.assertTrue(limiter.tryAcquire());
        }
        Assertions.assertEquals("wb", JrlAegisContext.getContext().getRequest().get("name"));
        Assertions.assertFalse(limiter::tryAcquire);
        JrlAegisContext.clear();
    }

    @Test
    public void testAddRule() throws InterruptedException {
        final AbstractJrlAegis<?, JrlAegisLimitRule> limiter = (AbstractJrlAegis) JrlAegisUtil.limit().local("testAddRule")
                .addRule(JrlAegisLimitRule.builder().count(10).build())
                .addRule(JrlAegisLimitRule.builder().count(20).id(3).build())
                .build();
        for (int i = 0; i < 5; i++) {
            Assertions.assertTrue(limiter.tryAcquire());
        }
        limiter.addRule(JrlAegisLimitRule.builder().id(1).count(4).build());
        for (int i = 0; i < 4; i++) {
            Assertions.assertTrue(limiter.tryAcquire());
        }
        Assertions.assertTrue(limiter.tryAcquire());
        //添加一个id大的
        limiter.addRule(JrlAegisLimitRule.builder().id(5).count(4).build());
        for (int i = 0; i < 4; i++) {
            Assertions.assertTrue(limiter.tryAcquire());
        }
        Assertions.assertFalse(limiter.tryAcquire());
    }

    @Test
    public void testChangeRule() throws InterruptedException {
        final AbstractJrlAegis<?, JrlAegisLimitRule> limiter = (AbstractJrlAegis) JrlAegisUtil.limit().local("testChangeRule")
                .addRule(JrlAegisLimitRule.builder().count(10).id(5).build())
                .addRule(JrlAegisLimitRule.builder().count(20).id(3).build())
                .build();

        for (int i = 0; i < 5; i++) {
            Assertions.assertTrue(limiter.tryAcquire());
        }
        limiter.changeRule(JrlAegisLimitRule.builder().id(5).count(6).build());
        Assertions.assertTrue(limiter.tryAcquire());
        Assertions.assertFalse(limiter.tryAcquire());

        //删除
        limiter.deleteRule(JrlAegisLimitRule.builder().id(5).count(6).build());
        for (int i = 0; i < 20; i++) {
            Assertions.assertTrue(limiter.tryAcquire());
        }
        Assertions.assertFalse(limiter.tryAcquire());
    }
}
