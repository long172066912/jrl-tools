package org.jrl.redis.exception;

/**
* @Title: CacheException
* @Description: 异常封装对象
* @author JerryLong
* @date 2021/2/23 3:41 PM
* @version V1.0
*/
public class CacheException extends RuntimeException {

    private int code;

    private String msg;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public CacheException(int code, String msg, Exception exception) {
        super(exception);
        this.code = code;
        this.msg = msg;
    }

    public CacheException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
