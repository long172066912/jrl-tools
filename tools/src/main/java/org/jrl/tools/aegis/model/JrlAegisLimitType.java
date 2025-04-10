package org.jrl.tools.aegis.model;

import org.jrl.tools.aegis.JrlAegisType;

/**
 * 限流类型
 *
 * @author JerryLong
 */
public enum JrlAegisLimitType implements JrlAegisType {
    /**
     * 限流-qps
     */
    QPS(LIMIT, 1),
    /**
     * 限流-时间滑动窗口
     */
    TIME_WINDOW(LIMIT, 2),
    /**
     * 限流-线程并发
     */
    THREAD(LIMIT, 3),
    ;

    /**
     * 行为处理方式，1限流，2熔断，3降级
     */
    private final int action;
    private final int type;

    JrlAegisLimitType(int action, int type) {
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
