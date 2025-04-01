package org.jrl.tools.condition;

import org.jrl.tools.condition.model.JrlConditionType;

import java.util.ArrayList;
import java.util.List;

/**
 * 条件接口
 *
 * @author JerryLong
 */
public class JrlCondition {
    /**
     * 条件
     */
    private final List<JrlConditionItem> conditions;
    /**
     * 多个条件匹配类型
     */
    private final JrlConditionType type;

    protected JrlCondition(List<JrlConditionItem> conditions, JrlConditionType type) {
        this.conditions = conditions;
        this.type = type;
    }

    /**
     * 条件项
     *
     * @return
     */
    public List<JrlConditionItem> getCondition() {
        return conditions;
    }

    public JrlConditionType getType() {
        return type;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<JrlConditionItem> items = new ArrayList<>();

        public Builder item(JrlConditionItem item) {
            this.items.add(item);
            return this;
        }

        public Builder2 and() {
            return new Builder2(JrlConditionType.AND);
        }

        public Builder2 or() {
            return new Builder2(JrlConditionType.OR);
        }

        public JrlCondition build() {
            return new JrlCondition(items, null);
        }
    }

    public static class Builder2 {
        private final List<JrlConditionItem> items = new ArrayList<>();
        private final JrlConditionType type;

        public Builder2(JrlConditionType type) {
            this.type = type;
        }

        public Builder2 item(JrlConditionItem item) {
            this.items.add(item);
            return this;
        }

        public JrlCondition build() {
            return new JrlCondition(items, type);
        }
    }
}
