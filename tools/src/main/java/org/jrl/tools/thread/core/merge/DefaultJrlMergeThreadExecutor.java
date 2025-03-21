package org.jrl.tools.thread.core.merge;

import org.jrl.tools.thread.api.JrlMergeThreadExecutor;
import org.jrl.tools.thread.api.JrlThreadShutdownHandler;
import org.jrl.tools.thread.api.callback.JrlThreadCallback;
import org.jrl.tools.thread.core.factory.JrlThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * 合并任务处理实现
 * 未来可以支持动态背压控制
 *
 * @author JerryLong
 */
public class DefaultJrlMergeThreadExecutor<T> implements JrlMergeThreadExecutor<T> {

    public static final int QUEUE_LENGTH = 10000;
    public static final int DEFAULT_DELAY_TIME = 1000;
    private static Logger LOGGER = LoggerFactory.getLogger(DefaultJrlMergeThreadExecutor.class);

    private final String name;
    private final int time;
    private final int count;
    private final JrlMergeConsumer<T> consumer;
    private final JrlThreadPool executePool;
    /**
     * 队列长度10000，暂不支持扩大，不够了直接调用consumer逻辑执行
     */
    private final LinkedBlockingDeque<T> queue;
    private final AtomicBoolean isCanJoin = new AtomicBoolean(true);
    private final Consumer<T> queueFullConsumer;

    public DefaultJrlMergeThreadExecutor(String name, JrlMergeRule<T> mergeRule, JrlMergeConsumer<T> consumer, JrlThreadPool executePool) {
        this.name = name;
        this.consumer = consumer;
        this.executePool = executePool;
        if (mergeRule.getCount() == 0 && mergeRule.getTime() == 0) {
            throw new IllegalArgumentException("jrl-thread mergeRule count and time can not both be 0 ! " + name);
        }
        this.count = mergeRule.getCount() <= 0 ? 100 : mergeRule.getCount();
        this.time = mergeRule.getTime() <= 0 ? DEFAULT_DELAY_TIME : mergeRule.getTime();
        //队列长度不能比10000大
        final int queueLength = Math.min(mergeRule.getQueueLength(), QUEUE_LENGTH);
        this.queue = new LinkedBlockingDeque<>(queueLength > 0 ? queueLength : QUEUE_LENGTH);
        this.queueFullConsumer = mergeRule.getQueueFullConsumer();
        executePool.setShutdownFailHandler(new JrlThreadShutdownHandler() {
            @Override
            public void onShutdown(ThreadPoolExecutor executor) {
                isCanJoin.compareAndSet(true, false);
            }

            @Override
            public void onFail(ThreadPoolExecutor executor) {
                LOGGER.error("jrl-thread mergeConsumer shutdown fail ! name : {} , queue size : {} , max size : {}", name, queue.size(), QUEUE_LENGTH);
            }
        });
        LOGGER.info("jrl-thread start mergeConsumer ! name : {} , time : {} , count : {}", name, time, count);
        this.executePool.execute(this::doConsume);
    }

    @Override
    public JrlMergeThreadExecutor<T> join(T task) {
        if (!isCanJoin.get()) {
            if (null != queueFullConsumer) {
                queueFullConsumer.accept(task);
            } else {
                throw new IllegalStateException("jrl-thread mergeTask join fail ! can't join ! pool name : " + name);
            }
        }
        try {
            queue.add(task);
        } catch (Throwable e) {
            if (null != queueFullConsumer) {
                queueFullConsumer.accept(task);
            } else {
                LOGGER.error("jrl-thread JrlMergeThreadExecutor join task error ! use consumer execute ! name : {} , queue size : {} , max size : {}", name, queue.size(), QUEUE_LENGTH, e);
                throw e;
            }
        }
        return this;
    }

    @Override
    public int size() {
        return queue.size();
    }

    private void doConsume() {
        List<T> tasks = new ArrayList<>(this.count);
        long l = System.currentTimeMillis();
        while (isCanJoin.get() || queue.size() > 0) {
            try {
                //如果超时或者数量够了，直接执行
                if ((this.time <= (System.currentTimeMillis() - l) && tasks.size() > 0) || tasks.size() >= this.count) {
                    execute(tasks);
                    //清除列表，等待重新添加
                    tasks = new ArrayList<>(this.count);
                    l = System.currentTimeMillis();
                    continue;
                }
                final T t = queue.poll(this.time, TimeUnit.MILLISECONDS);
                if (null != t) {
                    tasks.add(t);
                }
            } catch (Throwable e) {
                LOGGER.error("jrl-thread JrlMergeThreadExecutor doConsume error ! name : {} , queue size : {} , max size : {}", name, queue.size(), QUEUE_LENGTH, e);
            }
        }
    }

    /**
     * 执行合并任务
     *
     * @param tasks
     */
    private void execute(List<T> tasks) {
        if (null != tasks && tasks.size() > 0) {
            //复制list，并执行
            final ArrayList<T> ts = new ArrayList<>(tasks);
            LOGGER.info("jrl-thread put merge task ! name : {} , mergeSize : {}", this.name, ts.size());
            this.executePool.execute(() -> {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("jrl-thread execute merge task ! name : {} , mergeSize : {}", this.name, ts.size());
                }
                consumer.execute(ts);
            }, new JrlThreadCallback() {
                @Override
                public void onError(Throwable e) {
                    LOGGER.error("jrl-thread execute merge task error ! name : {} , taskSize : {}", name, tasks.size(), e);
                }
            });
        } else {
            LOGGER.warn("jrl-thread merge task is empty ! name : {}", name);
        }
    }
}
