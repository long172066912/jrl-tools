package org.jrl.utils.aegis.limit;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.statistic.metric.ArrayMetric;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class SentinelTest {

    @Test
    public void testRule() {
        final List<FlowRule> rules = new ArrayList<>();
        FlowRule qps = new FlowRule().setCount(1).setGrade(RuleConstant.FLOW_GRADE_QPS);
        qps.setResource("test1");
        rules.add(qps);
        FlowRuleManager.loadRules(rules);

        Assertions.assertTrue(FlowRuleManager.hasConfig("test1"));
        Assertions.assertFalse(FlowRuleManager.hasConfig("test2"));
        Assertions.assertEquals(1, FlowRuleManager.getRules().size());

        final List<FlowRule> rules1 = new ArrayList<>();
        FlowRule qps1 = new FlowRule().setCount(1).setGrade(RuleConstant.FLOW_GRADE_QPS);
        qps1.setResource("test2");
        rules1.add(qps1);
        //注意！！！！！！sentinel会把之前的覆盖
        FlowRuleManager.loadRules(rules1);

        Assertions.assertFalse(FlowRuleManager.hasConfig("test1"));
        Assertions.assertTrue(FlowRuleManager.hasConfig("test2"));
        Assertions.assertEquals(1, FlowRuleManager.getRules().size());

        final List<FlowRule> realRules = FlowRuleManager.getRules();
        realRules.add(qps);
        FlowRuleManager.loadRules(realRules);

        Assertions.assertTrue(FlowRuleManager.hasConfig("test1"));
        Assertions.assertTrue(FlowRuleManager.hasConfig("test2"));
        Assertions.assertEquals(2, FlowRuleManager.getRules().size());
    }

    @Test
    public void testTimeWindow2() throws BlockException, InterruptedException {
        final ArrayMetric arrayMetric = new ArrayMetric(10, 100, false);
        for (int i = 0; i < 5; i++) {
            arrayMetric.addPass(1);
        }
        Assertions.assertEquals(5, arrayMetric.pass());
        Thread.sleep(50L);
        Assertions.assertEquals(5, arrayMetric.pass());
        for (int i = 0; i < 5; i++) {
            arrayMetric.addPass(1);
        }
        Assertions.assertEquals(arrayMetric.getSampleCount(), arrayMetric.pass());
        Thread.sleep(50L);
        Assertions.assertEquals(5, arrayMetric.pass());
    }
}
