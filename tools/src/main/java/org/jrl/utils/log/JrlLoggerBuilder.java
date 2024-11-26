package org.jrl.utils.log;

import org.slf4j.Logger;

/**
 * logger构建工具，可以通过spi方式扩展自己的实现
 *
 * @author JerryLong
 */
public interface JrlLoggerBuilder {
    /**
     * 获取LOGGER
     *
     * @param loggerClass
     * @return
     */
    Logger getLogger(Class<?> loggerClass);
}
