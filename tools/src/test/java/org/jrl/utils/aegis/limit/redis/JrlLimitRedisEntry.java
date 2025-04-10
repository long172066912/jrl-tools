package org.jrl.utils.aegis.limit.redis;

import org.jrl.tools.aegis.core.JrlAegisEntry;
import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;

public class JrlLimitRedisEntry implements JrlAegisEntry {
    private static final Logger LOGGER = JrlLoggerFactory.getLogger(JrlLimitRedisEntry.class);
    private final JrlAegisRedisHandler handler;
    private final String key;

    public JrlLimitRedisEntry(JrlAegisRedisHandler handler, String key) {
        this.handler = handler;
        this.key = key;
    }

    @Override
    public void end() {
        try {
            handler.decr(key);
        } catch (Throwable e) {
            LOGGER.error("jrl-aegis limit-redis-entry exit error ! key : {}", key, e);
        }
    }

    @Override
    public void end(Throwable error) {
        this.end();
    }
}