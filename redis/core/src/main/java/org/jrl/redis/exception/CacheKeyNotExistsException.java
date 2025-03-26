package org.jrl.redis.exception;

/**
* key不存在异常
* @author JerryLong
*/
public class CacheKeyNotExistsException extends RuntimeException {
    public CacheKeyNotExistsException(String message) {
        super(message);
    }
}
