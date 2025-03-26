package org.jrl.redis.core.processor.factory;

import org.jrl.redis.core.constant.HandlePostProcessorTypeEnum;
import org.jrl.redis.core.processor.AbstractHandlePostProcessor;
import org.jrl.redis.extend.handle.processor.JedisHandlesPostProcessor;
import org.jrl.redis.extend.handle.processor.JedisPubSubPostProcessor;
import org.jrl.redis.extend.handle.processor.LettuceHandlesPostProcessor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: HandlePostFactory
 * @Description: 后置处理工厂
 * @date 2021/1/18 4:07 PM
 */
public class HandlePostFactory {
    /**
     * 后置处理器容器，正序
     */
    private static Map<String, List<AbstractHandlePostProcessor>> beanPostProcessors = new ConcurrentHashMap<>();

    /**
     * 后置处理器容器，倒序
     */
    private static Map<String, List<AbstractHandlePostProcessor>> revBeanPostProcessors = new ConcurrentHashMap<>();

    /**
     * 间隔符
     */
    private final static String SPACE_MARK = "_";

    static {
        //缓存操作
        addBeanPostProcessor(new LettuceHandlesPostProcessor());
        addBeanPostProcessor(new JedisHandlesPostProcessor());
        //发布订阅
        addBeanPostProcessor(new JedisPubSubPostProcessor());
    }

    /**
     * 处理器注册接口，StampedLock保证数据准确性
     *
     * @param handlePostProcessor
     */
    public static synchronized void addBeanPostProcessor(AbstractHandlePostProcessor handlePostProcessor) {
        final String key = getKey(handlePostProcessor.getHandleType(), handlePostProcessor.getClientType());
        List<AbstractHandlePostProcessor> interfaceHandlePostProcessors = beanPostProcessors.get(key);
        if (null == beanPostProcessors.get(key)) {
            interfaceHandlePostProcessors = new ArrayList<>();
        }
        if (!interfaceHandlePostProcessors.contains(handlePostProcessor)) {
            interfaceHandlePostProcessors.add(handlePostProcessor);
            if (interfaceHandlePostProcessors.size() > 0) {
                //排序
                Collections.sort(interfaceHandlePostProcessors, Comparator.comparing(AbstractHandlePostProcessor::getOrder));
            }
            revBeanPostProcessors.remove(key);
            beanPostProcessors.put(key, interfaceHandlePostProcessors);
        }
    }

    /**
     * 获取后置处理器列表
     *
     * @param handlePostProcessorTypeEnum
     * @return
     */
    public static List<AbstractHandlePostProcessor> getBeanPostProcessors(HandlePostProcessorTypeEnum handlePostProcessorTypeEnum, int clientType) {
        final String key = getKey(handlePostProcessorTypeEnum, clientType);
        List<AbstractHandlePostProcessor> list = null != beanPostProcessors.get(key) ? new ArrayList<>(beanPostProcessors.get(key)) : new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        List<AbstractHandlePostProcessor> revList = revBeanPostProcessors.get(key);
        if (CollectionUtils.isEmpty(revList) || list.size() != revList.size()) {
            revList = new ArrayList<>(list);
            if (list.size() > 1) {
                Collections.sort(revList, Comparator.comparing(AbstractHandlePostProcessor::getOrder).reversed());
            }
            revBeanPostProcessors.put(key, revList);
        }
        list.addAll(revList);
        return list;
    }

    /**
     * 获取key
     *
     * @param handlePostProcessorTypeEnum
     * @param clientType
     * @return
     */
    private static String getKey(HandlePostProcessorTypeEnum handlePostProcessorTypeEnum, int clientType) {
        return handlePostProcessorTypeEnum.getType() + SPACE_MARK + clientType;
    }
}
