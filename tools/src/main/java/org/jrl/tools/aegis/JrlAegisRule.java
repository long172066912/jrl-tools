package org.jrl.tools.aegis;

import org.jrl.tools.aegis.model.JrlAegisScope;
import org.jrl.tools.condition.JrlCondition;

/**
 * 神盾规则接口
 *
 * @author JerryLong
 */
public interface JrlAegisRule {

    /**
     * id对应一个规则
     * 同一时间多个规则时，大的执行
     *
     * @return
     */
    int id();

    /**
     * 限流类型，计数器、滑动窗口、令牌桶、漏斗
     *
     * @return
     */
    JrlAegisType type();

    /**
     * 作用域，支持自定义
     * 每一个作用域取优先级最高的规则执行
     *
     * @return
     */
    JrlAegisScope scope();

    /**
     * 条件，满足条件才执行
     *
     * @return 条件列表
     */
    default JrlCondition condition() {
        return null;
    }

    /**
     * 开始时间，默认永久有效
     *
     * @return 时间戳
     */
    default long startTime() {
        return -1;
    }

    /**
     * 结束时间，默认永久有效
     *
     * @return 时间戳
     */
    default long endTime() {
        return -1;
    }
}
