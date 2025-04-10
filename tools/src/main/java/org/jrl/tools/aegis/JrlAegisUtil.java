package org.jrl.tools.aegis;

import org.jrl.tools.aegis.builder.JrlAegisBuilder;
import org.jrl.tools.aegis.builder.JrlAegisLimitBuilder;
import org.jrl.tools.aegis.builder.JrlAegisLocalBuilder;
import org.jrl.tools.aegis.core.AbstractJrlAegis;
import org.jrl.tools.aegis.core.rule.JrlAegisBreakerRule;
import org.jrl.tools.aegis.core.rule.JrlAegisLimitRule;
import org.jrl.tools.aegis.manager.JrlAegisManager;
import org.jrl.tools.aegis.model.JrlAegisLimitType;
import org.jrl.tools.aegis.model.JrlAegisScope;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * 神盾
 *
 * @author JerryLong
 */
public class JrlAegisUtil {

    /**
     * 获取神盾处理器
     *
     * @param name key
     * @return jrl-aegis
     */
    public static JrlAegis getAegis(String name) {
        return JrlAegisManager.getAegis(name);
    }

    /**
     * 获取神盾处理器
     *
     * @param name     key
     * @param supplier 默认构造器
     * @return jrl-aegis
     */
    public static JrlAegis getAegis(String name, Supplier<JrlAegis> supplier) {
        final JrlAegis aegis = getAegis(name);
        if (null != aegis) {
            return aegis;
        }
        return supplier.get();
    }

    /**
     * 构建熔断器
     *
     * @return 熔断器构造器
     */
    public static JrlAegisLocalBuilder<JrlAegisBreakerRule> breaker(String name) {
        return JrlAegisBuilder.breaker(name);
    }

    /**
     * 熔断器-异常次数
     *
     * @param name         key
     * @param statMs       统计时间，单位：毫秒
     * @param minRequest   最小请求
     * @param blockSeconds 熔断时间，单位：秒
     * @param maxCount     最大异常次数
     * @return jrl-aegis
     */
    public static JrlAegis blockByExceptionCount(String name, int statMs, int minRequest, int blockSeconds, int maxCount) {
        final AbstractJrlAegis aegis = JrlAegisManager.getAegis(name);
        if (null != aegis) {
            return aegis;
        }
        return JrlAegisBuilder.breaker(name)
                .addRule(JrlAegisBreakerRule.builder().exceptionCount().minExceptionCount(maxCount).timeWindow(blockSeconds).stats(statMs, minRequest).build())
                .build();
    }

    /**
     * 熔断器-异常比例
     *
     * @param name         key
     * @param statMs       统计时间，单位：毫秒
     * @param minRequest   最小请求
     * @param blockSeconds 熔断时间，单位：秒
     * @param maxRadio     最大异常比例
     * @return jrl-aegis
     */
    public static JrlAegis blockByExceptionRadio(String name, int statMs, int minRequest, int blockSeconds, double maxRadio) {
        final AbstractJrlAegis aegis = JrlAegisManager.getAegis(name);
        if (null != aegis) {
            return aegis;
        }
        return JrlAegisBuilder.breaker(name)
                .addRule(JrlAegisBreakerRule.builder().exceptionRadio().minExceptionRadio(maxRadio).timeWindow(blockSeconds).stats(statMs, minRequest).build())
                .build();
    }

    /**
     * 熔断器-慢请求比例
     *
     * @param name         key
     * @param statMs       统计时间，单位：毫秒
     * @param minRequest   最小请求
     * @param blockSeconds 熔断时间，单位：秒
     * @param maxRadio     最大慢请求比例
     * @param slowCountMs  慢请求时间，单位：毫秒
     * @return jrl-aegis
     */
    public static JrlAegis blockBySlowRadio(String name, int statMs, int minRequest, int blockSeconds, int maxRadio, int slowCountMs) {
        final AbstractJrlAegis aegis = JrlAegisManager.getAegis(name);
        if (null != aegis) {
            return aegis;
        }
        return JrlAegisBuilder.breaker(name)
                .addRule(JrlAegisBreakerRule.builder().slowRadio().minSlowRadio(maxRadio).minCallMs(slowCountMs).timeWindow(blockSeconds).stats(statMs, minRequest).build())
                .build();
    }

    /**
     * 构建限流器
     *
     * @return 限流器构造器
     */
    public static JrlAegisLimitBuilder limit() {
        return JrlAegisBuilder.limit();
    }

    /**
     * 限流器-QPS
     *
     * @param name        key
     * @param qpsMaxCount 每秒最大请求
     * @return jrl-aegis
     */
    public static JrlAegis limitQps(String name, int qpsMaxCount) {
        final AbstractJrlAegis aegis = JrlAegisManager.getAegis(name);
        if (null != aegis) {
            return aegis;
        }
        return limit().local(name)
                .addRule(JrlAegisLimitRule.builder().count(qpsMaxCount).type(JrlAegisLimitType.QPS).build())
                .build();
    }

    /**
     * 限流器-QPS-分布式（默认redis）
     *
     * @param name        key
     * @param resource    资源
     * @param qpsMaxCount 每秒最大请求
     * @return
     */
    public static JrlAegis limitQpsMesh(String name, String resource, int qpsMaxCount) {
        final AbstractJrlAegis aegis = JrlAegisManager.getAegis(name);
        if (null != aegis) {
            return aegis;
        }
        return limit().mesh(name, resource)
                .addRule(JrlAegisLimitRule.builder().count(qpsMaxCount).type(JrlAegisLimitType.QPS).build())
                .build();
    }

    /**
     * 限流器-时间窗口
     *
     * @param name       key
     * @param maxCount   最大数量
     * @param timeWindow 时间窗口，单位：秒
     * @return jrl-aegis
     */
    public static JrlAegis limitTime(String name, int maxCount, int timeWindow) {
        final AbstractJrlAegis aegis = JrlAegisManager.getAegis(name);
        if (null != aegis) {
            return aegis;
        }
        return limit().local(name)
                .addRule(JrlAegisLimitRule.builder().count(maxCount).timeWindow(timeWindow).type(JrlAegisLimitType.TIME_WINDOW).build())
                .build();
    }

    /**
     * 限流器-时间窗口-分布式（默认redis）
     *
     * @param name       key
     * @param maxCount   最大数量
     * @param timeWindow 时间窗口，单位：秒
     * @return jrl-aegis
     */
    public static JrlAegis limitTimeMesh(String name, String resource, int maxCount, int timeWindow) {
        final AbstractJrlAegis aegis = JrlAegisManager.getAegis(name);
        if (null != aegis) {
            return aegis;
        }
        return limit().mesh(name, resource)
                .addRule(JrlAegisLimitRule.builder().count(maxCount).timeWindow(timeWindow).type(JrlAegisLimitType.TIME_WINDOW).build())
                .build();
    }

    /**
     * 限流器-时间窗口-分布式（默认redis）
     * 检测配置是否变更，如果变更重新生成限流器
     *
     * @param name       key
     * @param resource   资源
     * @param scope      作用域
     * @param ruleId     规则id
     * @param maxCount   最大数量
     * @param timeWindow 时间窗口，单位：秒
     * @return jrl-aegis
     */
    public static JrlAegis limitTimeMeshDynamicConfig(String name, String resource, JrlAegisScope scope, int ruleId, int maxCount, int timeWindow) {
        final AbstractJrlAegis aegis = JrlAegisManager.getAegis(name);
        if (null != aegis) {
            //判断配置是否有变化
            AtomicBoolean isChange = new AtomicBoolean(false);
            aegis.getRules().forEach((s, rules) -> {
                if (s.equals(scope)) {
                    ((List<JrlAegisLimitRule>) rules).forEach(rule -> {
                        if (rule.id() == ruleId) {
                            isChange.set(rule.count() != maxCount || rule.timeWindow() != timeWindow);
                        }
                    });
                }
            });
            if (isChange.get()) {
                aegis.changeRule(JrlAegisLimitRule.builder().scope(scope).id(ruleId).count(maxCount).timeWindow(timeWindow).type(JrlAegisLimitType.TIME_WINDOW).build());
            }
            return aegis;
        }
        return limit().mesh(name, resource)
                .addRule(JrlAegisLimitRule.builder().scope(scope).id(ruleId).count(maxCount).timeWindow(timeWindow).type(JrlAegisLimitType.TIME_WINDOW).build())
                .build();
    }

    /**
     * 限流器-线程数
     *
     * @param name     key
     * @param maxCount 最大数量
     * @return jrl-aegis
     */
    public static JrlAegis limitThread(String name, int maxCount) {
        final AbstractJrlAegis aegis = JrlAegisManager.getAegis(name);
        if (null != aegis) {
            return aegis;
        }
        return limit().local(name)
                .addRule(JrlAegisLimitRule.builder().count(maxCount).type(JrlAegisLimitType.THREAD).build())
                .build();
    }

    /**
     * 限流器-线程数-分布式（默认redis）
     *
     * @param name     key
     * @param maxCount 最大数量
     * @return jrl-aegis
     */
    public static JrlAegis limitThreadMesh(String name, String resource, int maxCount) {
        final AbstractJrlAegis aegis = JrlAegisManager.getAegis(name);
        if (null != aegis) {
            return aegis;
        }
        return limit().mesh(name, resource)
                .addRule(JrlAegisLimitRule.builder().count(maxCount).type(JrlAegisLimitType.THREAD).build())
                .build();
    }
}
