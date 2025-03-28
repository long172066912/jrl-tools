package org.jrl.utils.cache;

import org.jrl.tools.cache.JrlCache;
import org.jrl.tools.cache.JrlCacheUtil;
import org.jrl.tools.cache.config.DefaultJrlCacheExpireConfig;
import org.jrl.tools.cache.core.builder.CacheLoadBuilder;
import org.jrl.tools.cache.core.builder.JrlCacheBuilder;
import org.jrl.tools.cache.model.JrlCacheLoadType;
import org.jrl.tools.utils.function.AbstractJrlFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

/**
 * 本地缓存测试用例
 *
 * @author JerryLong
 */
public class JrlCacheLocalTest {
    @Test
    public void test() {
        final JrlCache<String, String> cache = JrlCacheUtil.getLocalCache("test", (k) -> "test");

        Assertions.assertEquals("test", cache.get("test"));
        cache.remove("test");
        Assertions.assertFalse(cache.exists("test"));
        cache.put("test", "test1");
        Assertions.assertEquals("test1", cache.get("test"));
    }

    @Test
    public void testPreload() {
        JrlCache<String, String> cache = JrlCacheBuilder.<String, String>builder("testPreload")
                .localCache()
                .cacheLoader(CacheLoadBuilder.<String, String>builder()
                        .cacheLoader(new AbstractJrlFunction<String, String>() {
                            @Override
                            public String apply(String s) {
                                return "test";
                            }
                        })
                        .loadType(JrlCacheLoadType.PRELOAD)
                        .preLoad("test")
                        .build()
                )
                .build();

        Assertions.assertTrue(cache.exists("test"));
    }

    @Test
    public void testSchedule() {
        JrlCache<String, String> cache = JrlCacheBuilder.<String, String>builder("testSchedule")
                .localCache()
                .cacheLoader(CacheLoadBuilder.<String, String>builder()
                        .cacheLoader(new AbstractJrlFunction<String, String>() {
                            @Override
                            public String apply(String s) {
                                return "test";
                            }
                        })
                        .loadType(JrlCacheLoadType.PRELOAD_SCHEDULED)
                        .preLoad("test")
                        .build()
                )
                //1秒过期
                .expire(new DefaultJrlCacheExpireConfig(1, TimeUnit.SECONDS))
                .build();

        Assertions.assertTrue(cache.exists("test"));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertTrue(cache.exists("test"));
    }
}
