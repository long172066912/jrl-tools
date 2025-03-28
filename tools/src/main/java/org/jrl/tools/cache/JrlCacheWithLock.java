package org.jrl.tools.cache;

import java.io.Closeable;
import java.util.function.Supplier;

/**
 * 缓存接口，加锁方式
 *
 * @author JerryLong
 */
public interface JrlCacheWithLock<K, V> extends Closeable {

    /**
     * 设置缓存，并且加锁，保证数据一致性
     *
     * @param key   key
     * @param value 数据来源操作，将数据库操作放到Supplier中
     */
    void putWithLock(K key, Supplier<V> value);

    /**
     * 删除缓存，并且加锁，保证数据一致性
     * 本地缓存时，不会加锁，先执行runnable，再删本地缓存
     *
     * @param key      key
     * @param runnable 数据操作，将数据库操作放到Runnable中
     */
    void removeWithLock(K key, Runnable runnable);
}
