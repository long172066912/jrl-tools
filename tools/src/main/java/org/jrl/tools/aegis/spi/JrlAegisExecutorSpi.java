package org.jrl.tools.aegis.spi;

import org.jrl.tools.aegis.JrlAegisRule;
import org.jrl.tools.aegis.core.JrlAegisEntry;
import org.jrl.tools.aegis.exception.JrlAegisException;
import org.jrl.tools.spi.JrlSpi;

/**
 * spi扩展接口，实现真正的拦截逻辑
 *
 * @author JerryLong
 */
@JrlSpi(
        group = {
                "local-breaker",
                "lock-limiter",
                "mesh-limiter"
        }
)
public interface JrlAegisExecutorSpi<E extends JrlAegisEntry, R extends JrlAegisRule> {
    /**
     * 初始化
     *
     * @param name
     * @param rule
     */
    void init(String name, R rule);

    /**
     * 尝试获取令牌
     *
     * @param name
     * @param rule
     * @return
     */
    E tryAcquire(String name, R rule) throws JrlAegisException;

    /**
     * 加载规则
     *
     * @param name
     * @param rule
     */
    void load(String name, R rule);
}
