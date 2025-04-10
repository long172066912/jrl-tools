package org.jrl.tools.aegis.exception;

import org.jrl.tools.aegis.JrlAegisRule;

/**
 * 异常
 *
 * @author JerryLong
 */
public class JrlAegisException extends RuntimeException {

    private final JrlAegisRule rule;
    private final String name;

    public JrlAegisException(String limitName, JrlAegisRule rule) {
        super("zeus-aegis block ! name : " + limitName + " scope : " + rule.scope().getScope() + " type : " + rule.type().name(), null);
        this.name = limitName;
        this.rule = rule;
    }

    public JrlAegisRule getRule() {
        return rule;
    }

    public String getName() {
        return name;
    }
}
