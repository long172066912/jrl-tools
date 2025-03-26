package org.jrl.redis.exception;

import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: CacheExceptionFactory
 * @Description: 统一异常处理
 * @date 2021/1/21 2:35 PM
 */
public class CacheExceptionFactory {

    private static final Logger LOGGER = JrlLoggerFactory.getLogger(CacheExceptionFactory.class);
    /**
     * 日志前缀
     */
    private static final String MESSAGE_PREFIX = "Cache2: ";

    /**
     * 抛出异常
     *
     * @param message
     */
    public static void throwException(String message) {
        throwException(CacheExceptionConstants.CACHE_ERROR_CODE, MESSAGE_PREFIX + message, null);
    }

    /**
     * 抛出异常
     *
     * @param message
     */
    public static void throwException(String message, String data) {
        throwException(CacheExceptionConstants.CACHE_ERROR_CODE, MESSAGE_PREFIX + message, data, null);
    }

    /**
     * 抛出异常
     *
     * @param message
     */
    public static void throwException(String message, Exception exception) {
        throwException(CacheExceptionConstants.CACHE_ERROR_CODE, MESSAGE_PREFIX + message, null, exception);
    }

    /**
     * 抛出异常
     *
     * @param errorEode
     * @param message
     * @param exception
     */
    public static void throwException(int errorEode, String message, Exception exception) {
        LOGGER.error(MESSAGE_PREFIX + "Cache exception error ! errorCode:{},message:{}", errorEode, message, exception);
        throw new CacheException(errorEode, message, null == exception ? new Exception(message) : exception);
    }

    /**
     * 抛出异常
     *
     * @param errorEode
     * @param message
     * @param exception
     */
    public static void throwException(int errorEode, String message, String data, Exception exception) {
        LOGGER.error(MESSAGE_PREFIX + "Cache exception error ! errorCode:{},message:{},data:{}", errorEode, message, data, exception);
        throw new CacheException(errorEode, message, null == exception ? new Exception(message) : exception);
    }

    /**
     * 抛出异常
     *
     * @param errorEode
     * @param message
     * @param exception
     */
    public static void throwExceptionNoLog(int errorEode, String message, String data, Exception exception) {
        throw new CacheException(errorEode, message, null == exception ? new Exception(message) : exception);
    }

    /**
     * 记录错误日志
     *
     * @param message
     */
    public static void addErrorLog(String message) {
        LOGGER.error(MESSAGE_PREFIX + "CacheError message:{}", message);
    }

    /**
     * 记录错误日志
     *
     * @param message
     */
    public static void addErrorLog(String message, Exception e) {
        LOGGER.error(MESSAGE_PREFIX + "CacheError message:{}", message, e);
    }

    /**
     * 记录错误日志
     *
     * @param message
     */
    public static void addErrorLog(String message,String data, Exception e) {
        LOGGER.error(MESSAGE_PREFIX + message, data, e);
    }

    /**
     * 记录错误日志
     *
     * @param service   类
     * @param errorInfo 错误信息：方法名->错误位置
     * @param message   额外参数
     * @param exception
     * @param args      参数值
     */
    public static void addErrorLog(String service, String errorInfo, String message, Exception exception, Object... args) {
        LOGGER.error(MESSAGE_PREFIX + "CacheError service:{},function:{},errorMessage:{},messageData:{}", service, errorInfo, message, args, exception);
    }

    /**
     * 警告日志
     *
     * @param msg
     * @param args
     */
    public static void addWarnLog(String msg, Exception exception, String... args) {
        LOGGER.warn(MESSAGE_PREFIX + msg, args, exception);
    }

    /**
     * 警告日志
     *
     * @param msg
     * @param args
     */
    public static void addWarnLog(String msg, String... args) {
        LOGGER.warn(MESSAGE_PREFIX + msg, args);
    }

    public static void main(String[] args) {
        throwException(CacheExceptionConstants.CACHE_ERROR_CODE, "test", null);
        addErrorLog("a", "b", "c:{},d:{}", new Exception(), "1", 2);
        System.out.println(1111);

    }
}
