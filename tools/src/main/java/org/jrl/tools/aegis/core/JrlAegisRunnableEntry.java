package org.jrl.tools.aegis.core;

/**
 * 神盾处理实体，通过end结束计数
 *
 * @author JerryLong
 */
public class JrlAegisRunnableEntry<V> implements JrlAegisEntry {
    private final JrlAegisEntry entry;
    private final V data;
    private final Throwable error;

    public JrlAegisRunnableEntry(JrlAegisEntry entry, V data, Throwable error) {
        this.entry = entry;
        this.data = data;
        this.error = error;
    }

    @Override
    public void end() {
        if (null != this.entry) {
            if (null != error) {
                this.entry.end(error);
            } else {
                this.entry.end();
            }
        }
    }

    /**
     * 应该用内部执行的异常进行end，而不是用外部传入的
     *
     * @param error 异常
     */
    @Override
    @Deprecated
    public void end(Throwable error) {
        if (null != this.entry) {
            this.entry.end(error);
        }
    }

    public V getData() {
        if (null != error) {
            throw new RuntimeException(error);
        }
        return data;
    }
}
