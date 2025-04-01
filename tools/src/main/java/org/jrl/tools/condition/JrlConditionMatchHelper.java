package org.jrl.tools.condition;

import org.jrl.tools.condition.model.JrlConditionType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;

/**
 * 条件匹配帮助类
 *
 * @author JerryLong
 */
public class JrlConditionMatchHelper {
    /**
     * 条件匹配
     *
     * @param condition 条件
     * @param request   请求
     * @return 是否匹配
     */
    public static MatchResult match(JrlCondition condition, Map<String, Object> request) {
        if (condition == null) {
            return MatchResult.success();
        }
        //获取条件
        final List<JrlConditionItem> items = condition.getCondition();
        final JrlConditionType type = condition.getType();
        boolean status = true;
        int count = 0;
        for (JrlConditionItem item : items) {
            final Object v = MapUtils.getObject(request, item.getKey());
            if (null == v) {
                if (item.isMust()) {
                    return MatchResult.fail("The request parameter cannot be empty ! " + item.getKey());
                }
                //如果不是必须的，则跳过
                continue;
            }
            count++;
            switch (item.getMatchType()) {
                case NOT_EQUAL:
                    if (CollectionUtils.isNotEmpty(item.getValues()) && item.getValues().contains(v)) {
                        status = false;
                    }
                    break;
                case EQUAL:
                    if (CollectionUtils.isNotEmpty(item.getValues()) && !item.getValues().contains(v)) {
                        status = false;
                    }
                default:
                    break;
            }
            //如果是and条件，并且不匹配，则抛出异常
            if (!status && type == JrlConditionType.AND) {
                return MatchResult.fail("The request parameter does not meet the conditions ! " + item.getKey());
            }
            //如果是or条件，并且匹配，则直接返回
            if (status && type == JrlConditionType.OR) {
                return MatchResult.success();
            }
        }
        if (count == 0) {
            return MatchResult.fail("The request parameter does not meet the conditions ! ");
        }
        return MatchResult.success();
    }

    public static class MatchResult {
        private final boolean status;
        private final String message;

        protected MatchResult(boolean status, String message) {
            this.status = status;
            this.message = message;
        }

        public boolean isMatch() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        protected static MatchResult success() {
            return new MatchResult(true, null);
        }

        protected static MatchResult fail(String message) {
            return new MatchResult(false, message);
        }
    }
}
