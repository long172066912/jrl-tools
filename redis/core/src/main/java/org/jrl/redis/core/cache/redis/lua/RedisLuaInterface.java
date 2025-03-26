package org.jrl.redis.core.cache.redis.lua;

/**
* @Title: RedisLuaInterface
* @Description: Lua脚本接口
* @author JerryLong
* @date 2022/1/10 3:56 PM
* @version V1.0
*/
@FunctionalInterface
public interface RedisLuaInterface {
    /**
     * 获取lua脚本
     * @return
     */
    String getScripts();
}
