package org.jrl.tools.thread.core.merge;

import org.jrl.tools.thread.api.JrlMergeThreadExecutor;

import java.util.function.Consumer;

/**
* 合并规则
* @author JerryLong
*/
public class DefaultJrlMergeRule<T> implements JrlMergeThreadExecutor.JrlMergeRule<T> {
    private int time;
    private int count;
    private int queueLength;
    private Consumer<T> consumer;

    @Override
    public JrlMergeThreadExecutor.JrlMergeRule<T> onTime(int time) {
        this.time = time;
        return this;
    }

    @Override
    public JrlMergeThreadExecutor.JrlMergeRule<T> onCount(int count) {
        this.count = count;
        return this;
    }

    @Override
    public JrlMergeThreadExecutor.JrlMergeRule<T> onQueueFull(Consumer<T> consumer) {
        this.consumer = consumer;
        return this;
    }

    @Override
    public void queueLength(int length) {
        this.queueLength = length;
    }

    @Override
    public int getTime() {
        return time;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public int getQueueLength() {
        return this.queueLength;
    }

    @Override
    public Consumer<T> getQueueFullConsumer() {
        return consumer;
    }
}