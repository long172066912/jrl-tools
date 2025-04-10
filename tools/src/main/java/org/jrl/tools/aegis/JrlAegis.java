package org.jrl.tools.aegis;

import org.jrl.tools.aegis.core.JrlAegisEntry;
import org.jrl.tools.aegis.core.JrlAegisRunnableEntry;
import org.jrl.tools.aegis.exception.JrlAegisException;
import org.jrl.tools.aegis.model.JrlAegisResourceType;
import org.jrl.tools.utils.function.AbstractJrlCallable;

/**
 * jrl 神盾
 *
 * @author JerryLong
 */
public interface JrlAegis {

    /**
     * 获取名称
     *
     * @return 名称
     */
    String getName();

    /**
     * 获取资源类型
     *
     * @return 本地 or 分布式
     */
    JrlAegisResourceType getResourceType();

    /**
     * 尝试获取令牌，如果被拒绝了返回false
     * 不适用于并发限流
     * 不支持mock，mock请使用{@linkplain JrlAegis#tryAcquire(AbstractJrlCallable)}
     *
     * @return true：获取成功，false：获取失败
     */
    boolean tryAcquire();

    /**
     * 尝试获取令牌，如果被拒绝了，抛出异常
     * 不支持mock，mock请使用{@linkplain JrlAegis#tryEntry(AbstractJrlCallable)}
     * ！！！注意，必须正确释放JrlAegisEntry
     *
     * @return
     */
    JrlAegisEntry tryEntry() throws JrlAegisException;

    /**
     * 尝试获取令牌，如果被拒绝了，抛出异常
     * 支持mock
     *
     * @param runnable 业务被神盾保护的逻辑
     * @return 保护成功时的业务执行返回值
     * @throws JrlAegisException 异常
     */
    <V> V tryAcquire(AbstractJrlCallable<V> runnable) throws JrlAegisException;

    /**
     * 尝试获取令牌，如果被拒绝了，抛出异常
     * 支持mock
     * ！！！注意，必须正确释放JrlAegisEntry
     *
     * @param runnable 业务被神盾保护的逻辑
     * @return 保护成功时的业务执行返回值
     * @throws JrlAegisException 异常
     */
    <V> JrlAegisRunnableEntry<V> tryEntry(AbstractJrlCallable<V> runnable) throws JrlAegisException;

    /**
     * 开启限流mock
     */
    default void openBlockMock() {
    }

    /**
     * 关闭限流mock
     */
    default void closeBlockMock() {
    }
}
