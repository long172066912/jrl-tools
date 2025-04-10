package org.jrl.tools.aegis.core.executor;

import org.jrl.tools.aegis.core.JrlAegisEntry;
import org.jrl.tools.aegis.core.JrlAegisExecutor;
import org.jrl.tools.aegis.core.rule.JrlAegisLimitRule;
import org.jrl.tools.aegis.spi.JrlAegisExecutorSpi;
import org.jrl.tools.log.JrlLoggerFactory;
import org.jrl.tools.spi.JrlSpiLoader;
import org.slf4j.Logger;

/**
 * sentinel实现
 *
 * @author JerryLong
 */
public class JrlLimitExecutorWrapper<E extends JrlAegisEntry> extends JrlAegisExecutor.AbstractZeusAegisExecutor<E, JrlAegisLimitRule> {

    private static final Logger LOGGER = JrlLoggerFactory.getLogger(JrlLimitExecutorWrapper.class);
    private final JrlAegisExecutorSpi<E, JrlAegisLimitRule> limitExecutor;

    public JrlLimitExecutorWrapper(String name, JrlAegisLimitRule rule) {
        super(name, rule);
        limitExecutor = JrlSpiLoader.getInstance(JrlAegisExecutorSpi.class, "local-limiter");
        limitExecutor.init(getName(), rule);
    }

    @Override
    public E tryAcquire() {
        return limitExecutor.tryAcquire(getName(), getRule());
    }

    @Override
    public void changeRule(JrlAegisLimitRule rule) {
        limitExecutor.load(getName(), rule);
        this.rule = rule;
    }

    @Override
    protected String alias() {
        return "zeus-aegis-limit-sentinel";
    }
}
