package org.jrl.tools.utils.hotkey;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;

/**
* 热key统计
* @author JerryLong
*/
public abstract class AbstractJrlHotKeyStatistics<K> {

    public AbstractJrlHotKeyStatistics(Function<K, JrlHotKey<K>> hotKeySupplier) {
        this.hotKeySupplier = hotKeySupplier;
    }

    protected Function<K, JrlHotKey<K>> hotKeySupplier;
    /**
     * 热key统计容量大小
     */
    protected int statisticCapacity = 100;

    /**
     * 热key统计起始值
     */
    protected int countLeastValue = 1000;

    /**
     * 读写锁
     */
    protected StampedLock stampedLock = new StampedLock();

    /**
     * key自增
     *
     * @param key
     */
    protected abstract void incr(K key);

    /**
     * 获取热key列表
     *
     * @return
     */
    public abstract List<JrlHotKey<K>> getHotkeys();

    /**
     * 清空
     */
    protected abstract void clean();

    /**
     * 获取热key
     *
     * @return
     */
    public List<JrlHotKey<K>> getHotKeysAndClean() {
        long writeLock = stampedLock.writeLock();
        try {
            List<JrlHotKey<K>> hotkeys = this.getHotkeys();
            if (CollectionUtils.isNotEmpty(hotkeys)) {
                this.clean();
            }
            return hotkeys;
        } finally {
            stampedLock.unlock(writeLock);
        }
    }

    /**
     * key自增
     *
     * @param key
     */
    public void hotKeyIncr(K key) {
        long readLock = stampedLock.readLock();
        try {
            this.incr(key);
        } finally {
            stampedLock.unlock(readLock);
        }
    }

    public int getCapacity() {
        return statisticCapacity;
    }

    public void setCapacity(int statisticCapacity) {
        this.statisticCapacity = statisticCapacity;
    }

    public int getCountLeastValue() {
        return countLeastValue;
    }

    public void setCountLeastValue(int countLeastValue) {
        this.countLeastValue = countLeastValue;
    }
}
