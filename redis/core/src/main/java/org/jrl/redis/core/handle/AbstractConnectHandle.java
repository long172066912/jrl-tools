package org.jrl.redis.core.handle;

import org.jrl.redis.core.constant.HandlePostProcessorTypeEnum;
import org.jrl.redis.core.converters.PostProcessorConvertersAndExecutor;
import org.jrl.redis.core.model.CacheHandleProcessorModel;
import org.jrl.redis.core.processor.AbstractHandlePostProcessor;
import org.jrl.redis.util.CacheFunction;

import java.util.List;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: AbstractCommomHandle
 * @Description: 对连接创建接口的抽象
 * @date 2021/1/18 3:20 PM
 */
public abstract class AbstractConnectHandle implements InterfaceCommomHandle {

    protected PostProcessorConvertersAndExecutor postProcessorConverters = new PostProcessorConvertersAndExecutor();

    /**
     * 获取客户端类型
     * RedisClientConstants
     *
     * @return
     */
    public abstract int getClientType();

    /**
     * 根据操作类型获取执行链路
     *
     * @return
     */
    public List<AbstractHandlePostProcessor> getHandleLinkList() {
        return postProcessorConverters.getHandlePostProcessors(HandlePostProcessorTypeEnum.CONNECT, this.getClientType());
    }

    /**
     * 执行命令
     *
     * @return
     */
    @Override
    public Object execute(CacheFunction function) {
        return postProcessorConverters.executeHandles(this.getHandleLinkList(), new CacheHandleProcessorModel(function));
    }

    @Override
    public Object execute(CacheFunction function, String... key) {
        return postProcessorConverters.executeHandles(this.getHandleLinkList(), new CacheHandleProcessorModel(function, function.fnToFnName(), null, key));
    }
}
