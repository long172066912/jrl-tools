package org.jrl.tools.aegis.core.executor.extensions.sentinel;

import com.alibaba.csp.sentinel.Entry;
import org.jrl.tools.aegis.JrlAegisType;
import org.jrl.tools.aegis.core.JrlAegisEntry;
import org.apache.commons.collections4.MapUtils;
import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;

import java.util.Optional;

import static org.jrl.tools.aegis.model.JrlAegisLimitType.TIME_WINDOW;

/**
* sentinel entry实现
* @author JerryLong
*/
public class JrlAegisSentinelEntry implements JrlAegisEntry {
    private static final Logger LOGGER = JrlLoggerFactory.getLogger(JrlAegisSentinelEntry.class);
    private final String name;
    private final Entry entry;
    private final JrlAegisType type;

    public JrlAegisSentinelEntry(String name, Entry entry, JrlAegisType type) {
        this.name = name;
        this.entry = entry;
        this.type = type;
    }

    @Override
    public void end() {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("jrl-aegis breaker-sentinel exit ! name : {}", name);
            }
            if (type == TIME_WINDOW) {
                Optional.of(MapUtils.getObject(JrlAegisSentinelLimitExtends.TIME_WINDOW_METRICS, name)).ifPresent(metric -> metric.addPass(1));
            }
            if (null != entry) {
                entry.exit(1);
            }
        } catch (Throwable e) {
            LOGGER.error("jrl-aegis breaker-sentinel exit error ! name : {}", name, e);
        }
    }

    @Override
    public void end(Throwable error) {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("jrl-aegis breaker-sentinel exit ! name : {} , error : {}", name, error);
            }
            if (type == TIME_WINDOW) {
                Optional.of(MapUtils.getObject(JrlAegisSentinelLimitExtends.TIME_WINDOW_METRICS, name)).ifPresent(metric -> metric.addPass(1));
            }
            if (null != entry) {
                if (null != error) {
                    entry.setError(error);
                }
                entry.exit(1);
            }
        } catch (Throwable e) {
            LOGGER.error("jrl-aegis breaker-sentinel exit error ! name : {}", name, e);
        }
    }
}