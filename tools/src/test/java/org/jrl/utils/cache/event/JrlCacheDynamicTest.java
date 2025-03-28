package org.jrl.utils.cache.event;

import org.jrl.tools.cache.JrlCache;
import org.jrl.tools.cache.JrlCacheUtil;
import org.jrl.tools.cache.core.event.JrlCacheEvents;
import org.jrl.tools.cache.core.event.model.JrlCacheDynamicEventData;
import org.jrl.tools.cache.extend.mesh.JrlCacheLockConfig;
import org.jrl.tools.utils.function.AbstractJrlFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
* 动态变更事件测试
* @author JerryLong
*/
public class JrlCacheDynamicTest {

    @Test
    public void test() throws InterruptedException {
        //先构建一个meshCache
        final JrlCache<String, String> redisCache = JrlCacheUtil.getRedisCache("test", "test", new AbstractJrlFunction<String, String>() {
            @Override
            public String apply(String s) {
                return "test";
            }
        }, JrlCacheLockConfig.noLock());

        Assertions.assertEquals(redisCache.get("test"), "test");
        //发送一个事件
        JrlCacheEvents.DYNAMIC_EVENT.publish(new JrlCacheDynamicEventData("test", 60));
        Thread.sleep(1000L);
        Assertions.assertEquals(redisCache.get("test"), "test");
    }
}
