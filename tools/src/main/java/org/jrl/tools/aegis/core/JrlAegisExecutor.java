package org.jrl.tools.aegis.core;

import org.jrl.tools.aegis.JrlAegisRule;

import java.io.Closeable;
import java.io.IOException;

public interface JrlAegisExecutor<E extends JrlAegisEntry, R extends JrlAegisRule> extends Closeable {
    /**
     * 尝试获取资源
     *
     * @return JrlAegisEntry
     */
    E tryAcquire();

    /**
     * 获取规则
     *
     * @return JrlAegisRule
     */
    R getRule();

    /**
     * 获取名称
     *
     * @return String
     */
    String getName();

    /**
     * 是否在有效期内
     *
     * @return boolean
     */
    default boolean inTime() {
        final JrlAegisRule rule = getRule();
        if (rule.endTime() <= 0) {
            return false;
        }
        final long l = System.currentTimeMillis();
        return rule.startTime() <= l && l < rule.endTime();
    }

    /**
     * 是否过期
     *
     * @return boolean
     */
    default boolean isExpired() {
        final JrlAegisRule rule = getRule();
        if (rule.endTime() <= 0) {
            return false;
        }
        final long l = System.currentTimeMillis();
        return l >= rule.endTime();
    }

    @Override
    default void close() throws IOException {

    }

    /**
     * 修改配置
     *
     * @param rule JrlAegisRule
     */
    void changeRule(R rule);

    abstract class AbstractJrlAegisExecutor<E extends JrlAegisEntry, R extends JrlAegisRule> implements JrlAegisExecutor<E, R> {
        protected final String name;
        protected R rule;

        public AbstractJrlAegisExecutor(String name, R rule) {
            this.name = alias() + ":" + name + "_" + rule.scope().getScope() + "_" + rule.id();
            this.rule = rule;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public R getRule() {
            return rule;
        }

        @Override
        public void close() throws IOException {

        }

        /**
         * 别名
         *
         * @return String
         */
        protected abstract String alias();
    }
}
