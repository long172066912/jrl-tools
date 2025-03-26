package org.jrl.redis.core.cache.redis.lua;

import org.jrl.redis.core.cache.redis.commands.RedisLuaCommands;
import org.jrl.redis.exception.CacheException;
import org.jrl.redis.exception.CacheExceptionConstants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 组件提供的Lua脚本
 *
 * @author JerryLong
 */
public enum RedisLuaScripts implements RedisLuaInterface {

    /**
     * zadd，如果key存在则添加
     */
    ZADD_IF_EXISTS("zaddIfKeyExists", "local ttl = redis.call('ttl', KEYS[1]) if ttl == -1 or ttl > 10 then return tostring(redis.call('zadd', KEYS[1], ARGV[1], ARGV[2])) else return '" + RedisLuaCommands.KEY_NOT_EXISTS + "' end"),
    /**
     * zscoreBatch ，批量zscore，只返回拿到的
     */
    ZSCORE_BATCH("zscoreBatch", "local r = {} " +
            "for i, v in ipairs(ARGV) do " +
            "local zr = redis.call('ZSCORE', KEYS[1], ARGV[i]); if zr ~= false and zr ~= nil then r[ARGV[i]] = zr end;" +
            "end;" +
            "return cjson.encode(r);"),
    /**
     * hget，如果key存在则正常返回，否则抛异常
     */
    HGET_IF_KEY_EXISTS("hgetIfKeyExists", "local ttl = redis.call('ttl', KEYS[1]) if ttl == -1 or ttl > 10 then return redis.call('hget', KEYS[1], ARGV[1]) else return '" + RedisLuaCommands.KEY_NOT_EXISTS + "' end"),
    /**
     * hset，如果key存在则添加
     */
    HSET_IF_EXISTS("hsetIfKeyExists", "local ttl = redis.call('ttl', KEYS[1]) if ttl == -1 or ttl > 10 then return tostring(redis.call('hset', KEYS[1], ARGV[1], ARGV[2])) else return '" + RedisLuaCommands.KEY_NOT_EXISTS + "' end"),
    /**
     * sadd，如果key存在则添加
     */
    SADD_IF_EXISTS("saddIfKeyExist", "local ttl = redis.call('ttl', KEYS[1]) if ttl == -1 or ttl > 10 then return tostring(redis.call('sadd', KEYS[1], ARGV[1])) else return '" + RedisLuaCommands.KEY_NOT_EXISTS + "' end"),
    /**
     * lpush，如果key存在则添加
     */
    LPUSH_IF_EXISTS("lpushIfExists", "local ttl = redis.call('ttl', KEYS[1]) if ttl == -1 or ttl > 10 then return tostring(redis.call('lpush', KEYS[1], ARGV[1])) else return '" + RedisLuaCommands.KEY_NOT_EXISTS + "' end"),
    /**
     * rpush，如果key存在则添加
     */
    RPUSH_IF_EXISTS("rpushIfExists", "local ttl = redis.call('ttl', KEYS[1]) if ttl == -1 or ttl > 10 then return tostring(redis.call('rpush', KEYS[1], ARGV[1])) else return '" + RedisLuaCommands.KEY_NOT_EXISTS + "' end"),
    ;

    RedisLuaScripts(String command, String scripts) {
        this.command = command;
        this.scripts = scripts;
    }

    private String command;
    /**
     * 操作类型
     */
    private String scripts;

    @Override
    public String getScripts() {
        return scripts;
    }

    /**
     * lua脚本列表
     */
    private static Set<RedisLuaInterface> luas = new HashSet<>();

    public static void addLua(RedisLuaInterface lua) {
        RedisLuaScripts.luas.add(lua);
    }

    public static void addLua(List<RedisLuaInterface> luas) {
        RedisLuaScripts.luas.addAll(luas);
    }

    public static Set<RedisLuaInterface> getRedisLuaScripts() {
        List<RedisLuaScripts> collect = Arrays.stream(RedisLuaScripts.values()).collect(Collectors.toList());
        RedisLuaScripts.luas.addAll(collect);
        return RedisLuaScripts.luas;
    }

    public static RedisLuaScripts valueOfCommand(String command) {
        return Arrays.stream(RedisLuaScripts.values()).filter(e -> e.command.equals(command)).findFirst()
                .orElseThrow(() -> new CacheException(CacheExceptionConstants.CACHE_ERROR_CODE, "Cache2: valueOfCommand error ! command not exits ! " + command));
    }
}
