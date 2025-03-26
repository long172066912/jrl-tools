package org.jrl.redis.core.handle;

import org.jrl.redis.util.CacheFunction;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: InterfaceCommomHandle
 * @Description: 公告操作类接口
 * @date 2021/1/18 3:17 PM
 */
public interface InterfaceCommomHandle {
    /**
     * 执行命令
     *
     * @param function
     * @return
     */
    Object execute(CacheFunction function);
    /**
     * 执行命令
     *
     * @param function
     * @param key
     * @return
     */
    Object execute(CacheFunction function, String... key);
}
