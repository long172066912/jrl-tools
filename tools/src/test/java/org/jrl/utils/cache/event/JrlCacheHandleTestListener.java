package org.jrl.utils.cache.event;

import org.jrl.tools.cache.core.event.listener.AbstractJrlCacheHandleEventListener;
import org.jrl.tools.cache.core.event.model.JrlCacheHandleEventData;
import org.jrl.tools.json.JrlJsonNoExpUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
* @Description: //TODO (用一句话描述该文件做什么)
* @author JerryLong
*/
public class JrlCacheHandleTestListener extends AbstractJrlCacheHandleEventListener {

    private static AtomicInteger incr = new AtomicInteger();

    @Override
    public void onEvent(JrlCacheHandleEventData eventData) {
        System.out.println(JrlJsonNoExpUtil.toJson(eventData));
        incr.incrementAndGet();
    }

    public int getIncr() {
        return incr.get();
    }
}
