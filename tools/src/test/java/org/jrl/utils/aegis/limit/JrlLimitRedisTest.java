package org.jrl.utils.aegis.limit;

import org.jrl.tools.aegis.JrlAegis;
import org.jrl.tools.aegis.JrlAegisUtil;
import org.jrl.tools.aegis.core.rule.JrlAegisLimitRule;
import org.jrl.tools.aegis.exception.JrlAegisLimitException;
import org.jrl.tools.aegis.model.JrlAegisLimitType;
import org.jrl.tools.aegis.model.JrlAegisScope;
import org.jrl.redis.client.CacheClientFactory;
import org.jrl.redis.core.BaseCacheExecutor;
import org.jrl.redis.core.cache.redis.lettuce.config.LettuceConnectSourceConfig;
import org.jrl.tools.cache.extend.mesh.JrlCacheMeshConnectType;
import org.jrl.tools.utils.function.AbstractJrlCallable;
import org.jrl.utils.aegis.limit.redis.JrlAegisRedisHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.jrl.utils.aegis.limit.redis.JrlAegisRedisHandler.RANDOM_LENGTH;
import static org.jrl.utils.aegis.limit.redis.JrlAegisRedisHandler.RANDOM_LENGTH_MAX;

public class JrlLimitRedisTest {

    private static final BaseCacheExecutor cacheExecutor = CacheClientFactory.getCacheExecutor("test", new LettuceConnectSourceConfig());

    @Test
    public void testTime() throws InterruptedException {
        final int qpsTimeWindow = 1 * RANDOM_LENGTH * 1000;
        List<Long> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Thread.sleep(200L);
            final long l = System.currentTimeMillis() * RANDOM_LENGTH + ThreadLocalRandom.current().nextInt(RANDOM_LENGTH, RANDOM_LENGTH_MAX);
            list.add(l);
        }
        long now = System.currentTimeMillis() * RANDOM_LENGTH + ThreadLocalRandom.current().nextInt(RANDOM_LENGTH, RANDOM_LENGTH_MAX);
        List<Long> finalList = new ArrayList<>();
        for (Long n : list) {
            final long qpsMin = now - qpsTimeWindow;
            System.out.println("n : " + n + " , t : " + qpsMin + " , d : "  + (n - qpsMin));
            if (n >= qpsMin) {
                finalList.add(n);
            }
        }
        Assertions.assertEquals(5, finalList.size());
    }

    @Test
    public void testLua() throws InterruptedException {
        cacheExecutor.del("test");
        final JrlAegisRedisHandler redisHandler = new JrlAegisRedisHandler("test", JrlCacheMeshConnectType.POOL, true);
        Assertions.assertTrue(redisHandler.incr("test", 1));
        Assertions.assertFalse(redisHandler.incr("test", 1));

        //qps-1秒
        cacheExecutor.del("test");
        for (int i = 0; i < 10; i++) {
            Assertions.assertTrue(redisHandler.incrByTimeWindow("test", 10, 1));
        }
        Assertions.assertFalse(redisHandler.incrByTimeWindow("test", 10, 1));

        //时间窗口-2秒
        cacheExecutor.del("test");
        Assertions.assertTrue(redisHandler.incrByTimeWindow("test", 2, 2));
        Thread.sleep(2000L);
        Assertions.assertTrue(redisHandler.incrByTimeWindow("test", 2, 2));
        Assertions.assertTrue(redisHandler.incrByTimeWindow("test", 2, 2));
        Assertions.assertFalse(redisHandler.incrByTimeWindow("test", 2, 2));
        Assertions.assertEquals(cacheExecutor.zcard("test"), 2);
        Assertions.assertTrue(cacheExecutor.ttl("test") <= 4, "ttl error : " + cacheExecutor.ttl("test"));
        cacheExecutor.del("test");
    }

    @Test
    public void testLua2() throws InterruptedException {
        BaseCacheExecutor cacheExecutor = CacheClientFactory.getCacheExecutor("test", new LettuceConnectSourceConfig());
        cacheExecutor.del("test");
        final JrlAegisRedisHandler jrlAegisRedisHandler = new JrlAegisRedisHandler("test", JrlCacheMeshConnectType.NORMAL, true);
        for (int i = 0; i < 10; i++) {
            Assertions.assertTrue(jrlAegisRedisHandler.incrByTimeWindow("test", 10, 100000));
            Thread.sleep(500L);
        }
        Assertions.assertEquals(10, cacheExecutor.zcard("test"));
        Assertions.assertFalse(jrlAegisRedisHandler.incrByTimeWindow("test", 10, 100000));
    }


    @Test
    public void testQps() throws InterruptedException {
        final JrlAegis jrlAegis = JrlAegisUtil.limit().mesh("test", "test")
                .addRule(JrlAegisLimitRule.builder().count(10).type(JrlAegisLimitType.QPS).build())
                .build();
        for (int i = 0; i < 10; i++) {
            Assertions.assertTrue(jrlAegis.tryAcquire());
        }
        Assertions.assertFalse(jrlAegis.tryAcquire());
        Thread.sleep(1000L);
        Assertions.assertTrue(jrlAegis.tryAcquire());
    }

    @Test
    public void testTimeWindow() throws InterruptedException {
        final JrlAegis jrlAegis = JrlAegisUtil.limit().mesh("testMeshTimeWindow", "test")
                .addRule(JrlAegisLimitRule.builder().count(10).timeWindow(2).type(JrlAegisLimitType.TIME_WINDOW).build())
                .build();
        for (int i = 0; i < 10; i++) {
            Thread.sleep(100L);
            Assertions.assertTrue(jrlAegis.tryAcquire());
        }
        Assertions.assertFalse(jrlAegis.tryAcquire());
        Thread.sleep(500L);
        Assertions.assertFalse(jrlAegis.tryAcquire());
        Thread.sleep(1000L);
        Assertions.assertTrue(jrlAegis.tryAcquire());
    }

    @Test
    public void testThread() throws InterruptedException {
        final JrlAegis limiter = JrlAegisUtil.limit().mesh("testMeshThread", "test")
                .addRule(JrlAegisLimitRule.builder().count(10).type(JrlAegisLimitType.THREAD).build())
                .build();
        AtomicInteger n = new AtomicInteger();
        CountDownLatch countDownLatch = new CountDownLatch(10);
        final ExecutorService pool = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
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
        Assertions.assertEquals(10, n.get());
        Assertions.assertFalse(limiter.tryAcquire());
        Assertions.assertThrows(JrlAegisLimitException.class, () -> limiter.tryAcquire(new AbstractJrlCallable<Object>() {
            @Override
            public Object call() throws Exception {
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
    public void testTimeWindowDynamic() throws InterruptedException {
        JrlAegis aegis = JrlAegisUtil.limitTimeMeshDynamicConfig("testMeshTimeWindow", "test", JrlAegisScope.GLOBAL, 1, 10, 1);
        for (int i = 0; i < 10; i++) {
            Assertions.assertTrue(aegis.tryAcquire());
        }
        Assertions.assertFalse(aegis.tryAcquire());
        Thread.sleep(1000L);
        //正确的修改配置
        aegis = JrlAegisUtil.limitTimeMeshDynamicConfig("testMeshTimeWindow", "test", JrlAegisScope.GLOBAL, 1, 5, 1);
        for (int i = 0; i < 5; i++) {
            Assertions.assertTrue(aegis.tryAcquire());
        }
        Assertions.assertFalse(aegis.tryAcquire());
        Thread.sleep(1000L);
        //错误的修改配置
        aegis = JrlAegisUtil.limitTimeMeshDynamicConfig("testMeshTimeWindow", "test", JrlAegisScope.GLOBAL, 2, 10, 1);
        for (int i = 0; i < 5; i++) {
            Assertions.assertTrue(aegis.tryAcquire());
        }
        Assertions.assertFalse(aegis.tryAcquire());
    }
}
