package org.jrl.utils.aegis.limit.redis;

import io.lettuce.core.ScriptOutputType;
import org.jrl.redis.client.CacheClientFactory;
import org.jrl.redis.core.BaseCacheExecutor;
import org.jrl.redis.core.cache.redis.lettuce.config.LettuceConnectSourceConfig;
import org.jrl.redis.core.cache.redis.lua.RedisLuaInterface;
import org.jrl.redis.core.model.CacheConfigModel;
import org.jrl.tools.cache.extend.mesh.JrlCacheMeshConnectType;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 玩吧cache 限流handler
 *
 * @author JerryLong
 */
public class JrlAegisRedisHandler {

    public static final int RANDOM_LENGTH = 10000;
    public static final int RANDOM_LENGTH_MAX = RANDOM_LENGTH * 10 - 1;
    public static final long TIME_WINDOW = 1000 * RANDOM_LENGTH;
    private final String resource;
    private final JrlCacheMeshConnectType connectType;
    private final BaseCacheExecutor cacheExecutor;

    public JrlAegisRedisHandler(String resource, JrlCacheMeshConnectType connectType, boolean isLocal) {
        this.resource = resource;
        this.connectType = connectType;
        if (isLocal) {
            this.cacheExecutor = CacheClientFactory.getCacheExecutor(resource, new LettuceConnectSourceConfig());
            return;
        }
        if (connectType.equals(JrlCacheMeshConnectType.POOL)) {
            this.cacheExecutor = CacheClientFactory.getCacheExecutor(CacheConfigModel.lettucePool(resource));
        } else if (connectType.equals(JrlCacheMeshConnectType.LOOP)) {
            this.cacheExecutor = CacheClientFactory.getCacheExecutor(CacheConfigModel.lettuceLoop(resource));
        } else {
            this.cacheExecutor = CacheClientFactory.getCacheExecutor(resource);
        }
    }

    /**
     * 限流
     *
     * @param key        key
     * @param max        最大值
     * @param timeWindow 时间窗口，秒
     * @return
     */
    public boolean incrByTimeWindow(String key, int max, int timeWindow) {
        final long res = (long) cacheExecutor.executeByLua(RedisLimitLua.INCR_LUA_TIME_WINDOW,
                ScriptOutputType.INTEGER,
                Collections.singletonList(key),
                //使用微秒，过期时间为时间窗口的2倍
                Arrays.asList(
                        max + "",
                        //时间窗口，秒 * 1000 * 随机数长度
                        timeWindow * TIME_WINDOW + "",
                        //当前时间+随机数，防止并发
                        System.currentTimeMillis() * RANDOM_LENGTH + ThreadLocalRandom.current().nextInt(RANDOM_LENGTH, RANDOM_LENGTH_MAX) + "",
                        timeWindow * 2 + ""
                )
        );
        return res > 0;
    }

    public boolean incr(String key, int max) {
        long res = (long) cacheExecutor.executeByLua(RedisLimitLua.INCR_LUA, ScriptOutputType.INTEGER, Collections.singletonList(key), Arrays.asList(max + "", "86400"));
        return res > 0;
    }

    public boolean decr(String key) {
        cacheExecutor.decr(key, -1);
        return true;
    }

    public enum RedisLimitLua implements RedisLuaInterface {
        INCR_LUA("local res = redis.call('GET', KEYS[1]);\n" +
                "if nil ~= res and res and tonumber(res) >= tonumber(ARGV[1]) then\n" +
                "    return -1;\n" +
                "else\n" +
                "    redis.call('INCR', KEYS[1]);\n" +
                "    redis.call('EXPIRE', KEYS[1], ARGV[2]);\n" +
                "    return 1;\n" +
                "end"),
        INCR_LUA_TIME_WINDOW("local key = KEYS[1]               -- 用户标识符\n" +
                "local limit = tonumber(ARGV[1])   -- 限制次数\n" +
                "local window = tonumber(ARGV[2])   -- 时间窗口\n" +
                "local current_timestamp = tonumber(ARGV[3])   -- 当前时间戳-纳秒\n" +
                "local expired_time = tonumber(ARGV[4])   -- 时间窗口（秒）\n" +
                "\n" +
                "-- 清理过期的请求\n" +
                "redis.call('ZREMRANGEBYSCORE', key, 0, current_timestamp - window)\n" +
                "\n" +
                "-- 检查当前请求次数\n" +
                "local count = redis.call('ZCARD', key)\n" +
                "if nil == count or count < limit then\n" +
                "    -- 记录当前请求\n" +
                "    redis.call('ZADD', key, current_timestamp, current_timestamp)\n" +
                "    redis.call('EXPIRE', key, expired_time) -- 设置 ZSET 的过期时间\n" +
                "    return 1 -- 允许请求\n" +
                "else\n" +
                "    return 0 -- 拒绝请求\n" +
                "end");

        private final String scripts;

        RedisLimitLua(String scripts) {
            this.scripts = scripts;
        }

        @Override
        public String getScripts() {
            return scripts;
        }
    }
}