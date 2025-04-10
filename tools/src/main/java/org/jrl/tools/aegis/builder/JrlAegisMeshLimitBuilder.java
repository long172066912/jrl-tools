package org.jrl.tools.aegis.builder;

import org.jrl.tools.aegis.JrlAegis;
import org.jrl.tools.aegis.core.rule.JrlAegisLimitRule;
import org.jrl.tools.aegis.manager.JrlAegisManager;
import org.jrl.tools.aegis.model.JrlAegisResourceType;
import org.apache.commons.lang3.StringUtils;
import org.jrl.tools.cache.extend.mesh.JrlCacheMeshConnectType;

/**
 * 分布式限流
 *
 * @author JerryLong
 */
public class JrlAegisMeshLimitBuilder extends JrlAegisLocalBuilder<JrlAegisLimitRule> {
    private final String resource;
    private JrlCacheMeshConnectType connectType;

    public JrlAegisMeshLimitBuilder(String name, String resource) {
        super(name);
        this.resource = resource;
    }

    public JrlAegisMeshLimitBuilder connectType(JrlCacheMeshConnectType connectType) {
        this.connectType = connectType;
        return this;
    }

    @Override
    public JrlAegis build() {
        resourceType = JrlAegisResourceType.MESH;
        if (StringUtils.isBlank(resource)) {
            throw new IllegalArgumentException("Zeus-Aegis 规则 resource 不能为空");
        }
        if (null == connectType) {
            connectType = JrlCacheMeshConnectType.NORMAL;
        }
        checkRule();
        //重置rule里的resource与connectType
        rules.forEach(rule -> {
            if (StringUtils.isBlank(rule.getResource())) {
                rule.setResource(resource);
            }
            if (rule.getConnectType() == null) {
                rule.setConnectType(connectType);
            }
        });
        return JrlAegisManager.getAegis(name, resourceType, rules);
    }
}