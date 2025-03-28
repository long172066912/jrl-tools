package org.jrl.tools.cache.core.event.listener;

import org.jrl.tools.cache.core.event.model.JrlCacheHandleEventData;
import org.jrl.tools.event.JrlEventListener;

/**
 * 命令执行时间监听器-抽象类（未完成，暂时先不支持JrlCache的命令处理耗时监听，redis走redis组件的监控以及统计）
 *
 * @author JerryLong
 */
public abstract class AbstractJrlCacheHandleEventListener implements JrlEventListener<JrlCacheHandleEventData> {

    public static final String CACHE_HANDLE = "jrlCacheHandle";

    public AbstractJrlCacheHandleEventListener() {
        this.register();
    }

    @Override
    public String eventName() {
        return CACHE_HANDLE;
    }
}
