package org.jrl.tools.aegis.core.executor;

import org.jrl.tools.aegis.core.JrlAegisEntry;
import org.jrl.tools.aegis.core.JrlAegisExecutor;
import org.jrl.tools.aegis.core.rule.JrlAegisLimitRule;
import org.jrl.tools.aegis.spi.JrlAegisExecutorSpi;
import org.jrl.tools.log.JrlLoggerFactory;
import org.jrl.tools.spi.JrlSpiLoader;
import org.slf4j.Logger;

/**
 * 限流执行器-redis
 *
 * @author JerryLong
 */
public class JrlLimitMeshExecutorWrapper<E extends JrlAegisEntry> extends JrlAegisExecutor.AbstractJrlAegisExecutor<E, JrlAegisLimitRule> {
    private static final Logger LOGGER = JrlLoggerFactory.getLogger(JrlLimitMeshExecutorWrapper.class);

    private final JrlAegisExecutorSpi<E, JrlAegisLimitRule> limitExecutor;

    public JrlLimitMeshExecutorWrapper(String name, JrlAegisLimitRule rule) {
        super(name, rule);
        limitExecutor = JrlSpiLoader.getInstance(JrlAegisExecutorSpi.class, "mesh-limiter");
        limitExecutor.init(getName(), rule);
    }

    @Override
    public E tryAcquire() {
        return limitExecutor.tryAcquire(name, rule);
    }

    @Override
    public void changeRule(JrlAegisLimitRule rule) {
        limitExecutor.load(name, rule);
        this.rule = rule;
    }

    @Override
    protected String alias() {
        return "jrl-aegis-limit-redis";
    }
}
