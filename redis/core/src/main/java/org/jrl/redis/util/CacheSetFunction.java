package org.jrl.redis.util;

import java.io.Serializable;

/**
* @Title: CacheFunction
* @Description: 自定义方法
* @author JerryLong
* @date 2021/2/23 3:06 PM
* @version V1.0
*/
@FunctionalInterface
public interface CacheSetFunction<R> extends Serializable {
    /**
     * 执行
     * @param dbData
     * @return
     */
    void apply(R dbData);
}
