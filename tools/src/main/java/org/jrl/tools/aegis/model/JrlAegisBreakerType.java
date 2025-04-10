package org.jrl.tools.aegis.model;

import org.jrl.tools.aegis.JrlAegisType;

/**
 * 断路器类型
 *
 * @author JerryLong
 */
public enum JrlAegisBreakerType implements JrlAegisType {
    /**
     * 异常数量
     */
    EXCEPTION_COUNT(BREAKER, 1),
    /**
     * 异常比例
     */
    EXCEPTION_RATIO(BREAKER, 2),
    /**
     * 慢请求比例
     */
    SLOW_RATIO(BREAKER, 3),
    /**
     * 降级（配置立即生效，因此需要通过配置中心扩展，支持动态开启关闭）
     */
    DEGRADE(BREAKER, 4),
    ;

    /**
     * 行为处理方式，1限流，2熔断，3降级
     */
    private final int action;
    private final int type;

    JrlAegisBreakerType(int action, int type) {
        this.action = action;
        this.type = type;
    }

    @Override
    public int getAction() {
        return action;
    }

    @Override
    public int getType() {
        return type;
    }
}
