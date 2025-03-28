package org.jrl.utils.cache;

import org.jrl.tools.cache.JrlCache;
import org.jrl.tools.cache.JrlCacheUtil;
import org.jrl.tools.cache.config.DefaultJrlCacheExpireConfig;
import org.jrl.tools.cache.extend.mesh.JrlCacheLockConfig;
import org.jrl.tools.utils.function.AbstractJrlFunction;
import org.jrl.utils.cache.model.Req;
import org.jrl.utils.cache.model.Res;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 多级缓存测试
 *
 * @author JerryLong
 */
public class JrlCacheLocalAndRedisTest {

    private static final AtomicInteger count = new AtomicInteger(0);

    private static final AbstractJrlFunction<String, String> DEFAULT_LOADER = new AbstractJrlFunction<String, String>() {
        @Override
        public String apply(String s) {
            return "test" + count.incrementAndGet();
        }
    };


    @Test
    public void test() {
        //多级缓存client
        final JrlCache<String, String> localAndRedisCache = JrlCacheUtil.getLocalAndRedisCache("test", "redis", DEFAULT_LOADER, JrlCacheLockConfig.noLock());
        //获取一个redis的client
        final JrlCache<String, String> redisCache = JrlCacheUtil.getRedisCache("test", "redis", DEFAULT_LOADER, JrlCacheLockConfig.noLock());

        String key = "testLocalAndRedis";
        localAndRedisCache.remove(key);
        Assertions.assertEquals("test1", localAndRedisCache.get(key));
        Assertions.assertEquals("test1", redisCache.get(key));
        Assertions.assertEquals("test1", localAndRedisCache.get(key));
        Assertions.assertEquals(1, count.get());
    }

    @Test
    public void test2() {
        final JrlCache<Req, Res> bothCache = JrlCacheUtil.getLocalAndRedisCacheExtKeyBuilder("testBoth", "test", new AbstractJrlFunction<Req, Res>() {
            @Override
            public Res apply(Req req) {
                return new Res(req.getName(), req.getAge());
            }
        }, JrlCacheLockConfig.tryLock(1L, 30L, TimeUnit.SECONDS), DefaultJrlCacheExpireConfig.oneDay());

        System.out.println(bothCache.getConfig().getCacheType());

        final JrlCache<Req, Res> redisCache = JrlCacheUtil.getRedisCacheExtKeyBuilder("test", "test", new AbstractJrlFunction<Req, Res>() {
            @Override
            public Res apply(Req req) {
                return new Res(req.getName(), req.getAge());
            }
        }, JrlCacheLockConfig.noLock(), DefaultJrlCacheExpireConfig.oneDay());

        bothCache.remove(new Req("test", 1));
        Assertions.assertEquals(new Res("test", 1), bothCache.get(new Req("test", 1)));
        redisCache.remove(new Req("test", 1));
        Assertions.assertFalse(redisCache.exists(new Req("test", 1)));
        Assertions.assertEquals(new Res("test", 1), bothCache.get(new Req("test", 1)));
    }

    @Test
    public void testSubscribe() {
        final JrlCache<Req, Res> bothCache = JrlCacheUtil.getLocalAndRedisCacheExtKeyBuilder("testBoth", "test", new AbstractJrlFunction<Req, Res>() {
            @Override
            public Res apply(Req req) {
                return new Res(req.getName(), req.getAge());
            }
        }, JrlCacheLockConfig.tryLock(1L, 30L, TimeUnit.SECONDS), DefaultJrlCacheExpireConfig.oneDay());

        bothCache.get(new Req("test", 1));
        bothCache.put(new Req("test", 1), new Res("test", 2));
        Assertions.assertEquals(new Res("test", 2), bothCache.get(new Req("test", 1)));
    }
}
