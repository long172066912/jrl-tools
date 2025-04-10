package org.jrl.tools.aegis.core.executor.extensions.sentinel;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.jrl.tools.aegis.core.rule.JrlAegisBreakerRule;
import org.jrl.tools.aegis.exception.JrlAegisBreakerException;
import org.jrl.tools.aegis.exception.JrlAegisException;
import org.jrl.tools.aegis.spi.JrlAegisExecutorSpi;
import org.jrl.tools.log.JrlLoggerFactory;
import org.jrl.tools.spi.JrlSpiGroup;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 本地默认实现
 *
 * @author JerryLong
 */
@JrlSpiGroup("local-breaker")
public class JrlAegisSentinelBreakerExtends implements JrlAegisExecutorSpi<JrlAegisSentinelEntry, JrlAegisBreakerRule> {
    private static final Logger LOGGER = JrlLoggerFactory.getLogger(JrlAegisSentinelBreakerExtends.class);
    private static final Set<String> DEGRADE_RULES = new HashSet<>();
    private static final int ZEUS_AEGIS_BREAKER_RESOURCE_TYPE_ID = 2000000;

    @Override
    public void init(String name, JrlAegisBreakerRule rule) {
        this.load(name, rule);
    }

    @Override
    public JrlAegisSentinelEntry tryAcquire(String name, JrlAegisBreakerRule rule) throws JrlAegisException {
        try {
            //判断服务是否被降级
            if (DEGRADE_RULES.contains(name)) {
                throw new DegradeException("zeus-breaker-sentinel DEGRADE ! name : " + name);
            }
            return new JrlAegisSentinelEntry(name, SphU.asyncEntry(name, ZEUS_AEGIS_BREAKER_RESOURCE_TYPE_ID, EntryType.OUT), rule.type());
        } catch (BlockException e) {
            LOGGER.warn("zeus-aegis breaker-sentinel block ! name : {} , rule : {}", name, rule);
            throw new JrlAegisBreakerException(name, rule);
        }
    }

    @Override
    public void load(String name, JrlAegisBreakerRule rule) {
        DegradeRule sentinelRule = new DegradeRule(name);
        sentinelRule.setId(Long.parseLong(rule.id() + ""));
        sentinelRule
                //熔断窗口（秒）
                .setTimeWindow(rule.timeWindow())
                //统计周期（毫秒）
                .setStatIntervalMs(rule.getStatWindowMs())
                //最小请求数
                .setMinRequestAmount(rule.getMinRequest());
        switch (rule.type()) {
            case EXCEPTION_COUNT:
                sentinelRule
                        .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT)
                        .setCount(rule.count());
                break;
            case EXCEPTION_RATIO:
                sentinelRule
                        .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
                        .setCount(rule.count());
                break;
            case SLOW_RATIO:
                sentinelRule
                        .setGrade(RuleConstant.DEGRADE_GRADE_RT)
                        .setCount(rule.getSlowCount())
                        .setSlowRatioThreshold(rule.count());
                break;
            case DEGRADE:
                DEGRADE_RULES.add(name);
                LOGGER.info("zeus-aegis breaker-sentinel DEGRADE load : {}", name);
                break;
            default:
                return;
        }
        loadSentinelRule(name, sentinelRule);
    }

    private static void loadSentinelRule(String name, DegradeRule rule) {
        LOGGER.info("zeus-aegis breaker-sentinel load : {} start ! rule : {}", name, rule);
        List<DegradeRule> rules = DegradeRuleManager.getRules();
        if (FlowRuleManager.hasConfig(name)) {
            //删除旧的规则
            rules.removeIf(flowRule -> flowRule.getResource().equals(name));
            LOGGER.info("zeus-aegis breaker-sentinel delete old rule : {}", name);
        }
        rules.add(rule);
        DegradeRuleManager.loadRules(rules);
        LOGGER.info("zeus-aegis breaker-sentinel load : {} success !", name);
    }
}
