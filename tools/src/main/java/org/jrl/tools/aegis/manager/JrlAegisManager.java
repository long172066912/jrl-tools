package org.jrl.tools.aegis.manager;

import org.jrl.tools.aegis.JrlAegisRule;
import org.jrl.tools.aegis.core.AbstractJrlAegis;
import org.jrl.tools.aegis.core.JrlAegisExecutor;
import org.jrl.tools.aegis.core.impl.JrlAegisLocal;
import org.jrl.tools.aegis.core.impl.JrlAegisMesh;
import org.jrl.tools.aegis.model.JrlAegisResourceType;
import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 神盾管理器
 *
 * @author JerryLong
 */
public class JrlAegisManager {

    private static final Logger LOGGER = JrlLoggerFactory.getLogger(JrlAegisManager.class);
    private static final Map<String, AbstractJrlAegis> JRL_AEGIS_MAP = new ConcurrentHashMap<>();

    public static AbstractJrlAegis getAegis(String name) {
        return JRL_AEGIS_MAP.get(name);
    }


    public static <R extends JrlAegisRule, E extends JrlAegisExecutor<?, R>> AbstractJrlAegis<E, R> getAegis(String name, JrlAegisResourceType resourceType, List<R> rules) {
        return JRL_AEGIS_MAP.computeIfAbsent(name, k -> {
            LOGGER.info("jrl-aegis create : {} , resourceType : {} , rule size : {}", name, resourceType, rules.size());
            switch (resourceType) {
                case MESH:
                    return new JrlAegisMesh<>(name, rules);
                case LOCAL:
                default:
                    return new JrlAegisLocal<>(name, rules);
            }
        });
    }
}
