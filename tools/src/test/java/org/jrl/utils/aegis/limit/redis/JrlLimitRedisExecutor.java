package org.jrl.utils.aegis.limit.redis;

import org.jrl.tools.aegis.core.rule.JrlAegisLimitRule;
import org.jrl.tools.aegis.exception.JrlAegisException;
import org.jrl.tools.aegis.exception.JrlAegisLimitException;
import org.jrl.tools.aegis.spi.JrlAegisExecutorSpi;
import org.jrl.tools.log.JrlLoggerFactory;
import org.jrl.tools.spi.JrlSpiGroup;
import org.slf4j.Logger;

/**
 * 限流执行器-redis
 *
 * @author JerryLong
 */
@JrlSpiGroup("mesh-limiter")
public class JrlLimitRedisExecutor implements JrlAegisExecutorSpi<JrlLimitRedisEntry, JrlAegisLimitRule> {
    private static final Logger LOGGER = JrlLoggerFactory.getLogger(JrlLimitRedisExecutor.class);
    private JrlAegisRedisHandler handler;

    @Override
    public void init(String name, JrlAegisLimitRule rule) {
        LOGGER.info("zeus-aegis limit-wbCache2 init start ! name : {} , cacheType : {} , connectionType : {}", name, rule.getResource(), rule.getConnectType());
        //使用本地连接
        handler = new JrlAegisRedisHandler(rule.getResource(), rule.getConnectType(), true);
        LOGGER.info("zeus-aegis limit-wbCache2 init success !");
    }

    @Override
    public JrlLimitRedisEntry tryAcquire(String name, JrlAegisLimitRule rule) throws JrlAegisException {
        boolean status = true;
        JrlLimitRedisEntry entry = null;
        try {
            switch (rule.type()) {
                case THREAD:
                    status = handler.incr(name, rule.count());
                    entry = new JrlLimitRedisEntry(handler, name);
                    break;
                case TIME_WINDOW:
                    status = handler.incrByTimeWindow(name, rule.count(), rule.timeWindow());
                    break;
                case QPS:
                default:
                    status = handler.incrByTimeWindow(name, rule.count(), 1);
                    break;
            }
        } catch (Exception e) {
            LOGGER.error("zeus-aegis limit-wbCache2 tryAcquire error ! name : {} , rule : {}", name, rule);
            return null;
        }
        if (!status) {
            LOGGER.warn("zeus-aegis limit-wbCache2 block ! name : {} , rule : {}", name, rule);
            throw new JrlAegisLimitException(name, rule);
        }
        return entry;
    }

    @Override
    public void load(String name, JrlAegisLimitRule rule) {

    }


}
