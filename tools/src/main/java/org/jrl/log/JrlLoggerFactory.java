package org.jrl.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * Jrl日志工厂
 *
 * @author JerryLong
 */
public class JrlLoggerFactory {

    private static JrlLoggerBuilder jrlLoggerBuilder = JrlLogSpiUtil.getSpiOrDefault(JrlLoggerBuilder.class, DefaultJrlLoggerBuilder::new);

    /**
     * 获取LOGGER
     *
     * @param loggerClass
     * @param <T>
     * @return
     */
    public static <T> Logger getLogger(Class<T> loggerClass) {
        return jrlLoggerBuilder.getLogger(loggerClass);
    }

    /**
     * 默认实现
     */
    public static class DefaultJrlLoggerBuilder implements JrlLoggerBuilder {

        @Override
        public Logger getLogger(Class<?> loggerClass) {
            return LoggerFactory.getLogger(loggerClass);
        }
    }

    static class JrlLogSpiUtil {

        public static <T> T getSpiOrDefault(Class<T> clazz, Supplier<T> def) {
            try {
                final Iterator<? extends T> iterator = ServiceLoader.load(clazz).iterator();
                if (iterator.hasNext()) {
                    return iterator.next();
                } else {
                    return def.get();
                }
            } catch (Exception e) {
                return def.get();
            }
        }
    }
}
