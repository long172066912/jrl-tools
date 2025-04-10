package org.jrl.tools.aegis.core.executor.extensions.sentinel;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.statistic.metric.ArrayMetric;
import org.jrl.tools.aegis.JrlAegisType;
import org.jrl.tools.aegis.core.rule.JrlAegisLimitRule;
import org.jrl.tools.aegis.exception.JrlAegisException;
import org.jrl.tools.aegis.exception.JrlAegisLimitException;
import org.jrl.tools.aegis.spi.JrlAegisExecutorSpi;
import org.jrl.tools.log.JrlLoggerFactory;
import org.jrl.tools.spi.JrlSpiGroup;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.jrl.tools.aegis.model.JrlAegisLimitType.TIME_WINDOW;

/**
 * 本地默认实现
 *
 * @author JerryLong
 */
@JrlSpiGroup("local-limiter")
public class JrlAegisSentinelLimitExtends
        implements JrlAegisExecutorSpi<JrlAegisSentinelEntry, JrlAegisLimitRule> {
    private static final Logger LOGGER = JrlLoggerFactory.getLogger(JrlAegisSentinelLimitExtends.class);
    protected static final Map<String, ArrayMetric> TIME_WINDOW_METRICS = new ConcurrentHashMap<>();
    private static final int JRL_AEGIS_LIMIT_RESOURCE_TYPE_ID = 1000000;

    @Override
    public void init(String name, JrlAegisLimitRule rule) {
        this.load(name, rule);
    }

    @Override
    public JrlAegisSentinelEntry tryAcquire(String name, JrlAegisLimitRule rule) throws JrlAegisException {
        try {
            final JrlAegisType type = rule.type();
            if (type == TIME_WINDOW) {
                final ArrayMetric metric = TIME_WINDOW_METRICS.get(name);
                if (null == metric) {
                    return null;
                }
                if (metric.getSampleCount() <= metric.pass()) {
                    throw new FlowException("jrl-limit-sentinel timeWindow flow fail ! name : " + name);
                }
                return new JrlAegisSentinelEntry(name, null, TIME_WINDOW);
            }
            return new JrlAegisSentinelEntry(name, SphU.asyncEntry(name, JRL_AEGIS_LIMIT_RESOURCE_TYPE_ID, EntryType.IN), type);
        } catch (BlockException e) {
            LOGGER.warn("jrl-aegis limit-sentinel block ! name : {} , rule : {}", name, rule);
            throw new JrlAegisLimitException(name, rule);
        }
    }

    @Override
    public void load(String name, JrlAegisLimitRule rule) {
        switch (rule.type()) {
            case THREAD:
                FlowRule thread = new FlowRule();
                thread.setResource(name);
                thread.setCount(rule.count());
                thread.setGrade(RuleConstant.FLOW_GRADE_THREAD);
                loadSentinelRule(name, thread);
                LOGGER.info("jrl-aegis limit-sentinel load THREAD : {} , count : {}", name, rule.count());
                break;
            case TIME_WINDOW:
                //通过时间窗口降级实现限流
                final ArrayMetric arrayMetric = new ArrayMetric(rule.count(), rule.timeWindow() * 1000, false);
                TIME_WINDOW_METRICS.put(name, arrayMetric);
                LOGGER.info("jrl-aegis limit-sentinel load TIME_WINDOW : {} , count : {} , timeWindow : {}", name, rule.count(), rule.timeWindow());
                break;
            case QPS:
            default:
                FlowRule qps = new FlowRule().setCount(rule.count()).setGrade(RuleConstant.FLOW_GRADE_QPS);
                qps.setResource(name);
                loadSentinelRule(name, qps);
                LOGGER.info("jrl-aegis limit-sentinel load QPS : {} , count : {}", name, rule.count());
                break;
        }
    }

    private void loadSentinelRule(String name, FlowRule rule) {
        List<FlowRule> rules = FlowRuleManager.getRules();
        if (FlowRuleManager.hasConfig(name)) {
            //删除旧的限流规则
            rules.removeIf(flowRule -> flowRule.getResource().equals(name));
            LOGGER.info("jrl-aegis limit-sentinel delete old rule : {}", name);
        }
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
    }
}
