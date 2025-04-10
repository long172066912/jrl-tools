package org.jrl.tools.aegis.core.rule;

import org.jrl.tools.aegis.JrlAegisRule;
import org.jrl.tools.aegis.model.JrlAegisLimitType;
import org.jrl.tools.aegis.model.JrlAegisScope;
import org.jrl.tools.cache.extend.mesh.JrlCacheMeshConnectType;
import org.jrl.tools.condition.JrlCondition;
import org.jrl.tools.utils.JrlDateUtil;

/**
 * 限流规则
 *
 * @author JerryLong
 */
public class JrlAegisLimitRule implements JrlAegisRule {
    private String resource;
    private JrlCacheMeshConnectType connectType;
    private final JrlAegisLimitType type;
    private final int count;
    private final int timeWindow;
    private final int priority;
    private final long startTime;
    private final long endTime;
    private final JrlAegisScope scope;
    private JrlCondition condition;

    protected JrlAegisLimitRule(JrlAegisLimitType type, int count, int timeWindow, int priority, long startTime, long endTime, JrlAegisScope scope) {
        this.type = type;
        this.count = count;
        this.timeWindow = timeWindow;
        this.priority = priority;
        this.startTime = startTime;
        this.endTime = endTime;
        this.scope = scope;
    }

    @Override
    public int id() {
        return priority;
    }

    @Override
    public JrlAegisLimitType type() {
        return type;
    }

    @Override
    public JrlAegisScope scope() {
        return scope;
    }

    public int timeWindow() {
        return timeWindow;
    }

    public int count() {
        return count;
    }

    @Override
    public long startTime() {
        return startTime;
    }

    @Override
    public long endTime() {
        return endTime;
    }

    @Override
    public JrlCondition condition() {
        return condition;
    }

    public void setCondition(JrlCondition condition) {
        this.condition = condition;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public JrlCacheMeshConnectType getConnectType() {
        return connectType;
    }

    public void setConnectType(JrlCacheMeshConnectType connectType) {
        this.connectType = connectType;
    }

    @Override
    public String toString() {
        return "limitRule [ priority=" + id() +
                ", type=" + type() +
                ", scope=" + scope() +
                ", timeWindow=" + timeWindow() +
                ", count=" + count() +
                ", startTime=" + startTime() +
                ", endTime=" + endTime() + "]";
    }


    /**
     * builder
     *
     * @return
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        protected JrlAegisLimitType type;
        protected Integer count;
        protected Integer timeWindow;
        protected Integer priority;
        protected Long startTime;
        protected Long endTime;
        protected JrlAegisScope scope;
        protected JrlCondition condition;

        public Builder() {
        }


        public Builder type(JrlAegisLimitType type) {
            this.type = type;
            return this;
        }

        public Builder count(int count) {
            this.count = count;
            return this;
        }

        public Builder timeWindow(int timeWindow) {
            this.timeWindow = timeWindow;
            return this;
        }

        public Builder id(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder scope(JrlAegisScope scope) {
            this.scope = scope;
            return this;
        }

        public Builder startTime(long startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder startDate(String startDate) {
            this.startTime = JrlDateUtil.date2TimeStamp(startDate, "yyyy-MM-dd HH:mm:ss");
            return this;
        }

        public Builder endTime(long endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder endDate(String endDate) {
            this.endTime = JrlDateUtil.date2TimeStamp(endDate, "yyyy-MM-dd HH:mm:ss");
            return this;
        }

        public Builder condition(JrlCondition condition) {
            this.condition = condition;
            return this;
        }

        public JrlAegisLimitRule build() {
            type = null == type ? JrlAegisLimitType.QPS : type;
            final JrlAegisLimitRule rule = new JrlAegisLimitRule(
                    type,
                    null == count ? 100 : count,
                    null == timeWindow ? 60 : timeWindow,
                    null == priority ? 0 : priority,
                    null == startTime ? -1 : startTime,
                    null == endTime ? Long.MAX_VALUE : endTime,
                    null == scope ? JrlAegisScope.GLOBAL : scope
            );
            rule.setCondition(condition);
            return rule;
        }
    }
}
