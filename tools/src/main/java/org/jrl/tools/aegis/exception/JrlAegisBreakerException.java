package org.jrl.tools.aegis.exception;

import org.jrl.tools.aegis.JrlAegisRule;

/**
 * 断路器异常
 *
 * @author JerryLong
 */
public class JrlAegisBreakerException extends JrlAegisException {

    public JrlAegisBreakerException(String limitName, JrlAegisRule rule) {
        super(limitName, rule);
    }
}
