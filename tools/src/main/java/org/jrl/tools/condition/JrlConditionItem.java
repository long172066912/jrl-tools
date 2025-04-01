package org.jrl.tools.condition;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 具体的条件单元
 *
 * @author JerryLong
 */
public class JrlConditionItem {
    /**
     * 条件key
     */
    private final String key;
    /**
     * 条件value
     */
    private final Set<Object> values;
    /**
     * 条件是否必须
     */
    private final boolean must;
    /**
     * 条件匹配类型
     */
    private final MatchType matchType;

    public JrlConditionItem(String key, Set<Object> values, boolean must, MatchType matchType) {
        this.key = key;
        this.values = values;
        this.must = must;
        this.matchType = matchType;
    }

    public String getKey() {
        return key;
    }

    public Set<Object> getValues() {
        return values;
    }

    public boolean isMust() {
        return must;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String key;
        private final Set<Object> values = new HashSet<>();
        private MatchType matchType = MatchType.EQUAL;
        private boolean must = false;

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder value(Object value) {
            this.values.add(value);
            return this;
        }

        public Builder values(Object... values) {
            if (values.length > 0) {
                this.values.addAll(Arrays.asList(values));
            }
            return this;
        }

        public Builder matchType(MatchType matchType) {
            this.matchType = matchType;
            return this;
        }

        public Builder must() {
            this.must = true;
            return this;
        }

        public JrlConditionItem build() {
            return new JrlConditionItem(key, values, must, matchType);
        }
    }

    public enum MatchType {
        /**
         * 等于
         */
        EQUAL,
        /**
         * 不等于
         */
        NOT_EQUAL,
    }
}
