package org.jrl.tools.thread.core.merge;

import org.jrl.tools.thread.api.JrlMergeThreadExecutor;

/**
* 构造器
* @author JerryLong
*/
public class JrlMergeRuleBuilder {
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private final JrlMergeThreadExecutor.JrlMergeRule<T> rule;

        public Builder() {
            rule = new DefaultJrlMergeRule<>();
        }

        public Builder<T> onTime(int time) {
            rule.onTime(time);
            return this;
        }

        public Builder<T> onCount(int count) {
            rule.onCount(count);
            return this;
        }

        public Builder<T> queueLength(int length) {
            rule.queueLength(length);
            return this;
        }

        public JrlMergeThreadExecutor.JrlMergeRule<T> build() {
            return rule;
        }
    }
}
