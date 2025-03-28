package org.jrl.utils.cache.event;

import org.jrl.tools.cache.JrlCache;
import org.jrl.tools.cache.JrlCacheUtil;
import org.jrl.tools.cache.core.event.JrlCacheEvents;
import org.jrl.tools.cache.core.event.model.JrlCacheHotKeyEventData;
import org.jrl.tools.cache.extend.mesh.JrlCacheLockConfig;
import org.jrl.tools.utils.function.AbstractJrlFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 热key处理事件测试
 *
 * @author JerryLong
 */
public class JrlCacheHotKeyTest {

    @Test
    public void test() throws InterruptedException {
        //先构建一个meshCache
        AtomicInteger atomicInteger = new AtomicInteger();
        final JrlCache<String, String> redisCache = JrlCacheUtil.getRedisCache("test", "test", new AbstractJrlFunction<String, String>() {
            @Override
            public String apply(String s) {
                atomicInteger.incrementAndGet();
                return "test";
            }
        }, JrlCacheLockConfig.noLock());

        redisCache.remove("test");
        Assertions.assertEquals(redisCache.get("test"), "test");
        //发送一个事件
        JrlCacheEvents.HOT_KEY_EVENT.publish(new JrlCacheHotKeyEventData<>("test", 60, new HashSet<>(Arrays.asList("test", "aaa"))));
        Thread.sleep(1000L);
        Assertions.assertEquals(redisCache.get("test"), "test");
        redisCache.put("aaa", "bbb");
        Assertions.assertEquals(redisCache.get("aaa"), "bbb");
        Assertions.assertEquals(atomicInteger.get(), 1);
    }
}
