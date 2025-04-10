package org.jrl.tools.aegis.core.impl;

import org.jrl.tools.aegis.JrlAegisRule;
import org.jrl.tools.aegis.JrlAegisType;
import org.jrl.tools.aegis.core.AbstractJrlAegis;
import org.jrl.tools.aegis.core.JrlAegisExecutor;
import org.jrl.tools.aegis.core.rule.JrlAegisLimitRule;
import org.jrl.tools.aegis.core.executor.JrlLimitMeshExecutorWrapper;
import org.jrl.tools.aegis.model.JrlAegisResourceType;

import java.util.List;

/**
 * 限流器本地实现
 *
 * @author JerryLong
 */
public class JrlAegisMesh<E extends JrlAegisExecutor<?, R>, R extends JrlAegisRule> extends AbstractJrlAegis<E, R> {

    public JrlAegisMesh(String name, List<R> rules) {
        super(name, rules);
    }

    @Override
    public JrlAegisResourceType getResourceType() {
        return JrlAegisResourceType.MESH;
    }

    @Override
    protected E buildExecutor(R rule) {
        if (rule.type().getAction() == JrlAegisType.LIMIT) {
            return (E) new JrlLimitMeshExecutorWrapper(this.getName(), (JrlAegisLimitRule) rule);
        } else {
            throw new IllegalArgumentException("Zeus-Aegis 规则类型错误: [" + rule.type().getAction() + "] , 暂不支持分布式断路器");
        }
    }
}
