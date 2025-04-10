package org.jrl.tools.aegis.exception;

import org.jrl.tools.aegis.JrlAegisRule;

/**
 * 限流异常
 *
 * @author JerryLong
 */
public class JrlAegisLimitException extends JrlAegisException {

    public JrlAegisLimitException(String limitName, JrlAegisRule rule) {
        super(limitName, rule);
    }
}
