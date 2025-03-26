package org.jrl.redis.core.cache.redis.lettuce.connect;

import org.jrl.redis.exception.CacheExceptionFactory;
import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

/**
 * 多连接模式
 *
 * @author JerryLong
 */
public class LettuceConnectLoop<T> {
    private static Logger LOGGER = JrlLoggerFactory.getLogger(LettuceConnectLoop.class);
    /**
     * 循环节点
     */
    private ResourceNode<T> Loop;
    private final Map<Integer, ResourceNode<T>> loopMap = new HashMap<>();

    public LettuceConnectLoop(int length, Supplier<T> resoureceCreator) {
        if (length <= 1) {
            throw new RuntimeException("length must > 1");
        }
        //初始化环
        ResourceNode<T> nextNode = null;
        LOGGER.info("LettuceConnectLoop init ! length : {}", length);
        for (int i = 0; i < length; i++) {
            ResourceNode<T> node = new ResourceNode<>(resoureceCreator.get(), i);
            loopMap.put(i, node);
            if (null != nextNode) {
                nextNode.setNext(node);
            } else {
                Loop = node;
            }
            nextNode = node;
        }
        //变成环
        nextNode.setNext(Loop);
        LOGGER.info("LettuceConnectLoop init success ! length : {}", length);
    }

    public ResourceNode<T> getResource() {
        final ResourceNode<T> next = Loop.getNext();
        Loop = next;
        return next;
    }

    /**
     * 重置连接
     *
     * @param index
     */
    public void resetResource(int index, T resource) {
        ResourceNode<T> node = loopMap.get(index);
        if (null == node) {
            CacheExceptionFactory.throwException("lettuce loop node index not exist ." + index);
            return;
        }
        node.setResource(resource);
    }

    /**
     * 连接节点
     */
    public static class ResourceNode<T> {
        private T resource;
        private final int index;
        /**
         * 下一个节点
         */
        private ResourceNode<T> next;
        private final StampedLock stampedLock = new StampedLock();

        private ResourceNode(T resource, int index) {
            this.resource = resource;
            this.index = index;
        }

        public T getResource() {
            final long lock = stampedLock.readLock();
            try {
                return resource;
            } finally {
                stampedLock.unlockRead(lock);
            }
        }

        public void setNext(ResourceNode<T> next) {
            this.next = next;
        }

        public ResourceNode<T> getNext() {
            return next;
        }

        public int getIndex() {
            return index;
        }

        public void setResource(T resource) {
            final long lock = stampedLock.writeLock();
            try {
                this.resource = resource;
                LOGGER.info("cache2 lettuce loop reset resource reset ! index : {}", index);
            } finally {
                stampedLock.unlockWrite(lock);
            }
        }
    }
}
