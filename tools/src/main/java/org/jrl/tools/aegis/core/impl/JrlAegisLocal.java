package org.jrl.tools.aegis.core.impl;

import org.jrl.tools.aegis.JrlAegisRule;
import org.jrl.tools.aegis.JrlAegisType;
import org.jrl.tools.aegis.core.AbstractJrlAegis;
import org.jrl.tools.aegis.core.JrlAegisExecutor;
import org.jrl.tools.aegis.core.rule.JrlAegisBreakerRule;
import org.jrl.tools.aegis.core.executor.JrlBreakerExecutorWrapper;
import org.jrl.tools.aegis.core.rule.JrlAegisLimitRule;
import org.jrl.tools.aegis.core.executor.JrlLimitExecutorWrapper;
import org.jrl.tools.aegis.model.JrlAegisResourceType;

import java.util.List;

/**
 * 限流器本地实现
 *
 * @author JerryLong
 */
public class JrlAegisLocal<E extends JrlAegisExecutor<?, R>, R extends JrlAegisRule> extends AbstractJrlAegis<E, R> {

    public JrlAegisLocal(String name, List<R> rules) {
        super(name, rules);
    }

    @Override
    public JrlAegisResourceType getResourceType() {
        return JrlAegisResourceType.LOCAL;
    }

    @Override
    protected E buildExecutor(R rule) {
        if (rule.type().getAction() == JrlAegisType.LIMIT) {
            return (E) new JrlLimitExecutorWrapper(this.getName(), (JrlAegisLimitRule) rule);
        } else if (rule.type().getAction() == JrlAegisType.BREAKER) {
            return (E) new JrlBreakerExecutorWrapper(this.getName(), (JrlAegisBreakerRule) rule);
        }
        throw new IllegalArgumentException("Jrl-Aegis 规则类型错误: [" + rule.type().getAction() + "] , 不存在的类型");
    }
}
