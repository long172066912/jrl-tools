package org.jrl.redis.core.processor;

import org.jrl.redis.core.constant.HandlePostProcessorTypeEnum;
import org.jrl.redis.core.model.CacheHandleProcessorModel;

import java.util.Set;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: AbstractHandlePostProcessor
 * @Description: 后置处理抽象类
 * @date 2021/1/18 11:52 AM
 */
public abstract class AbstractHandlePostProcessor implements InterfaceHandlePostProcessor {

    /**
     * 获取优先级Id
     *
     * @return
     */
    public abstract int getOrder();

    /**
     * 获取处理器Id
     *
     * @return
     */
    public abstract int getHandlePostId();

    /**
     * 获取处理器类型
     *
     * @return
     */
    public abstract HandlePostProcessorTypeEnum getHandleType();

    /**
     * 获取客户端类型
     *
     * @return
     */
    public abstract int getClientType();

    @Override
    public Set<String> specifiedCommands() {
        return null;
    }

    /**
     * 注册实现类到工厂
     */
    public abstract void registerIntoPostFactory();

    @Override
    public void handleBefore(CacheHandleProcessorModel cacheHandleProcessorModel) {
    }

    @Override
    public void handleAfter(CacheHandleProcessorModel cacheHandleProcessorModel) {
    }

    @Override
    public void onSuccess(CacheHandleProcessorModel cacheHandleProcessorModel) {
    }

    @Override
    public void onFail(CacheHandleProcessorModel cacheHandleProcessorModel) {
    }
}
