package org.jrl.tools.utils.hotkey;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * 优先队列方式实现，未加锁，通过单线程消费无并发
 *
 * @author JerryLong
 */
public class JrlHotKeyPriorityQueueStatistics<K> extends AbstractJrlHotKeyStatistics<K> {

    public JrlHotKeyPriorityQueueStatistics(Function<K, JrlHotKey<K>> hotKeySupplier) {
        super(hotKeySupplier);
    }

    public JrlHotKeyPriorityQueueStatistics(int statisticCapacity, int countLeastValue, Function<K, JrlHotKey<K>> hotKeySupplier) {
        super(hotKeySupplier);
        this.statisticCapacity = statisticCapacity;
        this.countLeastValue = countLeastValue;
    }

    /**
     * 数量限制，只保留指定数量的元素
     * 最小堆，即频率最低的在队头，每次将新元素与堆头的比较，留下其中较大者
     */
    private final PriorityQueue<JrlHotKey<K>> hotKeyQueue = new PriorityQueue<>(statisticCapacity, (e1, e2) -> e1.getCount().intValue() > e2.getCount().intValue() ? 1 : 0);

    private final Set<K> existedSet = new HashSet<>();

    private final ConcurrentHashMap<K, JrlHotKey<K>> hotkeyMap = new ConcurrentHashMap<>();

    private final ReentrantLock incrLock = new ReentrantLock();

    @Override
    public List<JrlHotKey<K>> getHotkeys() {
        return Arrays.asList(hotKeyQueue.toArray(new JrlHotKey[]{}));
    }

    @Override
    public void clean() {
        hotkeyMap.clear();
        existedSet.clear();
        hotKeyQueue.clear();
    }

    @Override
    public void incr(K key) {
        JrlHotKey<K> item = hotkeyMap.computeIfAbsent(key, e -> hotKeySupplier.apply(key));
        incrLock.lock();
        try {
            //自增
            item.getCount().increment();
            //如果未达到起始值，不计入热key
            if (item.getCount().intValue() < countLeastValue) {
                return;
            }
            //如果队列中存在对象
            if (existedSet.contains(key)) {
                hotKeyQueue.remove(item);
                hotKeyQueue.offer(item);
            } else {
                //如果队列长度不足
                if (hotKeyQueue.size() < statisticCapacity) {
                    hotKeyQueue.remove(item);
                    hotKeyQueue.offer(item);
                    existedSet.add(key);
                    return;
                }
                //弹出首位最小的进行对比，如果大于则插入
                JrlHotKey<K> head = hotKeyQueue.peek();
                if (head == null) {
                    return;
                }
                if (item.getCount().intValue() > head.getCount().intValue()) {
                    hotKeyQueue.poll();
                    hotKeyQueue.offer(item);
                    existedSet.remove(head.getKey());
                    existedSet.add(key);
                }
            }
        } finally {
            incrLock.unlock();
        }
    }
}
