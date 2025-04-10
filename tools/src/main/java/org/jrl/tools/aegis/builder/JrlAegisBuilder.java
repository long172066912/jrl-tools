package org.jrl.tools.aegis.builder;

import org.jrl.tools.aegis.core.rule.JrlAegisBreakerRule;

/**
 * 神盾构建器
 *
 * @author JerryLong
 */
public class JrlAegisBuilder {

    /**
     * 构建神盾-限流
     *
     * @return
     */
    public static JrlAegisLimitBuilder limit() {
        return new JrlAegisLimitBuilder();
    }

    /**
     * 构建神盾-断路器（熔断/降级）
     *
     * @return
     */
    public static JrlAegisLocalBuilder<JrlAegisBreakerRule> breaker(String name) {
        return new JrlAegisLocalBuilder<>(name);
    }
}
