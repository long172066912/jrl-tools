package org.jrl.tools.utils.hotkey;

import java.util.concurrent.atomic.LongAdder;

/**
 * 热key统计对象
 *
 * @author JerryLong
 */
public interface JrlHotKey<K> {

    K getKey();

    LongAdder getCount();

    int intValue();

    boolean equals(Object o);
}
