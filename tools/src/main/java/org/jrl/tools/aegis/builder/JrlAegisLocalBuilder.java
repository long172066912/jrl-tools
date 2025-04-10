package org.jrl.tools.aegis.builder;

import org.jrl.tools.aegis.JrlAegis;
import org.jrl.tools.aegis.JrlAegisRule;
import org.jrl.tools.aegis.manager.JrlAegisManager;
import org.jrl.tools.aegis.model.JrlAegisResourceType;
import org.apache.commons.collections4.CollectionUtils;
import org.jrl.tools.json.JrlJsonNoExpUtil;
import org.jrl.tools.utils.JrlCollectionUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 神盾-本地-构建
 *
 * @author JerryLong
 */
public class JrlAegisLocalBuilder<R extends JrlAegisRule> {
    /**
     * 数据资源类型
     */
    protected JrlAegisResourceType resourceType;
    /**
     * 规则
     */
    protected final List<R> rules = new ArrayList<>();
    /**
     * 名称
     */
    protected final String name;

    public JrlAegisLocalBuilder(String name) {
        this.name = name;
    }

    public JrlAegisLocalBuilder<R> addRule(R rule) {
        this.rules.add(rule);
        return this;
    }

    public JrlAegis build() {
        resourceType = JrlAegisResourceType.LOCAL;
        checkRule();
        return JrlAegisManager.getAegis(name, resourceType, rules);
    }

    protected void checkRule() {
        if (CollectionUtils.isEmpty(rules)) {
            throw new IllegalArgumentException("Zeus-Aegis 规则不能为空");
        }
        //检测rules的id是否唯一
        //对rules分组
        final Map<String, List<R>> ruleInfo = JrlCollectionUtil.group(rules, rule -> rule.scope().getScope());
        //检测action是否有重复，不同的action应该是不同的name
        Map<Integer, AtomicInteger> actions = new HashMap<>(8);
        ruleInfo.values().forEach(rule -> {
            Map<Integer, AtomicInteger> counter = new HashMap<>(8);
            rule.forEach(rule1 -> {
                final int i = counter.computeIfAbsent(rule1.id(), k -> new AtomicInteger(0)).incrementAndGet();
                if (i > 1) {
                    throw new IllegalArgumentException("Zeus-Aegis scope[" + rule1.scope().getScope() + "] 规则 id 不能重复:" + rule1.id());
                }
                actions.computeIfAbsent(rule1.type().getAction(), k -> new AtomicInteger(0)).incrementAndGet();
            });
        });
        if (actions.keySet().size() > 1) {
            throw new IllegalArgumentException("Zeus-Aegis 规则 action 不能重复: [" + JrlJsonNoExpUtil.toJson(actions.keySet()) + "]");
        }
    }
}