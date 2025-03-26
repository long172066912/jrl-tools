package org.jrl.redis.extend.handle.monitor.statistics;

import org.jrl.redis.extend.handle.monitor.hotkey.model.HotKeyItem;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: SkipMapStatistics
 * @Description: 跳表方式实现，未加锁，通过单线程消费无并发
 * @date 2021/7/5 9:44 AM
 */
public class SkipMapStatistics extends AbstractHotKeyStatistics {

    public static final int MAX_SIZE = 10000;
    public final int mapSize;
    private final String type;

    public SkipMapStatistics(String type,int mapSize) {
        if (mapSize > MAX_SIZE) {
            throw new IllegalArgumentException("maxSize must be less than 10000");
        }
        this.type = type;
        this.mapSize = mapSize;
    }

    public SkipMapStatistics(String type, int statisticCapacity, int countLeastValue) {
        this.type = type;
        this.statisticCapacity = statisticCapacity;
        this.countLeastValue = countLeastValue;
        this.mapSize = 1000;
    }

    private Map<String, HotKeyItem> map = new ConcurrentHashMap<>();

    private ConcurrentSkipListMap<HotKeyItem, String> hotkeyMap =
            new ConcurrentSkipListMap<>((e1, e2) -> {
                if (e1.getKey().equals(e2.getKey())) {
                    return 0;
                }
                return e1.getCount().intValue() == e2.getCount().intValue() ? e1.getKey().compareTo(e2.getKey()) : e1.getCount().intValue() > e2.getCount().intValue() ? 1 : -1;
            });

    private ReentrantLock incrLock = new ReentrantLock();

    @Override
    public List<HotKeyItem> getHotkeys() {
        return hotkeyMap.keySet().stream().collect(Collectors.toList());
    }

    @Override
    public void clean() {
        map.clear();
        hotkeyMap.clear();
    }

    public Set<String> getKeys() {
        return new HashSet<>(hotkeyMap.values());
    }

    @Override
    public void incr(String commands, String key) {
        //如果map中不存在该key，且map已满，则不加入
        if (!map.containsKey(key) && map.size() > mapSize) {
            return;
        }
        HotKeyItem item = map.computeIfAbsent(key, e -> new HotKeyItem(commands, key, new LongAdder()));
        incrLock.lock();
        try {
            //必须先删除
            hotkeyMap.remove(item);
            item.calibrationCommands(commands);
            item.getCount().increment();
            //如果未达到起始值，不计入热key
            if (item.getCount().intValue() < countLeastValue) {
                return;
            }

            hotkeyMap.put(item, key);
            //如果队列长度不足STATISTIC_CAPACITY
            if (hotkeyMap.size() <= statisticCapacity) {
                return;
            }
            //弹出首位最小的进行对比，如果大于则插入
            Map.Entry<HotKeyItem, String> first = hotkeyMap.firstEntry();
            if (item.getCount().intValue() > first.getKey().getCount().intValue()) {
                hotkeyMap.pollFirstEntry();
                hotkeyMap.put(item, key);
            }
        } finally {
            incrLock.unlock();
        }
    }
}
