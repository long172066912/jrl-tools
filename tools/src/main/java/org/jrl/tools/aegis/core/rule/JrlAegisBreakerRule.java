package org.jrl.tools.aegis.core.rule;

import org.jrl.tools.aegis.JrlAegisRule;
import org.jrl.tools.aegis.model.JrlAegisBreakerType;
import org.jrl.tools.aegis.model.JrlAegisScope;
import org.jrl.tools.condition.JrlCondition;
import org.jrl.tools.utils.JrlDateUtil;

/**
 * 断路器规则
 *
 * @author JerryLong
 */
public class JrlAegisBreakerRule implements JrlAegisRule {
    private String resource;
    private final JrlAegisBreakerType type;
    /**
     * 类型是异常比例/类型是慢调用比例时，count则是小于1的小数
     */
    private final double count;
    /**
     * 熔断周期，秒
     */
    private final int timeWindow;
    /**
     * 统计时间，毫秒
     */
    private final int statWindowMs;
    /**
     * 最小请求数
     */
    private final int minRequest;
    private final int priority;
    private final long startTime;
    private final long endTime;
    private final JrlAegisScope scope;
    private JrlCondition condition;
    /**
     * 慢调用阈值，默认2000ms
     */
    private final double slowCount;

    protected JrlAegisBreakerRule(String resource, JrlAegisBreakerType type, double count, int timeWindow, int statWindowMs, int minRequest, int priority, long startTime, long endTime, JrlAegisScope scope, JrlCondition condition, double slowCount) {
        this.resource = resource;
        this.type = type;
        this.count = count;
        this.timeWindow = timeWindow;
        this.statWindowMs = statWindowMs;
        this.minRequest = minRequest;
        this.priority = priority;
        this.startTime = startTime;
        this.endTime = endTime;
        this.scope = scope;
        this.condition = condition;
        this.slowCount = slowCount;
    }

    @Override
    public int id() {
        return priority;
    }

    @Override
    public JrlAegisBreakerType type() {
        return type;
    }

    @Override
    public JrlAegisScope scope() {
        return scope;
    }

    public int timeWindow() {
        return timeWindow;
    }

    public int getStatWindowMs() {
        return statWindowMs;
    }

    public int getMinRequest() {
        return minRequest;
    }

    public double count() {
        return count;
    }

    public double getSlowCount() {
        return slowCount;
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

    @Override
    public String toString() {
        return "breakerRule [ priority=" + id() +
                ", type=" + type() +
                ", scope=" + scope() +
                ", count=" + count() +
                ", timeWindow=" + timeWindow() +
                ", statWindowMs=" + statWindowMs +
                ", minRequest=" + minRequest +
                ", slowCount=" + slowCount +
                ", startTime=" + startTime() +
                ", endTime=" + endTime() + "]";
    }


    public static BreakerBuilder builder() {
        return new BreakerBuilder();
    }

    public static class BreakerBuilder {
        public ExceptionCount.Builder exceptionCount() {
            return ExceptionCount.builder();
        }

        public ExceptionRadio.Builder exceptionRadio() {
            return ExceptionRadio.builder();
        }

        public SlowRadio.Builder slowRadio() {
            return SlowRadio.builder();
        }

        public DegradeBuilder degrade() {
            return new DegradeBuilder();
        }
    }

    public static abstract class AbstractBuilder {
        private String resource;
        private int priority;
        private long startTime;
        private long endTime;
        private JrlAegisScope scope;
        private JrlCondition condition;
        protected ExceptionCount exceptionCount;
        protected ExceptionRadio exceptionRadio;
        protected SlowRadio slowRadio;
        private Stats stats;
        private int timeWindow;

        public AbstractBuilder() {
        }

        public AbstractBuilder resource(String resource) {
            this.resource = resource;
            return this;
        }

        public AbstractBuilder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public AbstractBuilder startTime(long startTime) {
            this.startTime = startTime;
            return this;
        }

        public AbstractBuilder startDate(String startDate) {
            this.startTime = JrlDateUtil.date2TimeStamp(startDate, "yyyy-MM-dd HH:mm:ss");
            return this;
        }

        public AbstractBuilder endTime(long endTime) {
            this.endTime = endTime;
            return this;
        }

        public AbstractBuilder endDate(String endDate) {
            this.endTime = JrlDateUtil.date2TimeStamp(endDate, "yyyy-MM-dd HH:mm:ss");
            return this;
        }

        public AbstractBuilder condition(JrlCondition condition) {
            this.condition = condition;
            return this;
        }

        public AbstractBuilder scope(JrlAegisScope scope) {
            this.scope = scope;
            return this;
        }

        public AbstractBuilder stats(Stats stats) {
            this.stats = stats;
            return this;
        }

        /**
         * 统计时间，毫秒
         *
         * @param statMs     统计时间，毫秒
         * @param minRequest 最小请求数
         * @return
         */
        public AbstractBuilder stats(int statMs, int minRequest) {
            this.stats = new Stats(statMs, minRequest);
            return this;
        }

        public AbstractBuilder timeWindow(int timeWindow) {
            this.timeWindow = timeWindow;
            return this;
        }

        protected abstract JrlAegisBreakerType getType();

        public JrlAegisBreakerRule build() {
            if (null == stats) {
                stats = new Stats(1000, 10);
            }
            if (null == scope) {
                scope = JrlAegisScope.GLOBAL;
            }
            double count = 0;
            int timeWindow = 0 == this.timeWindow ? 5 : this.timeWindow;
            int statWindowMs = 0 == stats.getStatWindowMs() ? 1000 : stats.getStatWindowMs();
            int minRequest = 0 == stats.getMinRequest() ? 1 : stats.getMinRequest();
            int slowCount = 2000;
            switch (getType()) {
                case EXCEPTION_COUNT:
                    count = exceptionCount.getCount();
                    break;
                case EXCEPTION_RATIO:
                    count = exceptionRadio.getCount();
                    break;
                case SLOW_RATIO:
                    count = slowRadio.getCount();
                    slowCount = slowRadio.getSlowMs();
                    break;
                case DEGRADE:
                default:
                    //默认不处理
                    break;
            }
            this.startTime = 0 == startTime ? -1 : startTime;
            this.endTime = 0 == endTime ? Long.MAX_VALUE : endTime;
            return new JrlAegisBreakerRule(resource, getType(), count, timeWindow, statWindowMs, minRequest, priority, startTime, endTime, scope, condition, slowCount);
        }
    }

    public static class Stats {
        private final int statWindowMs;
        private final int minRequest;

        protected Stats(int statWindowMs, int minRequest) {
            this.statWindowMs = statWindowMs;
            this.minRequest = minRequest;
        }

        public int getStatWindowMs() {
            return statWindowMs;
        }

        public int getMinRequest() {
            return minRequest;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private int statWindowMs;
            private int minRequest;

            public Builder() {
            }

            public Builder statWindowMs(int statWindowMs) {
                this.statWindowMs = statWindowMs;
                return this;
            }

            public Builder minRequest(int minRequest) {
                this.minRequest = minRequest;
                return this;
            }

            public Stats build() {
                if (0 == statWindowMs) {
                    statWindowMs = 1000;
                }
                if (0 == minRequest) {
                    minRequest = 1;
                }
                return new Stats(statWindowMs, minRequest);
            }
        }
    }

    /**
     * 异常次数
     */
    public static class ExceptionCount {
        private final int count;

        protected ExceptionCount(int count) {
            this.count = count;
        }

        public int getCount() {
            return count;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder extends AbstractBuilder {
            private int count;

            public Builder() {
            }

            @Override
            protected JrlAegisBreakerType getType() {
                return JrlAegisBreakerType.EXCEPTION_COUNT;
            }

            public Builder minExceptionCount(int count) {
                this.count = count;
                return this;
            }

            @Override
            public JrlAegisBreakerRule build() {
                super.exceptionCount = new ExceptionCount(count);
                return super.build();
            }
        }
    }

    /**
     * 异常次数
     */
    public static class ExceptionRadio {
        private final double count;

        protected ExceptionRadio(double count) {
            this.count = count;
        }

        public double getCount() {
            return count;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder extends AbstractBuilder {
            private double count;
            private int timeWindow;

            public Builder() {
            }

            @Override
            protected JrlAegisBreakerType getType() {
                return JrlAegisBreakerType.EXCEPTION_RATIO;
            }

            public Builder minExceptionRadio(double radio) {
                this.count = radio;
                return this;
            }

            @Override
            public JrlAegisBreakerRule build() {
                super.exceptionRadio = new ExceptionRadio(count);
                return super.build();
            }
        }
    }

    /**
     * 异常次数
     */
    public static class SlowRadio {
        private final double count;
        /**
         * 慢请求阈值，毫秒
         */
        private final int slowMs;

        protected SlowRadio(double count, int slowMs) {
            this.count = count;
            this.slowMs = slowMs;
        }

        public double getCount() {
            return count;
        }

        public int getSlowMs() {
            return slowMs;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder extends AbstractBuilder {
            private double count;
            private int slowMs;

            public Builder() {
            }

            public Builder minSlowRadio(double radio) {
                this.count = radio;
                return this;
            }

            @Override
            protected JrlAegisBreakerType getType() {
                return JrlAegisBreakerType.SLOW_RATIO;
            }

            public Builder minCallMs(int slowMs) {
                this.slowMs = slowMs;
                return this;
            }

            @Override
            public JrlAegisBreakerRule build() {
                super.slowRadio = new SlowRadio(count, slowMs);
                return super.build();
            }
        }
    }

    /**
     * 降级
     */
    public static class DegradeBuilder extends AbstractBuilder {
        @Override
        protected JrlAegisBreakerType getType() {
            return JrlAegisBreakerType.DEGRADE;
        }
    }
}
