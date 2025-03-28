package org.jrl.utils.cache;

import org.jrl.tools.cache.JrlCache;
import org.jrl.tools.cache.JrlCacheUtil;
import org.jrl.tools.cache.extend.mesh.JrlCacheLockConfig;
import org.jrl.tools.utils.function.AbstractJrlFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
* 主动刷新测试
* @author JerryLong
*/
public class JrlCacheRefreshTest {

    @Test
    public void local() {
        AtomicInteger count = new AtomicInteger(0);
        final JrlCache<String, String> cache = JrlCacheUtil.getLocalCache("test", (k) -> "test" + count.incrementAndGet());
        Assertions.assertEquals(cache.get("test"), "test1");
        cache.refresh("test");
        Assertions.assertEquals(cache.get("test"), "test2");
        Assertions.assertEquals(cache.get("test"), "test2");
    }


    @Test
    public void mesh() {
        AtomicInteger count = new AtomicInteger(0);
        final JrlCache<String, String> cache = JrlCacheUtil.getRedisCache("test", "test", new AbstractJrlFunction<String, String>() {
            @Override
            public String apply(String s) {
                return "test" + count.incrementAndGet();
            }
        }, JrlCacheLockConfig.noLock());
        cache.remove("test");
        Assertions.assertEquals(cache.get("test"), "test1");
        cache.refresh("test");
        Assertions.assertEquals(cache.get("test"), "test2");
        Assertions.assertEquals(cache.get("test"), "test2");
        cache.remove("test");
    }
}
