package org.jrl.redis.core.model;

import org.jrl.redis.exception.CacheLockFailException;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: CacheLockResponse
 * @Description: //TODO (用一句话描述该文件做什么)
 * @date 2023/3/2 14:45
 */
public class CacheLockResponse<T> {

    public CacheLockResponse(boolean isLockSuccess, T data) {
        this.isLockSuccess = isLockSuccess;
        this.data = data;
    }

    public CacheLockResponse(boolean isLockSuccess, T data, Throwable e) {
        this.isLockSuccess = isLockSuccess;
        this.data = data;
        this.businessException = e;
    }

    private boolean isLockSuccess;
    private T data;
    private Throwable businessException;

    public T get() throws CacheLockFailException {
        if (!isLockSuccess) {
            throw new CacheLockFailException();
        }
        return data;
    }

    public T getCanNoLock() {
        return data;
    }

    public Throwable getBusinessException() {
        return businessException;
    }
}
