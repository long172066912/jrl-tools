package org.jrl.tools.aegis.builder;

import org.jrl.tools.aegis.core.rule.JrlAegisLimitRule;

/**
 * 限流构建器
 *
 * @author JerryLong
 */
public class JrlAegisLimitBuilder {

    /**
     * 本地限流
     *
     * @param name
     * @return
     */
    public JrlAegisLocalBuilder<JrlAegisLimitRule> local(String name) {
        return new JrlAegisLocalBuilder<>(name);
    }

    /**
     * 分布式限流
     *
     * @param name
     * @param resource
     * @return
     */
    public JrlAegisMeshLimitBuilder mesh(String name, String resource) {
        return new JrlAegisMeshLimitBuilder(name, resource);
    }
}
