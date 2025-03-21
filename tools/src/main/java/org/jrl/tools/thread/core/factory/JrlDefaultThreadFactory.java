package org.jrl.tools.thread.core.factory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程工厂
 *
 * @author JerryLong
 */
public class JrlDefaultThreadFactory implements ThreadFactory {
    private final AtomicInteger threadNumber = new AtomicInteger(0);
    private final String namePrefix;

    public JrlDefaultThreadFactory(String namePrefix) {
        this.namePrefix = namePrefix + "-thread-";
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, namePrefix + threadNumber.getAndIncrement());
    }
}