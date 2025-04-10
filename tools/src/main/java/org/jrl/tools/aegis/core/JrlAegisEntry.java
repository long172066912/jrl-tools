package org.jrl.tools.aegis.core;

/**
 * 神盾处理实体，通过end结束计数
 *
 * @author JerryLong
 */
public interface JrlAegisEntry extends AutoCloseable {

    /**
     * 结束
     */
    void end();

    /**
     * 异常结束
     *
     * @param error 异常
     */
    void end(Throwable error);

    @Override
    default void close() throws Exception {
        end();
    }

    default void close(Throwable error) {
        end(error);
    }
}
