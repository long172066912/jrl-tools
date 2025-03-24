package org.jrl.tools.utils.hotkey;

import java.io.Serializable;
import java.util.concurrent.atomic.LongAdder;

/**
 * 热key统计对象
 *
 * @author JerryLong
 */
public class DefaultJrlHotKey<K> implements Serializable, JrlHotKey<K> {

    public DefaultJrlHotKey(K key) {
        this.key = key;
        this.count = new LongAdder();
    }

    private K key;

    private LongAdder count;

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public LongAdder getCount() {
        return count;
    }

    @Override
    public int intValue() {
        return count.intValue();
    }
}
