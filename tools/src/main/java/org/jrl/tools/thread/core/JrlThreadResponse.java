package org.jrl.tools.thread.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 线程池返回结果
 *
 * @author JerryLong
 */
public class JrlThreadResponse {

    private static Logger LOGGER = LoggerFactory.getLogger(JrlThreadResponse.class);

    private Object result;
    private Throwable throwable;

    public JrlThreadResponse() {
    }

    public JrlThreadResponse(Object result, Throwable throwable) {
        this.result = result;
        this.throwable = throwable;
    }

    public Object getResultOrNull() {
        if (null != throwable) {
            LOGGER.error("jrl-thread JrlThreadResponse get result error !", throwable);
        }
        return result;
    }

    public Object getResult() throws Throwable {
        if (null != throwable) {
            throw throwable;
        }
        return result;
    }

    public JrlThreadResponse setResult(Object result) {
        this.result = result;
        return this;
    }

    public JrlThreadResponse setThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }
}
