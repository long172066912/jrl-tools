package org.jrl.tools.exception;

/**
 * tools基础异常
 *
 * @author JerryLong
 */
public class JrlToolsException extends RuntimeException {
    public JrlToolsException(String s, Throwable e) {
        super(s, e);
    }
}
