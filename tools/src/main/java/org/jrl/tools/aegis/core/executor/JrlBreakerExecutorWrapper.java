package org.jrl.tools.aegis.core.executor;

import org.jrl.tools.aegis.core.JrlAegisEntry;
import org.jrl.tools.aegis.core.JrlAegisExecutor;
import org.jrl.tools.aegis.core.rule.JrlAegisBreakerRule;
import org.jrl.tools.aegis.spi.JrlAegisExecutorSpi;
import org.jrl.tools.log.JrlLoggerFactory;
import org.jrl.tools.spi.JrlSpiLoader;
import org.slf4j.Logger;

/**
 * sentinel实现
 *
 * @author JerryLong
 */
public class JrlBreakerExecutorWrapper<E extends JrlAegisEntry> extends JrlAegisExecutor.AbstractZeusAegisExecutor<E, JrlAegisBreakerRule> {

    private static final Logger LOGGER = JrlLoggerFactory.getLogger(JrlBreakerExecutorWrapper.class);

    private final JrlAegisExecutorSpi<E, JrlAegisBreakerRule> breakExecutor;

    public JrlBreakerExecutorWrapper(String name, JrlAegisBreakerRule rule) {
        super(name, rule);
        breakExecutor = JrlSpiLoader.getInstance(JrlAegisExecutorSpi.class, "local-breaker");
        breakExecutor.init(getName(), rule);
    }

    @Override
    public E tryAcquire() {
        return breakExecutor.tryAcquire(name, rule);
    }

    @Override
    public void changeRule(JrlAegisBreakerRule rule) {
        breakExecutor.load(name, rule);
        this.rule = rule;
    }

    @Override
    protected String alias() {
        return "zeus-aegis-breaker-sentinel";
    }
}
