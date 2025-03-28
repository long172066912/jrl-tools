package org.jrl.utils.cache.event;

import org.jrl.tools.cache.core.event.JrlCacheEvents;
import org.jrl.tools.cache.core.event.model.JrlCacheHandleEventData;
import org.jrl.tools.cache.model.JrlCacheType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * 热key处理事件测试
 *
 * @author JerryLong
 */
public class JrlCacheHandleEventTest {

    @Test
    public void test() throws InterruptedException {
        final JrlCacheHandleTestListener jrlCacheHandleTestListener = new JrlCacheHandleTestListener();
        JrlCacheEvents.HANDLE_EVENT.sync().publish(
                new JrlCacheHandleEventData("test", JrlCacheType.LOCAL, "get", true, Arrays.asList(new JrlCacheHandleEventData.Key("test", true, "aa"))));
        Assertions.assertEquals(jrlCacheHandleTestListener.getIncr(), 1);
    }
}
