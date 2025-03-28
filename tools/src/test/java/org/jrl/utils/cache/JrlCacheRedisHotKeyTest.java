package org.jrl.utils.cache;

import org.jrl.tools.cache.JrlCache;
import org.jrl.tools.cache.core.builder.CacheLoadBuilder;
import org.jrl.tools.cache.core.builder.JrlCacheBuilder;
import org.jrl.tools.cache.extend.mesh.redis.JrlCacheRedisKeyPrefixBuilder;
import org.jrl.redis.client.CacheClientFactory;
import org.jrl.redis.core.BaseCacheExecutor;
import org.jrl.redis.core.cache.redis.lettuce.config.LettuceConnectSourceConfig;
import org.jrl.tools.utils.function.AbstractJrlFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * 热key单元测试
 *
 * @author JerryLong
 */
public class JrlCacheRedisHotKeyTest {

    @Test
    public void testString() throws InterruptedException {
        Map<String, AtomicInteger> at = new ConcurrentHashMap<>();
        JrlCache<String, String> cache = JrlCacheBuilder.<String, String>builder("test")
                .redisNoLock("test")
                //统计热key，1秒
                .hotKey(true, true, 1, 50, 1000, 10)
                .cacheLoader(CacheLoadBuilder.<String, String>builder()
                        .cacheLoader(new AbstractJrlFunction<String, String>() {
                            @Override
                            public String apply(String s) {
                                at.computeIfAbsent(s, k -> new AtomicInteger(0)).incrementAndGet();
                                return s;
                            }
                        })
                        .build())
                .build();
        test(cache, () -> "test1", () -> "test2", at, "test1", "test2");
        Assertions.assertEquals(cache.get("test1"), "test1");
        Assertions.assertEquals(cache.get("test2"), "test2");
    }

    @Test
    public void testKeyBuilder() throws InterruptedException {
        Map<String, AtomicInteger> at = new ConcurrentHashMap<>();
        JrlCache<JrlCacheRedisKeyPrefixBuilder<TestModel>, TestModel> cache = JrlCacheBuilder.<JrlCacheRedisKeyPrefixBuilder<TestModel>, TestModel>builder("test")
                .redisNoLock("test")
                //统计热key，1秒
                .hotKey(true, true, 1, 50, 1000, 10)
                .cacheLoader(CacheLoadBuilder.<JrlCacheRedisKeyPrefixBuilder<TestModel>, TestModel>builder()
                        .cacheLoader(new AbstractJrlFunction<JrlCacheRedisKeyPrefixBuilder<TestModel>, TestModel>() {
                            @Override
                            public TestModel apply(JrlCacheRedisKeyPrefixBuilder<TestModel> jrlCacheRedisKeyPrefixBuilder) {
                                at.computeIfAbsent(jrlCacheRedisKeyPrefixBuilder.getKey().getName(), k -> new AtomicInteger(0)).incrementAndGet();
                                return jrlCacheRedisKeyPrefixBuilder.getKey();
                            }
                        })
                        .build())
                .build();

        test(cache,
                () -> new JrlCacheRedisKeyPrefixBuilder<>("test", new TestModel("1", 1)),
                () -> new JrlCacheRedisKeyPrefixBuilder<>("test", new TestModel("2", 2)),
                at, "1", "2");

        Assertions.assertEquals(cache.get(new JrlCacheRedisKeyPrefixBuilder<>("test", new TestModel("1", 1))).getName(), "1");
        Assertions.assertEquals(cache.get(new JrlCacheRedisKeyPrefixBuilder<>("test", new TestModel("2", 2))).getName(), "2");
    }

    @Test
    public void testBothHotKey() throws InterruptedException {
        JrlCache<String, String> cache = JrlCacheBuilder.<String, String>builder("test")
                .redisNoLock("test")
                //统计热key，1秒
                .hotKey(true, true, 1, 50, 1000, 10)
                .cacheLoader(CacheLoadBuilder.<String, String>builder()
                        .cacheLoader(new AbstractJrlFunction<String, String>() {
                            @Override
                            public String apply(String s) {
                                return s;
                            }
                        })
                        .build())
                .build();
        final BaseCacheExecutor cacheExecutor = CacheClientFactory.getCacheExecutor("test", new LettuceConnectSourceConfig());
        for (int i = 0; i < 1000; i++) {
            cache.get("test1");
        }
        // 等待1秒
        Thread.sleep(1000L);
        cache.get("test1");
        cacheExecutor.del("test1");
        Assertions.assertEquals(cache.get("test1"), "test1");
        cache.remove("test1");
        //再循环1000次，让key再次变成热key
        for (int i = 0; i < 1000; i++) {
            cache.get("test1");
        }
        // 等待1秒
        Thread.sleep(1000L);
        cacheExecutor.del("test1");
        Assertions.assertEquals(cache.get("test1"), "test1");
    }

    private <K, V> void test(JrlCache cache, Supplier<K> key1, Supplier<K> key2, Map<String, AtomicInteger> at, String k1, String k2) throws InterruptedException {
        final BaseCacheExecutor cacheExecutor = CacheClientFactory.getCacheExecutor("test", new LettuceConnectSourceConfig());
        cache.remove(key1.get());
        cache.remove(key2.get());
        for (int i = 0; i < 999; i++) {
            cache.get(key1.get());
            cache.get(key2.get());
        }
        //1多调用一次
        cache.get(key1.get());
        // 等待1秒
        Thread.sleep(1000L);
        for (int i = 0; i < 5; i++) {
            cacheExecutor.del("test1");
            cacheExecutor.del("test2");
            cache.get(key1.get());
            cache.get(key2.get());
        }
        Assertions.assertEquals(at.get(k1).get(), 2);
        Assertions.assertEquals(at.get(k2).get(), 6);
    }

    private static class TestModel {
        private String name;
        private Integer age;

        public TestModel() {
        }

        public TestModel(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestModel)) return false;
            TestModel testModel = (TestModel) o;
            return name.equals(testModel.name) && age.equals(testModel.age);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }

        @Override
        public String toString() {
            return name;
        }

        public String getName() {
            return name;
        }

        public Integer getAge() {
            return age;
        }
    }
}
