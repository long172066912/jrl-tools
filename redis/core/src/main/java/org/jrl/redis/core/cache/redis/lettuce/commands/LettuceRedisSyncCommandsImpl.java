package org.jrl.redis.core.cache.redis.lettuce.commands;

import io.lettuce.core.*;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.apache.commons.collections4.CollectionUtils;
import org.jrl.redis.config.BaseCacheConfig;
import org.jrl.redis.core.cache.redis.lettuce.AbstractLettuceHandleExecutor;
import org.jrl.redis.core.cache.redis.lettuce.config.LettuceClusterConnectSourceConfig;
import org.jrl.redis.core.cache.redis.lettuce.config.LettuceConnectSourceConfig;
import org.jrl.redis.core.constant.ConnectTypeEnum;
import org.jrl.redis.core.constant.RedisMagicConstants;
import org.jrl.redis.core.model.InterfacePubSubModel;
import org.jrl.redis.exception.CacheExceptionFactory;
import org.jrl.redis.util.CacheCommonUtils;
import redis.clients.jedis.*;
import redis.clients.jedis.params.geo.GeoRadiusParam;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;
import redis.clients.util.SafeEncoder;
import redis.clients.util.Slowlog;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static redis.clients.jedis.Protocol.Keyword.*;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: LettuceRedisSyncCommandsImpl
 * @Description: Lettuce同步命令实现
 * @date 2021/2/23 3:12 PM
 */
@SuppressWarnings("unchecked")
public class LettuceRedisSyncCommandsImpl extends AbstractLettuceHandleExecutor {

    @Override
    public Boolean set(String key, String value, int timeout) {
        return (Boolean) this.execute(() -> RedisMagicConstants.OK.equals(
                asyncGet() ? this.getAsyncExecutor().setex(key, timeout, value).get(getTimeout(), TimeUnit.MILLISECONDS) :
                        this.sync().setex(key, timeout, value)
        ), key);
    }

    @Override
    public Boolean set(String key, String value, String nxxx, String expx, long time) {
        return (Boolean) this.execute(() -> {
            SetArgs setArgs = new SetArgs();
            if (RedisMagicConstants.UNX.equals(nxxx) || RedisMagicConstants.NX.equals(nxxx)) {
                setArgs.nx();
            }
            if (RedisMagicConstants.UXX.equals(nxxx) || RedisMagicConstants.XX.equals(nxxx)) {
                setArgs.xx();
            }
            if (RedisMagicConstants.UEX.equals(expx) || RedisMagicConstants.EX.equals(expx)) {
                setArgs.ex(time);
            }
            if (RedisMagicConstants.UPX.equals(expx) || RedisMagicConstants.PX.equals(expx)) {
                setArgs.px(time);
            }
            return RedisMagicConstants.OK.equals(
                    this.asyncGet() ? this.getAsyncExecutor().set(key, value, setArgs).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().set(key, value, setArgs)
            );
        }, key);
    }

    @Override
    public String get(String key) {
        return (String) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().get(key).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().get(key);
        }, key);
    }

    @Override
    public Boolean exists(String key) {
        return (Boolean) this.execute(() -> {
            return
                    (
                            this.asyncGet() ? ((Future<Long>) this.getAsyncExecutor().exists(key)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().exists(key)
                    ) > 0;
        }, key);
    }

    @Override
    public Long del(String key) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().del(key).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().del(key);
        }, key);
    }

    @Override
    public Boolean expire(String key, int seconds) {
        return (Boolean) this.execute(() ->
                        this.asyncGet() ? this.getAsyncExecutor().expire(key, seconds).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                this.sync().expire(key, seconds)
                , key);
    }

    @Override
    public Boolean expireAt(String key, long unixTime) {
        return (Boolean) this.execute(() ->
                        this.asyncGet() ? this.getAsyncExecutor().expireat(key, unixTime).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                this.sync().expireat(key, unixTime)
                , key);
    }

    @Override
    public Long ttl(String key) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().ttl(key).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().ttl(key);
        }, key);
    }

    @Override
    public String getSet(String key, String value) {
        return (String) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().getset(key, value).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().getset(key, value);
        }, key);
    }

    @Override
    public Map<String, Object> mget(String... keys) {
        List<KeyValue<String, Object>> list = (List<KeyValue<String, Object>>) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().mget(keys).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().mget(keys);
        }, keys);
        if (CollectionUtils.isEmpty(list)) {
            return new HashMap<>(2);
        }
        Map<String, Object> map = new HashMap<>(list.size());
        for (KeyValue<String, Object> stringObjectKeyValue : list) {
            stringObjectKeyValue.ifHasValue(e -> {
                map.put(stringObjectKeyValue.getKey(), stringObjectKeyValue.getValue());
            });
            stringObjectKeyValue.ifEmpty(() -> {
                map.put(stringObjectKeyValue.getKey(), null);
            });
        }
        return map;
    }

    @Override
    public Boolean setnx(String key, String value, int seconds) {
        return this.set(key, value, RedisMagicConstants.UNX, RedisMagicConstants.EX, seconds);
    }

    @Override
    public String setex(String key, int seconds, String value) {
        return (String) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().setex(key, seconds, value).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().setex(key, seconds, value);
        }, key);
    }

    @Override
    public String mset(int seconds, String... keysvalues) {
        return this.mset(seconds, CacheCommonUtils.strings2Map(keysvalues));
    }

    @Override
    public String mset(int seconds, Map<String, String> keyValues) {
        if (null == keyValues) {
            CacheExceptionFactory.throwException("Lettuce->mset 参数错误");
            return null;
        }
        try {
            return (String) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().mset(keyValues).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().mset(keyValues)
                    , keyValues.keySet().toArray(new String[keyValues.keySet().size()]));
        } finally {
            if (seconds > 0) {
                //设置过期时间
                CompletableFuture.runAsync(() -> keyValues.keySet().forEach(key -> this.expire(key, seconds)), threadPoolExecutor);
            }
        }
    }

    @Override
    public Boolean msetnx(int seconds, String... keysvalues) {
        Map<String, Object> keyValues = CacheCommonUtils.stringsToMap(keysvalues);
        if (null == keyValues) {
            CacheExceptionFactory.throwException("Lettuce->msetnx 参数错误");
            return null;
        }
        try {
            return (Boolean) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().msetnx(keyValues).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().msetnx(keyValues)
                    , keyValues.keySet().toArray(new String[keyValues.keySet().size()]));
        } finally {
            if (seconds > 0) {
                //设置过期时间
                CompletableFuture.runAsync(() -> keyValues.keySet().forEach(key -> this.expire(key, seconds)), threadPoolExecutor);
            }
        }
    }

    @Override
    public Long decrBy(String key, long decrement, int seconds) {
        try {
            return (Long) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().decrby(key, decrement).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().decrby(key, decrement)
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public Long decr(String key, int seconds) {
        try {
            return (Long) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().decr(key).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().decr(key)
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public Long incrBy(String key, long increment, int seconds) {
        try {
            return (Long) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().incrby(key, increment).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().incrby(key, increment)
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public Double incrByFloat(String key, double increment, int seconds) {
        try {
            return (Double) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().incrbyfloat(key, increment).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().incrbyfloat(key, increment)
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public Long incr(String key, int seconds) {
        try {
            return (Long) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().incr(key).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().incr(key)
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public Long append(String key, String value) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().append(key, value).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().append(key, value);
        }, key);
    }

    @Override
    public String substr(String key, int start, int end) {
        return (String) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().getrange(key, start, end).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().getrange(key, start, end);
        }, key);
    }

    @Override
    public Boolean hset(String key, String field, String value, int seconds) {
        try {
            return (Boolean) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().hset(key, field, value).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().hset(key, field, value)
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public Long hset(String key, Map<String, String> hash, int seconds) {
        try {
            String res = (String) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().hmset(key, hash).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().hmset(key, hash)
                    , key);
            return RedisMagicConstants.OK.equals(res) ? 1 : 0L;
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public String hget(String key, String field) {
        return (String) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().hget(key, field).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().hget(key, field);
        }, key);
    }

    @Override
    public Boolean hsetnx(String key, String field, String value, int seconds) {
        try {
            return (Boolean) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().hsetnx(key, field, value).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().hsetnx(key, field, value)
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public String hmset(String key, Map<String, String> hash, int seconds) {
        try {
            return (String) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().hmset(key, hash).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().hmset(key, hash)
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public List<String> hmget(String key, String[] fields) {
        List<KeyValue<String, Object>> list = (List<KeyValue<String, Object>>) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().hmget(key, fields).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().hmget(key, fields);
        }, key);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<String> res = new ArrayList<>();
        for (KeyValue<String, Object> stringObjectKeyValue : list) {
            stringObjectKeyValue.ifHasValue(e -> {
                res.add((String) stringObjectKeyValue.getValue());
            });
            stringObjectKeyValue.ifEmpty(() -> {
                res.add(null);
            });
        }
        return res;
    }

    @Override
    public Map<String, Object> hmgetToMap(String key, String[] fields) {
        List<KeyValue<String, Object>> list = (List<KeyValue<String, Object>>) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().hmget(key, fields).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().hmget(key, fields);
        }, key);
        if (CollectionUtils.isEmpty(list)) {
            return new HashMap<>(2);
        }
        Map<String, Object> res = new HashMap<>(list.size());
        for (KeyValue<String, Object> stringObjectKeyValue : list) {
            stringObjectKeyValue.ifHasValue(e -> {
                res.put(stringObjectKeyValue.getKey(), stringObjectKeyValue.getValue());
            });
        }
        return res;
    }

    @Override
    public Map<String, Object> hmgetToMapCanNull(String key, String[] fields) {
        List<KeyValue<String, Object>> list = (List<KeyValue<String, Object>>) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().hmget(key, fields).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().hmget(key, fields);
        }, key);
        if (CollectionUtils.isEmpty(list)) {
            return new HashMap<>(2);
        }
        Map<String, Object> res = new HashMap<>(list.size());
        for (KeyValue<String, Object> stringObjectKeyValue : list) {
            stringObjectKeyValue.ifHasValue(e -> {
                res.put(stringObjectKeyValue.getKey(), stringObjectKeyValue.getValue());
            });
            stringObjectKeyValue.ifEmpty(() -> {
                res.put(stringObjectKeyValue.getKey(), null);
            });
        }
        return res;
    }

    @Override
    public Long hincrBy(String key, String field, long value, int seconds) {
        try {
            return (Long) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().hincrby(key, field, value).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().hincrby(key, field, value)
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public Double hincrByFloat(String key, String field, double value, int seconds) {
        try {
            return (Double) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().hincrbyfloat(key, field, value).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().hincrbyfloat(key, field, value)
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public Boolean hexists(String key, String field) {
        return (Boolean) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().hexists(key, field).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().hexists(key, field);
        }, key);
    }

    @Override
    public Long hdel(String key, String field) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().hdel(key, field).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().hdel(key, field);
        }, key);
    }

    @Override
    public Long hdel(String key, String[] fields) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().hdel(key, fields).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().hdel(key, fields);
        }, key);
    }

    @Override
    public Long hlen(String key) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().hlen(key).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().hlen(key);
        }, key);
    }

    @Override
    public Set<String> hkeys(String key) {
        return (Set<String>) this.execute(() -> {
            List hkeys =
                    this.asyncGet() ? ((Future<List>) this.getAsyncExecutor().hkeys(key)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().hkeys(key);
            if (CollectionUtils.isNotEmpty(hkeys)) {
                return new LinkedHashSet<>(hkeys);
            }
            return null;
        }, key);
    }

    @Override
    public List<String> hvals(String key) {
        return (List<String>) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<List>) this.getAsyncExecutor().hvals(key)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().hvals(key);
        }, key);
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        return (Map<String, String>) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().hgetall(key).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().hgetall(key);
        }, key);
    }

    @Override
    public Long rpush(String key, String string, int seconds) {
        try {
            return (Long) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().rpush(key, string).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().rpush(key, string)
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public Long lpush(String key, String string, int seconds) {
        try {
            return (Long) this.execute(() -> {
                return
                        this.asyncGet() ? this.getAsyncExecutor().lpush(key, string).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                this.sync().lpush(key, string);
            }, key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public Long rpush(String key, String[] strings, int seconds) {
        try {
            return (Long) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().rpush(key, strings).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().rpush(key, strings)
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public Long lpush(String key, String[] strings, int seconds) {
        try {
            return (Long) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().lpush(key, strings).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().lpush(key, strings)
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public Long llen(String key) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().llen(key).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().llen(key);
        }, key);
    }

    @Override
    public List<String> lrange(String key, long start, long stop) {
        return (List<String>) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().lrange(key, start, stop).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().lrange(key, start, stop);
        }, key);
    }

    @Override
    public String ltrim(String key, long start, long stop) {
        return (String) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().ltrim(key, start, stop).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().ltrim(key, start, stop);
        }, key);
    }

    @Override
    public String lindex(String key, long index) {
        return (String) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().lindex(key, index).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().lindex(key, index);
        }, key);
    }

    @Override
    public String lset(String key, long index, String value, int seconds) {
        try {
            return (String) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().lset(key, index, value).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().lset(key, index, value)
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public Long lrem(String key, long count, String value) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().lrem(key, count, value).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().lrem(key, count, value);
        }, key);
    }

    @Override
    public String lpop(String key) {
        return (String) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().lpop(key).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().lpop(key);
        }, key);
    }

    @Override
    public String rpop(String key) {
        return (String) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().rpop(key).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().rpop(key);
        }, key);
    }

    @Override
    public String rpoplpush(String srckey, String dstkey, int seconds) {
        try {
            return (String) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().rpoplpush(srckey, dstkey).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().rpoplpush(srckey, dstkey)
                    , srckey);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(dstkey, seconds));
            }
        }
    }

    @Override
    public Long sadd(String key, String member, int seconds) {
        try {
            return (Long) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().sadd(key, member).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().sadd(key, member)
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public Long sadd(String key, String[] members, int seconds) {
        try {
            return (Long) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().sadd(key, members).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().sadd(key, members)
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public Set<String> smembers(String key) {
        return (Set<String>) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().smembers(key).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().smembers(key);
        }, key);
    }

    @Override
    public Long srem(String key, String member) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().srem(key, member).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().srem(key, member);
        }, key);
    }

    @Override
    public Long srem(String key, String[] members) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().srem(key, members).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().srem(key, members);
        }, key);
    }

    @Override
    public String spop(String key) {
        return (String) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().spop(key).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().spop(key);
        }, key);
    }

    @Override
    public Set<String> spop(String key, long count) {
        return (Set<String>) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().spop(key, count).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().spop(key, count);
        }, key);
    }

    @Override
    public Boolean smove(String srckey, String dstkey, String member) {
        return (Boolean) this.execute(() ->
                        this.asyncGet() ? this.getAsyncExecutor().smove(srckey, dstkey, member).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                this.sync().smove(srckey, dstkey, member)
                , srckey);
    }

    @Override
    public Long scard(String key) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().scard(key).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().scard(key);
        }, key);
    }

    @Override
    public Boolean sismember(String key, String member) {
        return (Boolean) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().sismember(key, member).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().sismember(key, member);
        }, key);
    }

    @Override
    public Set<String> sinter(String... keys) {
        return (Set<String>) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().sinter(keys).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().sinter(keys);
        }, keys);
    }

    @Override
    public Long sinterstore(String dstkey, int seconds, String... keys) {
        try {
            return (Long) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().sinterstore(dstkey, keys).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().sinterstore(dstkey, keys)
                    , keys);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(dstkey, seconds));
            }
        }
    }

    @Override
    public Set<String> sunion(String... keys) {
        return (Set<String>) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().sunion(keys).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().sunion(keys);
        }, keys);
    }

    @Override
    public Long sunionstore(String dstkey, int seconds, String... keys) {
        try {
            return (Long) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().sunionstore(dstkey, keys).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().sunionstore(dstkey, keys)
                    , keys);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(dstkey, seconds));
            }
        }
    }

    @Override
    public Set<String> sdiff(String... keys) {
        return (Set<String>) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().sdiff(keys).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().sdiff(keys);
        }, keys);
    }

    @Override
    public Long sdiffstore(String dstkey, int seconds, String... keys) {
        try {
            return (Long) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().sdiffstore(dstkey, keys).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().sdiffstore(dstkey, keys)
                    , keys);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(dstkey, seconds));
            }
        }
    }

    @Override
    public String srandmember(String key) {
        return (String) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().srandmember(key).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().srandmember(key);
        }, key);
    }

    @Override
    public List<String> srandmember(String key, int count) {
        return (List<String>) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().srandmember(key, count).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().srandmember(key, count);
        }, key);
    }

    @Override
    public Long zadd(String key, double score, String member, int seconds) {
        try {
            return (Long) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().zadd(key, score, member).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().zadd(key, score, member)
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public Long zadd(String key, double score, String member, ZAddParams params, int seconds) {
        try {
            return (Long) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().zadd(key, this.zAddParamsToZaddArgs(params), score, member).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().zadd(key, this.zAddParamsToZaddArgs(params), score, member)
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    private ZAddArgs zAddParamsToZaddArgs(ZAddParams params) {
        ZAddArgs zAddArgs = new ZAddArgs();
        if (null != params) {
            if (null != params.getParam(RedisMagicConstants.NX)) {
                zAddArgs.nx();
            }
            if (null != params.getParam(RedisMagicConstants.XX)) {
                zAddArgs.xx();
            }
            if (null != params.getParam(RedisMagicConstants.CH)) {
                zAddArgs.ch();
            }
        }
        return zAddArgs;
    }

    private ZAddArgs zIncrByParamsToZaddArgs(ZIncrByParams params) {
        ZAddArgs zAddArgs = new ZAddArgs();
        if (null != params) {
            if (null != params.getParam(RedisMagicConstants.NX)) {
                zAddArgs.nx();
            }
            if (null != params.getParam(RedisMagicConstants.XX)) {
                zAddArgs.xx();
            }
            if (null != params.getParam(RedisMagicConstants.CH)) {
                zAddArgs.ch();
            }
        }
        return zAddArgs;
    }

    private Object[] scoreMembersToObjects(Map<String, Double> scoreMembers) {
        Object[] scoresAndValues = new Object[scoreMembers.size() * 2];
        int i = 0;
        for (Map.Entry<String, Double> m : scoreMembers.entrySet()) {
            scoresAndValues[i++] = m.getValue();
            scoresAndValues[i++] = m.getKey();
        }
        return scoresAndValues;
    }

    @Override
    public Long zadd(String key, Map<String, Double> scoreMembers, int seconds) {
        try {
            return (Long) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().zadd(key, this.scoreMembersToObjects(scoreMembers)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().zadd(key, this.scoreMembersToObjects(scoreMembers))
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params, int seconds) {
        try {
            return (Long) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().zadd(key, this.zAddParamsToZaddArgs(params), this.scoreMembersToObjects(scoreMembers)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().zadd(key, this.zAddParamsToZaddArgs(params), this.scoreMembersToObjects(scoreMembers))
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public Set<String> zrange(String key, long start, long stop) {
        return (Set<String>) this.execute(() -> {
            List zrange =
                    this.asyncGet() ? ((Future<List>) this.getAsyncExecutor().zrange(key, start, stop)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrange(key, start, stop);
            if (CollectionUtils.isNotEmpty(zrange)) {
                return new LinkedHashSet<>(zrange);
            }
            return null;
        }, key);
    }

    @Override
    public Long zrem(String key, String member) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().zrem(key, member).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrem(key, member);
        }, key);
    }

    @Override
    public Long zrem(String key, String[] members) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().zrem(key, members).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrem(key, members);
        }, key);
    }

    @Override
    public Double zincrby(String key, double increment, String member, int seconds) {
        try {
            return (Double) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().zincrby(key, increment, member).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().zincrby(key, increment, member)
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public Double zincrby(String key, double increment, String member, ZIncrByParams params, int seconds) {
        try {
            return (Double) this.execute(() ->
                            this.asyncGet() ? this.getAsyncExecutor().zaddincr(key, this.zIncrByParamsToZaddArgs(params), increment, member).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().zaddincr(key, this.zIncrByParamsToZaddArgs(params), increment, member)
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public Long zrank(String key, String member) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().zrank(key, member).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrank(key, member);
        }, key);
    }

    @Override
    public Long zrevrank(String key, String member) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().zrevrank(key, member).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrevrank(key, member);
        }, key);
    }

    @Override
    public Set<String> zrevrange(String key, long start, long stop) {
        return (Set<String>) this.execute(() -> {
            List zrevrange =
                    this.asyncGet() ? ((Future<List>) this.getAsyncExecutor().zrevrange(key, start, stop)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrevrange(key, start, stop);
            if (CollectionUtils.isNotEmpty(zrevrange)) {
                return new LinkedHashSet<>(zrevrange);
            }
            return null;
        }, key);
    }

    /**
     * 类型转换
     *
     * @param list
     * @return
     */
    private Set<Tuple> scoreValuesToTuples(List<ScoredValue> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            Set<Tuple> res = new LinkedHashSet<>();
            for (ScoredValue scoredValue : list) {
                if (null != scoredValue) {
                    res.add(new Tuple((String) scoredValue.getValue(), scoredValue.getScore()));
                }
            }
            return res;
        }
        return new LinkedHashSet<>();
    }

    private List<Tuple> scoreValuesToTupleList(List<ScoredValue> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            List<Tuple> res = new ArrayList<>();
            for (ScoredValue scoredValue : list) {
                res.add(new Tuple((String) scoredValue.getValue(), scoredValue.getScore()));
            }
            return res;
        }
        return new ArrayList<>();
    }

    @Override
    public Set<Tuple> zrangeWithScores(String key, long start, long stop) {
        return (Set<Tuple>) this.execute(() -> {
            return this.scoreValuesToTuples(
                    this.asyncGet() ? ((Future<List<ScoredValue>>) this.getAsyncExecutor().zrangeWithScores(key, start, stop)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrangeWithScores(key, start, stop));
        }, key);
    }

    @Override
    public Set<Tuple> zrevrangeWithScores(String key, long start, long stop) {
        return (Set<Tuple>) this.execute(() -> {
            return this.scoreValuesToTuples(
                    this.asyncGet() ? ((Future<List<ScoredValue>>) this.getAsyncExecutor().zrevrangeWithScores(key, start, stop)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrevrangeWithScores(key, start, stop));
        }, key);
    }

    @Override
    public Long zcard(String key) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().zcard(key).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zcard(key);
        }, key);
    }

    @Override
    public Double zscore(String key, String member) {
        return (Double) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().zscore(key, member).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zscore(key, member);
        }, key);
    }

    @Override
    public ScoredValue zpopmax(String key) {
        return (ScoredValue) this.execute(() ->
                        this.asyncGet() ? this.getAsyncExecutor().zpopmax(key).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                this.sync().zpopmax(key)
                , key);
    }

    @Override
    public List<ScoredValue> zpopmax(String key, int count) {
        return (List<ScoredValue>) this.execute(() ->
                        this.asyncGet() ? this.getAsyncExecutor().zpopmax(key, count).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                this.sync().zpopmax(key, count)
                , key);
    }

    @Override
    public ScoredValue zpopmin(String key) {
        return (ScoredValue) this.execute(() ->
                        this.asyncGet() ? this.getAsyncExecutor().zpopmin(key).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                this.sync().zpopmin(key)
                , key);
    }

    @Override
    public List<ScoredValue> zpopmin(String key, int count) {
        return (List<ScoredValue>) this.execute(() ->
                        this.asyncGet() ? this.getAsyncExecutor().zpopmin(key, count).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                this.sync().zpopmin(key, count)
                , key);
    }

    @Override
    public KeyValue<String, ScoredValue> bzpopmax(long timeout, String... keys) {
        //如果是loop模式，不支持
        if (ConnectTypeEnum.LOOP == getConnectTypeEnum()) {
            CacheExceptionFactory.throwException("mget不支持loop模式");
        }
        return (KeyValue<String, ScoredValue>) this.execute(() ->
                        this.asyncGet() ? this.getAsyncExecutor().bzpopmax(timeout, keys).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                this.sync().bzpopmax(timeout, keys)
                , keys);
    }

    @Override
    public KeyValue<String, ScoredValue> bzpopmin(long timeout, String... keys) {
        //如果是loop模式，不支持
        if (ConnectTypeEnum.LOOP == getConnectTypeEnum()) {
            CacheExceptionFactory.throwException("mget不支持loop模式");
        }
        return (KeyValue<String, ScoredValue>) this.execute(() ->
                        this.asyncGet() ? this.getAsyncExecutor().bzpopmin(timeout, keys).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                this.sync().bzpopmin(timeout, keys)
                , keys);
    }

    @Override
    public List<String> sort(String key) {
        return (List<String>) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().sort(key).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().sort(key);
        }, key);
    }

    @Override
    public List<String> sort(String key, SortingParams sortingParameters) {
        CacheExceptionFactory.throwException("Lettuce暂不支持此命令，请使用sort(String key, SortArgs sortArgs)");
        return null;
    }

    @Override
    public List<String> sort(String key, SortArgs sortArgs) {
        return (List<String>) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().sort(key, sortArgs).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().sort(key, sortArgs);
        }, key);
    }

    @Override
    public Long sort(String key, SortingParams sortingParameters, String dstkey) {
        CacheExceptionFactory.throwException("Lettuce暂不支持此命令，请使用sort(String key, SortArgs sortArgs, String dstkey)");
        return null;
    }

    @Override
    public Long sort(String key, SortArgs sortArgs, String dstkey) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().sortStore(key, sortArgs, dstkey).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().sortStore(key, sortArgs, dstkey);
        }, key);
    }

    @Override
    public Long sort(String key, String dstkey) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().sortStore(key, new SortArgs(), dstkey).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().sortStore(key, new SortArgs(), dstkey);
        }, key);
    }

    @Override
    public List<String> blpop(int timeout, String... keys) {
        final int defaultTimeout = getTimeout();
        if (timeout >= (defaultTimeout / 1000 )) {
            CacheExceptionFactory.throwException("blpop timeout必须小于" + (defaultTimeout / 1000));
        }
        return (List<String>) this.execute(() -> {
            KeyValue blpop =
                    this.asyncGet() ? ((Future<KeyValue>) this.getAsyncExecutor().blpop(timeout, keys)).get(defaultTimeout, TimeUnit.MILLISECONDS) :
                            this.sync().blpop(timeout, keys);
            if (null != blpop) {
                List<String> res = new ArrayList<>();
                res.add((String) blpop.getValue());
                return res;
            }
            return null;
        }, keys);
    }

    @Override
    public List<String> brpop(int timeout, String[] keys) {
        //如果是loop模式，不支持
        if (ConnectTypeEnum.LOOP == getConnectTypeEnum()) {
            CacheExceptionFactory.throwException("brpop不支持loop模式");
        }
        final int defaultTimeout = getTimeout();
        if (timeout >= (defaultTimeout / 1000 )) {
            CacheExceptionFactory.throwException("brpop timeout必须小于" + (defaultTimeout / 1000));
        }
        return (List<String>) this.execute(() -> {
            KeyValue brpop =
                    this.asyncGet() ? ((Future<KeyValue>) this.getAsyncExecutor().brpop(timeout, keys)).get(defaultTimeout, TimeUnit.MILLISECONDS) :
                            this.sync().brpop(timeout, keys);
            if (null != brpop) {
                List<String> res = new ArrayList<>();
                res.add((String) brpop.getValue());
                return res;
            }
            return null;
        }, keys);
    }

    @Override
    public Long zcount(String key, double min, double max) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().zcount(key, min, max).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zcount(key, min, max);
        }, key);
    }

    @Override
    public Long zcount(String key, String min, String max) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? this.getAsyncExecutor().zcount(key, min, max).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zcount(key, min, max);
        }, key);
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max) {
        return (Set<String>) this.execute(() -> {
            List zrangebyscore =
                    this.asyncGet() ? ((Future<List>)this.getAsyncExecutor().zrangebyscore(key, min, max)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrangebyscore(key, min, max);
            if (CollectionUtils.isNotEmpty(zrangebyscore)) {
                return new LinkedHashSet<>(zrangebyscore);
            }
            return null;
        }, key);
    }

    @Override
    public Set<String> zrangeByScore(String key, String min, String max) {
        return (Set<String>) this.execute(() -> {
            List list =
                    this.asyncGet() ? ((Future<List>)this.getAsyncExecutor().zrangebyscore(key, min, max)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrangebyscore(key, min, max);
            if (CollectionUtils.isNotEmpty(list)) {
                return new LinkedHashSet<>(list);
            }
            return null;
        }, key);
    }

    @Override
    public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
        return (Set<String>) this.execute(() -> {
            List zrangebyscore =
                    this.asyncGet() ? ((Future<List>)this.getAsyncExecutor().zrangebyscore(key, min, max, offset, count)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrangebyscore(key, min, max, offset, count);
            if (CollectionUtils.isNotEmpty(zrangebyscore)) {
                return new LinkedHashSet<>(zrangebyscore);
            }
            return null;
        }, key);
    }

    @Override
    public Set<String> zrangeByScore(String key, String min, String max, int offset, int count) {
        return (Set<String>) this.execute(() -> {
            List zrangebyscore =
                    this.asyncGet() ? ((Future<List>)this.getAsyncExecutor().zrangebyscore(key, min, max, offset, count)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrangebyscore(key, min, max, offset, count);
            if (CollectionUtils.isNotEmpty(zrangebyscore)) {
                return new LinkedHashSet<>(zrangebyscore);
            }
            return null;
        }, key);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
        return (Set<Tuple>) this.execute(() -> {
            return this.scoreValuesToTuples(
                    this.asyncGet() ? ((Future<List>)this.getAsyncExecutor().zrangebyscoreWithScores(key, min, max)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrangebyscoreWithScores(key, min, max)
            );
        }, key);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
        return (Set<Tuple>) this.execute(() -> {
            return this.scoreValuesToTuples(
                    this.asyncGet() ? ((Future<List>)this.getAsyncExecutor().zrangebyscoreWithScores(key, min, max)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrangebyscoreWithScores(key, min, max)
            );
        }, key);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
        return (Set<Tuple>) this.execute(() -> {
            return this.scoreValuesToTuples(
                    this.asyncGet() ? ((Future<List>)this.getAsyncExecutor().zrangebyscoreWithScores(key, min, max, offset, count)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrangebyscoreWithScores(key, min, max, offset, count)
            );
        }, key);
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
        return (Set<Tuple>) this.execute(() -> {
            return this.scoreValuesToTuples(
                    this.asyncGet() ? ((Future<List>)this.getAsyncExecutor().zrangebyscoreWithScores(key, min, max, offset, count)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrangebyscoreWithScores(key, min, max, offset, count)
            );
        }, key);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, double max, double min) {
        return (Set<String>) this.execute(() -> {
            List zrevrangebyscore =
                    this.asyncGet() ? ((Future<List>)this.getAsyncExecutor().zrevrangebyscore(key, max, min)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrevrangebyscore(key, max, min);
            if (CollectionUtils.isNotEmpty(zrevrangebyscore)) {
                return new HashSet<>(zrevrangebyscore);
            }
            return new LinkedHashSet<>();
        }, key);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, String max, String min) {
        return (Set<String>) this.execute(() -> {
            List list =
                    //todo 之前同步命令min和max写反了
                    this.asyncGet() ? ((Future<List>)this.getAsyncExecutor().zrevrangebyscore(key, min, max)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrevrangebyscore(key, min, max);
            if (CollectionUtils.isNotEmpty(list)) {
                return new LinkedHashSet<>(list);
            }
            return new LinkedHashSet<>();
        }, key);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
        return (Set<String>) this.execute(() -> {
            List list =
                    //todo 之前同步命令min和max写反了
                    this.asyncGet() ? ((Future<List>)this.getAsyncExecutor().zrevrangebyscore(key, min, max, offset, count)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrevrangebyscore(key, min, max, offset, count);
            if (CollectionUtils.isNotEmpty(list)) {
                return new LinkedHashSet<>(list);
            }
            return new LinkedHashSet<>();
        }, key);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, Range<? extends Number> range, Limit limit) {
        return (Set<String>) this.execute(() -> {
            List list =
                    this.asyncGet() ? ((Future<List>)this.getAsyncExecutor().zrevrangebyscore(key, range, limit)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrevrangebyscore(key, range, limit);
            if (CollectionUtils.isNotEmpty(list)) {
                return new LinkedHashSet<>(list);
            }
            return new LinkedHashSet<>();
        }, key);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
        return (Set<Tuple>) this.execute(() -> {
            return this.scoreValuesToTuples(
                    this.asyncGet() ? ((Future<List>)this.getAsyncExecutor().zrevrangebyscoreWithScores(key, max, min)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrevrangebyscoreWithScores(key, max, min)
            );
        }, key);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
        return (Set<Tuple>) this.execute(() -> {
            return this.scoreValuesToTuples(
                    this.asyncGet() ? ((Future<List>)this.getAsyncExecutor().zrevrangebyscoreWithScores(key, max, min, offset, count)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrevrangebyscoreWithScores(key, max, min, offset, count)
            );
        }, key);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
        return (Set<Tuple>) this.execute(() -> {
            return this.scoreValuesToTuples(
                    this.asyncGet() ? ((Future<List>)this.getAsyncExecutor().zrevrangebyscoreWithScores(key, max, min, offset, count)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrevrangebyscoreWithScores(key, max, min, offset, count)
            );
        }, key);
    }

    @Override
    public Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
        return (Set<String>) this.execute(() -> {
            List list =
                    this.asyncGet() ? ((Future<List>)this.getAsyncExecutor().zrevrangebyscore(key, max, min, offset, count)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrevrangebyscore(key, max, min, offset, count);
            if (CollectionUtils.isNotEmpty(list)) {
                return new LinkedHashSet<>(list);
            }
            return new LinkedHashSet<>();
        }, key);
    }

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
        return (Set<Tuple>) this.execute(() -> {
            return this.scoreValuesToTuples(
                    this.asyncGet() ? ((Future<List>)this.getAsyncExecutor().zrevrangebyscoreWithScores(key, max, min)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrevrangebyscoreWithScores(key, max, min)
            );
        }, key);
    }

    @Override
    public Long zremrangeByRank(String key, long start, long stop) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().zremrangebyrank(key, start, stop)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zremrangebyrank(key, start, stop);
        }, key);
    }

    @Override
    public Long zremrangeByScore(String key, double min, double max) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().zremrangebyscore(key, min, max)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zremrangebyscore(key, min, max);
        }, key);
    }

    @Override
    public Long zremrangeByScore(String key, String min, String max) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().zremrangebyscore(key, min, max)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zremrangebyscore(key, min, max);
        }, key);
    }

    @Override
    public Long zunionstore(String dstkey, String... sets) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().zunionstore(dstkey, sets)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zunionstore(dstkey, sets);
        }, sets);
    }

    private ZStoreArgs zParamsToZstoreArgs(ZParams params) {
        ZStoreArgs zStoreArgs = new ZStoreArgs();
        Collection<byte[]> paramsList = params.getParams();
        if (CollectionUtils.isEmpty(paramsList)) {
            CacheExceptionFactory.throwException("ZParams to ZStoreArgs error ! ZParams is empty !");
            return null;
        }
        Map<byte[], byte[]> paramsMap = new HashMap<>(paramsList.size() >> 1);
        byte[] key = null;
        int i = 0;
        for (byte[] bytes : paramsList) {
            i++;
            if (i % 2 > 0) {
                key = bytes;
            } else {
                paramsMap.put(key, bytes);
            }
        }
        if (null != paramsMap.get(WEIGHTS.raw)) {
            zStoreArgs.weights(Double.valueOf(SafeEncoder.encode(paramsMap.get(WEIGHTS.raw))));
        }
        if (null != paramsMap.get(AGGREGATE.raw)) {
            ZParams.Aggregate aggregate = ZParams.Aggregate.valueOf(SafeEncoder.encode(paramsMap.get(AGGREGATE.raw)));
            switch (aggregate) {
                case MAX:
                    zStoreArgs.max();
                    break;
                case MIN:
                    zStoreArgs.min();
                    break;
                case SUM:
                    zStoreArgs.sum();
                    break;
                default:
                    break;
            }
        }
        return zStoreArgs;
    }

    @Override
    public Long zunionstore(String dstkey, ZParams params, String... sets) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().zunionstore(dstkey, this.zParamsToZstoreArgs(params), sets)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zunionstore(dstkey, this.zParamsToZstoreArgs(params), sets);
        }, sets);
    }

    @Override
    public Long zinterstore(String dstkey, String... sets) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().zinterstore(dstkey, sets)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zinterstore(dstkey, sets);
        }, sets);
    }

    @Override
    public Long zinterstore(String dstkey, ZParams params, String... sets) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().zinterstore(dstkey, this.zParamsToZstoreArgs(params), sets)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zinterstore(dstkey, this.zParamsToZstoreArgs(params), sets);
        }, sets);
    }

    @Override
    public Long zlexcount(String key, String min, String max) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().zlexcount(key, min, max)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zlexcount(key, min, max);
        }, key);
    }

    @Override
    public Set<String> zrangeByLex(String key, String min, String max) {
        return (Set<String>) this.execute(() -> {
            List list =
                    this.asyncGet() ? ((Future<List>)this.getAsyncExecutor().zrangebylex(key, min, max)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrangebylex(key, min, max);
            if (CollectionUtils.isNotEmpty(list)) {
                return new LinkedHashSet<>(list);
            }
            return new LinkedHashSet<>();
        }, key);
    }

    @Override
    public Set<String> zrangeByLex(String key, String min, String max, int offset, int count) {
        return (Set<String>) this.execute(() -> {
            List list =
                    this.asyncGet() ? ((Future<List>)this.getAsyncExecutor().zrangebylex(key, min, max, offset, count)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrangebylex(key, min, max, offset, count);
            if (CollectionUtils.isNotEmpty(list)) {
                return new LinkedHashSet<>(list);
            }
            return new LinkedHashSet<>();
        }, key);
    }

    @Override
    public Set<String> zrevrangeByLex(String key, String max, String min) {
        //这里min、max与Jedis不一样，位置替换
        return (Set<String>) this.execute(() -> {
            List list =
                    this.asyncGet() ? ((Future<List>)this.getAsyncExecutor().zrangebylex(key, min, max)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrangebylex(key, min, max);
            if (CollectionUtils.isNotEmpty(list)) {
                return new LinkedHashSet<>(list);
            }
            return new LinkedHashSet<>();
        }, key);
    }

    @Override
    public Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
        return (Set<String>) this.execute(() -> {
            List list =
                    this.asyncGet() ? ((Future<List>)this.getAsyncExecutor().zrangebylex(key, min, max, offset, count)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zrangebylex(key, min, max, offset, count);
            if (CollectionUtils.isNotEmpty(list)) {
                return new LinkedHashSet<>(list);
            }
            return new LinkedHashSet<>();
        }, key);
    }

    @Override
    public Long zremrangeByLex(String key, String min, String max) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().zremrangebylex(key, min, max)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zremrangebylex(key, min, max);
        }, key);
    }

    @Override
    public Long strlen(String key) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().strlen(key)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().strlen(key);
        }, key);
    }

    @Override
    public Long lpushx(String key, String... string) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().lpushx(key, string)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().lpushx(key, string);
        }, key);
    }

    @Override
    public Long rpushx(String key, String... string) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().rpushx(key, string)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().rpushx(key, string);
        }, key);
    }

    @Override
    public String echo(String string) {
        return (String) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<String>)this.getAsyncExecutor().echo(string)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().echo(string);
        }, string);
    }

    @Override
    public Long linsert(String key, Client.LIST_POSITION where, String pivot, String value, int seconds) {
        try {
            return (Long) this.execute(() ->
                            this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().linsert(key, where == Client.LIST_POSITION.BEFORE, pivot, value)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().linsert(key, where == Client.LIST_POSITION.BEFORE, pivot, value)
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public String brpoplpush(String source, String destination, int timeout) {
        //如果是loop模式，不支持
        if (ConnectTypeEnum.LOOP == getConnectTypeEnum()) {
            CacheExceptionFactory.throwException("mget不支持loop模式");
        }
        return (String) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<String>)this.getAsyncExecutor().brpoplpush(timeout, source, destination)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().brpoplpush(timeout, source, destination);
        }, source);
    }

    @Override
    public Boolean setbit(String key, long offset, boolean value, int seconds) {
        try {
            return (Boolean) this.execute(() ->
                            (
                                    this.asyncGet() ? ((Future<Long>) this.getAsyncExecutor().setbit(key, offset, value ? 1 : 0)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                            this.sync().setbit(key, offset, value ? 1 : 0)
                            )
                                    > 0 ? Boolean.TRUE : Boolean.FALSE
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public Boolean setbit(String key, long offset, String value, int seconds) {
        if (!RedisMagicConstants.ONE.equals(value) && !RedisMagicConstants.ZERO.equals(value)) {
            CacheExceptionFactory.throwException("Lettuce->setbit(String key, long offset, String value) value必须为0或1");
            return false;
        }
        try {
            return (Boolean) this.execute(() ->
                            (
                                    this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().setbit(key, offset, Integer.parseInt(value))).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                            this.sync().setbit(key, offset, Integer.parseInt(value))
                            )
                                    > 0 ? Boolean.TRUE : Boolean.FALSE
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public Boolean getbit(String key, long offset) {
        return (Boolean) this.execute(() -> {
            return
                    (
                            this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().getbit(key, offset)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().getbit(key, offset)
                    )
                            > 0 ? Boolean.TRUE : Boolean.FALSE;
        }, key);
    }

    @Override
    public Long setrange(String key, long offset, String value) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().setrange(key, offset, value)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().setrange(key, offset, value);
        }, key);
    }

    @Override
    public String getrange(String key, long startOffset, long endOffset) {
        return (String) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<String>)this.getAsyncExecutor().getrange(key, startOffset, endOffset)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().getrange(key, startOffset, endOffset);
        }, key);
    }

    @Override
    public Long bitpos(String key, boolean value) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().bitpos(key, value)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().bitpos(key, value);
        }, key);
    }

    @Override
    public Long bitpos(String key, boolean state, long start, long end) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().bitpos(key, state, start, end)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().bitpos(key, state, start, end);
        }, key);
    }

    @Override
    public void subscribe(JedisPubSub jedisPubSub, String[] channels) {
        StatefulRedisPubSubConnection<String, String> lettucePubSubConnection = this.getPubSubConnection();
        lettucePubSubConnection.addListener(new RedisPubSubListener() {
            @Override
            public void message(Object channel, Object message) {
                jedisPubSub.onMessage((String) channel, (String) message);
            }

            @Override
            public void message(Object pattern, Object channel, Object message) {
                jedisPubSub.onPMessage((String) pattern, (String) channel, (String) message);
            }

            @Override
            public void subscribed(Object channel, long count) {
                jedisPubSub.onSubscribe((String) channel, (int) count);
            }

            @Override
            public void psubscribed(Object pattern, long count) {
                jedisPubSub.onPSubscribe((String) pattern, (int) count);
            }

            @Override
            public void unsubscribed(Object channel, long count) {
                jedisPubSub.onUnsubscribe((String) channel, (int) count);
            }

            @Override
            public void punsubscribed(Object pattern, long count) {
                jedisPubSub.onPUnsubscribe((String) pattern, (int) count);
            }
        });
        this.execute(() -> {
            lettucePubSubConnection.sync().subscribe(channels);
            return null;
        }, channels);
    }

    @Override
    public void subscribe(JedisPubSub jedisPubSub, String channel) {
        this.subscribe(jedisPubSub, new String[]{channel});
    }

    @Override
    public void subscribe(InterfacePubSubModel pubSubModel, String channel) {
        this.subscribe(pubSubModel, new String[]{channel});
    }

    @Override
    public void subscribe(InterfacePubSubModel pubSubModel, String[] channels) {
        StatefulRedisPubSubConnection<String, String> lettucePubSubConnection = this.getPubSubConnection();
        lettucePubSubConnection.addListener(new RedisPubSubListener() {
            @Override
            public void message(Object channel, Object message) {
                pubSubModel.onMessage((String) message);
            }

            @Override
            public void message(Object pattern, Object channel, Object message) {
            }

            @Override
            public void subscribed(Object channel, long count) {
            }

            @Override
            public void psubscribed(Object pattern, long count) {
            }

            @Override
            public void unsubscribed(Object channel, long count) {
            }

            @Override
            public void punsubscribed(Object pattern, long count) {
            }
        });
        this.execute(() -> {
            lettucePubSubConnection.sync().subscribe(channels);
            return null;
        }, channels);
    }

    @Override
    public Long publish(String channel, String message) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().publish(channel, message)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().publish(channel, message);
        }, channel);
    }

    @Override
    public void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
        StatefulRedisPubSubConnection<String, String> lettucePubSubConnection = this.getPubSubConnection();
        lettucePubSubConnection.addListener(new RedisPubSubListener() {
            @Override
            public void message(Object channel, Object message) {
                jedisPubSub.onMessage((String) channel, (String) message);
            }

            @Override
            public void message(Object pattern, Object channel, Object message) {
                jedisPubSub.onPMessage((String) pattern, (String) channel, (String) message);
            }

            @Override
            public void subscribed(Object channel, long count) {
                jedisPubSub.onSubscribe((String) channel, (int) count);
            }

            @Override
            public void psubscribed(Object pattern, long count) {
                jedisPubSub.onPSubscribe((String) pattern, (int) count);
            }

            @Override
            public void unsubscribed(Object channel, long count) {
                jedisPubSub.onUnsubscribe((String) channel, (int) count);
            }

            @Override
            public void punsubscribed(Object pattern, long count) {
                jedisPubSub.onPUnsubscribe((String) pattern, (int) count);
            }
        });
        this.execute(() -> {
            lettucePubSubConnection.sync().psubscribe(patterns);
            return null;
        }, "");
    }

    @Override
    public List<Slowlog> slowlogGet() {
        //TODO 类型转换可能有问题
        return (List<Slowlog>) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<List<Slowlog>>)this.getAsyncExecutor().slowlogGet()).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().slowlogGet();
        }, "");
    }

    @Override
    public List<Slowlog> slowlogGet(long entries) {
        return (List<Slowlog>) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<List<Slowlog>>)this.getAsyncExecutor().slowlogGet((int) entries)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().slowlogGet((int) entries);
        }, "");
    }

    @Override
    public Long bitcount(String key) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().bitcount(key)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().bitcount(key);
        }, key);
    }

    @Override
    public Long bitcount(String key, long start, long end) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().bitcount(key, start, end)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().bitcount(key, start, end);
        }, key);
    }

    @Override
    public Long bitop(BitOP op, String destKey, String... srcKeys) {
        return (Long) this.execute(() -> {
            switch (op) {
                case OR:
                    return
                            this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().bitopOr(destKey, srcKeys)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().bitopOr(destKey, srcKeys);
                case AND:
                    return
                            this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().bitopAnd(destKey, srcKeys)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().bitopAnd(destKey, srcKeys);
                case NOT:
                    return
                            this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().bitopNot(destKey, srcKeys)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().bitopNot(destKey, srcKeys);
                default:
                    return
                            this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().bitopXor(destKey, srcKeys)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().bitopXor(destKey, srcKeys);
            }
        }, srcKeys);
    }

    @Override
    public Boolean pexpire(String key, long milliseconds) {
        return (Boolean) this.execute(() ->
                        this.asyncGet() ? ((Future<Boolean>)this.getAsyncExecutor().pexpire(key, milliseconds)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                this.sync().pexpire(key, milliseconds)
                , key);
    }

    @Override
    public Boolean pexpireAt(String key, long millisecondsTimestamp) {
        return (Boolean) this.execute(() ->
                        this.asyncGet() ? ((Future<Boolean>)this.getAsyncExecutor().pexpireat(key, millisecondsTimestamp)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                this.sync().pexpireat(key, millisecondsTimestamp)
                , key);
    }

    @Override
    public Long pttl(String key) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().pttl(key)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().pttl(key);
        }, key);
    }

    @Override
    public String psetex(String key, long milliseconds, String value) {
        return (String) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<String>)this.getAsyncExecutor().psetex(key, milliseconds, value)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().psetex(key, milliseconds, value);
        }, key);
    }

    @Override
    public ScanResult<String> scan(String cursor) {
        return (ScanResult<String>) this.execute(() -> {
            KeyScanCursor scan =
                    this.asyncGet() ? ((Future<KeyScanCursor>)this.getAsyncExecutor().scan(this.getScanCursor(cursor))).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().scan(this.getScanCursor(cursor));
            if (null != scan) {
                return new ScanResult<String>(scan.getCursor(), scan.getKeys());
            }
            return null;
        }, "");
    }

    @Override
    public ScanResult<String> scan(String cursor, ScanParams params) {
        return (ScanResult<String>) this.execute(() -> {
            KeyScanCursor scan =
                    this.asyncGet() ? ((Future<KeyScanCursor>)this.getAsyncExecutor().scan(this.getScanCursor(cursor), this.scanParamsToScanArgs(params))).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().scan(this.getScanCursor(cursor), this.scanParamsToScanArgs(params));
            if (null != scan) {
                return new ScanResult<String>(scan.getCursor(), scan.getKeys());
            }
            return null;
        }, "");
    }

    @Override
    public ScanResult<Map.Entry<String, String>> hscan(String key, String cursor) {
        return (ScanResult<Map.Entry<String, String>>) this.execute(() -> {
            MapScanCursor scan =
                    this.asyncGet() ? ((Future<MapScanCursor>)this.getAsyncExecutor().hscan(key, this.getScanCursor(cursor))).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().hscan(key, this.getScanCursor(cursor));
            if (null != scan) {
                return new ScanResult<Map.Entry<String, String>>(scan.getCursor(), new ArrayList<>(scan.getMap().entrySet()));
            }
            return null;
        }, key);
    }

    @Override
    public ScanResult<Map.Entry<String, String>> hscan(String key, String cursor, ScanParams params) {
        return (ScanResult<Map.Entry<String, String>>) this.execute(() -> {
            MapScanCursor scan =
                    this.asyncGet() ? ((Future<MapScanCursor>)this.getAsyncExecutor().hscan(key, this.getScanCursor(cursor), this.scanParamsToScanArgs(params))).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().hscan(key, this.getScanCursor(cursor), this.scanParamsToScanArgs(params));
            if (null != scan) {
                return new ScanResult<Map.Entry<String, String>>(scan.getCursor(), new ArrayList<>(scan.getMap().entrySet()));
            }
            return null;
        }, key);
    }

    @Override
    public ScanResult<String> sscan(String key, String cursor) {
        return (ScanResult<String>) this.execute(() -> {
            ValueScanCursor scan =
                    this.asyncGet() ? ((Future<ValueScanCursor>)this.getAsyncExecutor().sscan(key, this.getScanCursor(cursor))).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().sscan(key, this.getScanCursor(cursor));
            if (null != scan) {
                return new ScanResult<String>(scan.getCursor(), scan.getValues());
            }
            return null;
        }, key);
    }

    @Override
    public ScanResult<String> sscan(String key, String cursor, ScanParams params) {
        return (ScanResult<String>) this.execute(() -> {
            ValueScanCursor scan =
                    this.asyncGet() ? ((Future<ValueScanCursor>)this.getAsyncExecutor().sscan(key, this.getScanCursor(cursor), this.scanParamsToScanArgs(params))).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().sscan(key, this.getScanCursor(cursor), this.scanParamsToScanArgs(params));
            if (null != scan) {
                return new ScanResult<String>(scan.getCursor(), scan.getValues());
            }
            return null;
        }, key);
    }

    @Override
    public ScanResult<Tuple> zscan(String key, String cursor) {
        return (ScanResult<Tuple>) this.execute(() -> {
            ScoredValueScanCursor scan =
                    this.asyncGet() ? ((Future<ScoredValueScanCursor>)this.getAsyncExecutor().zscan(key, this.getScanCursor(cursor))).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zscan(key, this.getScanCursor(cursor));
            if (null != scan) {
                return new ScanResult<Tuple>(scan.getCursor(), this.scoreValuesToTupleList(scan.getValues()));
            }
            return null;
        }, key);
    }

    @Override
    public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
        return (ScanResult<Tuple>) this.execute(() -> {
            ScoredValueScanCursor scan =
                    this.asyncGet() ? ((Future<ScoredValueScanCursor>)this.getAsyncExecutor().zscan(key, this.getScanCursor(cursor), this.scanParamsToScanArgs(params))).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().zscan(key, this.getScanCursor(cursor), this.scanParamsToScanArgs(params));
            if (null != scan) {
                return new ScanResult<Tuple>(scan.getCursor(), this.scoreValuesToTupleList(scan.getValues()));
            }
            return null;
        }, key);
    }

    private ScanCursor getScanCursor(String cursor) {
        ScanCursor scanCursor = new ScanCursor();
        scanCursor.setCursor(cursor);
        return scanCursor;
    }

    private ScanArgs scanParamsToScanArgs(ScanParams params) {
        ScanArgs scanArgs = new ScanArgs();
        Collection<byte[]> paramsList = params.getParams();
        if (CollectionUtils.isEmpty(paramsList)) {
            CacheExceptionFactory.throwException("ScanParams to ScanArgs error ! ScanParams is empty !");
            return null;
        }
        Map<byte[], byte[]> paramsMap = new HashMap<>(paramsList.size() >> 1);
        byte[] key = null;
        int i = 0;
        for (byte[] bytes : paramsList) {
            i++;
            if (i % 2 > 0) {
                key = bytes;
            } else {
                paramsMap.put(key, bytes);
            }
        }
        if (null != paramsMap.get(COUNT.raw)) {
            scanArgs.limit(Long.parseLong(SafeEncoder.encode(paramsMap.get(COUNT.raw))));
        }
        if (null != paramsMap.get(MATCH.raw)) {
            scanArgs.match(new String(paramsMap.get(MATCH.raw)));
        }
        return scanArgs;
    }

    @Override
    public List<String> pubsubChannels(String pattern) {
        return (List<String>) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<List<String>>)this.getAsyncExecutor().pubsubChannels(pattern)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().pubsubChannels(pattern);
        }, "");
    }

    @Override
    public Long pubsubNumPat() {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().pubsubNumpat()).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().pubsubNumpat();
        }, "");
    }

    @Override
    public Map<String, String> pubsubNumSub(String... channels) {
        return (Map<String, String>) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Map<String, String>>)this.getAsyncExecutor().pubsubNumsub(channels)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().pubsubNumsub(channels);
        }, "");
    }

    @Override
    public Long pfadd(String key, int seconds, String... elements) {
        try {
            return (Long) this.execute(() ->
                            this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().pfadd(key, elements)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().pfadd(key, elements)
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public long pfcount(String key) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().pfcount(key)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().pfcount(key);
        }, key);
    }

    @Override
    public long pfcount(String... keys) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().pfcount(keys)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().pfcount(keys);
        }, keys);
    }

    @Override
    public String pfmerge(String destkey, int seconds, String... sourcekeys) {
        try {
            return (String) this.execute(() ->
                            this.asyncGet() ? ((Future<String>)this.getAsyncExecutor().pfmerge(destkey, sourcekeys)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().pfmerge(destkey, sourcekeys)
                    , sourcekeys);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(destkey, seconds));
            }
        }
    }

    @Override
    public List<String> blpop(int timeout, String key) {
        //如果是loop模式，不支持
        if (ConnectTypeEnum.LOOP == getConnectTypeEnum()) {
            CacheExceptionFactory.throwException("mget不支持loop模式");
        }
        //检测timeout时间
        final int defaultTimeout = getTimeout();
        if (timeout >= (defaultTimeout / 1000 )) {
            CacheExceptionFactory.throwException("blpop timeout必须小于" + (defaultTimeout / 1000));
        }
        KeyValue execute = (KeyValue) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<KeyValue>)this.getAsyncExecutor().blpop(timeout, key)).get(defaultTimeout, TimeUnit.MILLISECONDS) :
                            this.sync().blpop(timeout, key);
        }, key);
        if (null != execute) {
            ArrayList<String> strings = new ArrayList<>();
            strings.add(String.valueOf(execute.getValue()));
            return strings;
        }
        return null;
    }

    @Override
    public List<String> brpop(int timeout, String key) {
        //如果是loop模式，不支持
        if (ConnectTypeEnum.LOOP == getConnectTypeEnum()) {
            CacheExceptionFactory.throwException("mget不支持loop模式");
        }
        //检测timeout时间
        final int defaultTimeout = getTimeout();
        if (timeout >= (defaultTimeout / 1000 )) {
            CacheExceptionFactory.throwException("brpop timeout必须小于" + (defaultTimeout / 1000));
        }
        KeyValue execute = (KeyValue) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<KeyValue>)this.getAsyncExecutor().brpop(timeout, key)).get(defaultTimeout, TimeUnit.MILLISECONDS) :
                            this.sync().brpop(timeout, key);
        }, key);
        if (null != execute) {
            ArrayList<String> strings = new ArrayList<>();
            strings.add(String.valueOf(execute.getValue()));
            return strings;
        }
        return null;
    }

    @Override
    public Long geoadd(String key, double longitude, double latitude, String member, int seconds) {
        try {
            return (Long) this.execute(() ->
                            this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().geoadd(key, longitude, latitude, member)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().geoadd(key, longitude, latitude, member)
                    , key);
        } finally {
            if (seconds > 0) {
                CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
            }
        }
    }

    @Override
    public Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap, int seconds) {
        if (null != memberCoordinateMap && memberCoordinateMap.size() > 0) {
            try {
                return (Long) this.execute(() -> {
                    Object[] params = new Object[memberCoordinateMap.size() * 3];
                    int i = 0;
                    for (Map.Entry<String, GeoCoordinate> stringGeoCoordinateEntry : memberCoordinateMap.entrySet()) {
                        params[i++] = stringGeoCoordinateEntry.getValue().getLongitude();
                        params[i++] = stringGeoCoordinateEntry.getValue().getLatitude();
                        params[i++] = stringGeoCoordinateEntry.getKey();
                    }
                    return
                            this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().geoadd(key, params)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                                    this.sync().geoadd(key, params);
                }, key);
            } finally {
                if (seconds > 0) {
                    CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
                }
            }
        }
        return 0L;
    }

    @Override
    public Double geodist(String key, String member1, String member2) {
        return (Double) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Double>)this.getAsyncExecutor().geodist(key, member1, member2, GeoArgs.Unit.m)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().geodist(key, member1, member2, GeoArgs.Unit.m);
        }, key);
    }

    @Override
    public Double geodist(String key, String member1, String member2, GeoUnit unit) {
        return (Double) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Double>)this.getAsyncExecutor().geodist(key, member1, member2, this.geoUnitToGeoArgs(unit))).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().geodist(key, member1, member2, this.geoUnitToGeoArgs(unit));
        }, key);
    }

    @Override
    public List<String> geohash(String key, String... members) {
        return (List<String>) this.execute(() -> {
            List<Value<String>> geohash =
                    this.asyncGet() ? ((Future<List<Value<String>>>)this.getAsyncExecutor().geohash(key, members)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().geohash(key, members);
            if (CollectionUtils.isNotEmpty(geohash)) {
                List<String> list = new ArrayList<>();
                for (Value<String> stringValue : geohash) {
                    stringValue.ifHasValue(t -> {
                        list.add(stringValue.getValue());
                    });
                    stringValue.ifEmpty(() -> {
                        list.add(null);
                    });
                }
                return list;
            }
            return null;
        }, key);
    }

    private GeoCoordinate geoCoordinatesToGeoCoordinate(GeoCoordinates geoCoordinates) {
        if (null != geoCoordinates) {
            return new GeoCoordinate(geoCoordinates.getX().doubleValue(), geoCoordinates.getY().doubleValue());
        }
        return null;
    }

    /**
     * 经纬度转换
     *
     * @param list
     * @return
     */
    private List<GeoCoordinate> geoCoordinatesToGeoCoordinate(List<GeoCoordinates> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            List<GeoCoordinate> res = new ArrayList<>();
            for (GeoCoordinates geoCoordinates : list) {
                res.add(this.geoCoordinatesToGeoCoordinate(geoCoordinates));
            }
            return res;
        }
        return null;
    }

    @Override
    public List<GeoCoordinate> geopos(String key, String... members) {
        return (List<GeoCoordinate>) this.execute(() -> {
            return this.geoCoordinatesToGeoCoordinate(
                    this.asyncGet() ? ((Future<List<GeoCoordinates>>)this.getAsyncExecutor().geopos(key, members)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().geopos(key, members)
            );
        }, key);
    }

    private List<GeoRadiusResponse> geoWithinsToGeoRadiusResponses(List<GeoWithin> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            List<GeoRadiusResponse> res = new ArrayList<>();
            for (GeoWithin geoWithin : list) {
                GeoRadiusResponse geoRadiusResponse = new GeoRadiusResponse(geoWithin.getMember().toString().getBytes(Charset.defaultCharset()));
                if (null != geoWithin.getDistance()) {
                    geoRadiusResponse.setDistance(geoWithin.getDistance());
                }
                if (null != geoWithin.getCoordinates()) {
                    geoRadiusResponse.setCoordinate(this.geoCoordinatesToGeoCoordinate(geoWithin.getCoordinates()));
                }
                res.add(geoRadiusResponse);
            }
            return res;
        }
        return null;
    }

    @Override
    public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit, GeoRadiusParam param) {
        return (List<GeoRadiusResponse>) this.execute(() -> {
            return this.geoWithinsToGeoRadiusResponses(
                    this.asyncGet() ? ((Future<List<GeoWithin>>)this.getAsyncExecutor().georadius(key, longitude, latitude, radius, this.geoUnitToGeoArgs(unit), this.geoRadiusParamToGeoArgs(param))).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().georadius(key, longitude, latitude, radius, this.geoUnitToGeoArgs(unit), this.geoRadiusParamToGeoArgs(param))
            );
        }, key);
    }

//    @Override
//    public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit) {
//        return (List<GeoRadiusResponse>) this.execute(() -> {
//            //TODO
//            return this.sync().georadiusbymember(key, member, radius, this.geoUnitToGeoArgs(unit));
//        },Thread.currentThread() .getStackTrace()[1].getMethodName(),key);
//    }

    @Override
    public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit, GeoRadiusParam param) {
        return (List<GeoRadiusResponse>) this.execute(() -> {
            return this.geoWithinsToGeoRadiusResponses(
                    this.asyncGet() ? ((Future<List<GeoWithin>>)this.getAsyncExecutor().georadiusbymember(key, member, radius, this.geoUnitToGeoArgs(unit), this.geoRadiusParamToGeoArgs(param))).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().georadiusbymember(key, member, radius, this.geoUnitToGeoArgs(unit), this.geoRadiusParamToGeoArgs(param))
            );
        }, key);
    }

    private GeoArgs.Unit geoUnitToGeoArgs(GeoUnit unit) {
        switch (unit) {
            case M:
                return GeoArgs.Unit.m;
            case KM:
                return GeoArgs.Unit.km;
            case MI:
                return GeoArgs.Unit.mi;
            case FT:
                return GeoArgs.Unit.ft;
            default:
                return GeoArgs.Unit.m;
        }
    }

    private GeoArgs geoRadiusParamToGeoArgs(GeoRadiusParam param) {
        GeoArgs geoArgs = new GeoArgs();
        if (null != param) {
            if (param.contains(RedisMagicConstants.WITHCOORD)) {
                geoArgs.withCoordinates();
            }
            if (param.contains(RedisMagicConstants.WITHDIST)) {
                geoArgs.withDistance();
            }
            if (param.contains(RedisMagicConstants.ASC)) {
                geoArgs.asc();
            }
            if (param.contains(RedisMagicConstants.DESC)) {
                geoArgs.desc();
            }
            if (param.contains(RedisMagicConstants.COUNT)) {
                geoArgs.withCount(Long.parseLong(param.getParam("count").toString()));
            }
        }
        return geoArgs;
    }

    @Override
    public List<Long> bitfield(String key, String... arguments) {
        CacheExceptionFactory.throwException("请使用 Lettuce->bitfield(String key, BitFieldArgs bitFieldArgs)");
        return null;
    }

    @Override
    public List<Long> bitfield(String key, BitFieldArgs bitFieldArgs) {
        return (List<Long>) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<List<Long>>)this.getAsyncExecutor().bitfield(key, bitFieldArgs)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().bitfield(key, bitFieldArgs);
        }, key);
    }

    @Override
    public Long hstrlen(String key, String field) {
        return (Long) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Long>)this.getAsyncExecutor().hstrlen(key, field)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().hstrlen(key, field);
        }, key);
    }

    @Override
    public Object eval(String script, int keyCount, String[] params) {
        return this.eval(script, ScriptOutputType.VALUE, keyCount, params);
    }

    @Override
    public Object eval(String script, ScriptOutputType outputType, int keyCount, String[] params) {
        String[] keys = new String[keyCount];
        String[] values = new String[params.length - keyCount];
        for (int i = 0; i < params.length; i++) {
            if (i < keyCount) {
                keys[i] = params[i];
            } else {
                values[i - keyCount] = params[i];
            }
        }
        return this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Object>)this.getAsyncExecutor().eval(script, outputType, keys, values)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().eval(script, outputType, keys, values);
        }, "");
    }

    @Override
    public Object eval(String script, List<String> keys, List<String> args) {
        return this.eval(script, ScriptOutputType.VALUE, keys, args);
    }

    @Override
    public Object eval(String script, ScriptOutputType outputType, List<String> keys, List<String> args) {
        return this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Object>)this.getAsyncExecutor().eval(script, outputType, keys.toArray(new String[keys.size()]), args.toArray())).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().eval(script, outputType, keys.toArray(new String[keys.size()]), args.toArray());
        }, "");
    }

    @Override
    public Object eval(String script) {
        return this.eval(script, ScriptOutputType.VALUE);
    }

    @Override
    public Object eval(String script, ScriptOutputType outputType) {
        return this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Object>)this.getAsyncExecutor().eval(script, outputType)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().eval(script, outputType);
        }, "");
    }

    @Override
    public Object evalsha(String sha1) {
        return this.evalsha(sha1, ScriptOutputType.INTEGER);
    }

    @Override
    public Object evalsha(String sha1, ScriptOutputType outputType) {
        return this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Object>)this.getAsyncExecutor().evalsha(sha1, outputType)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().evalsha(sha1, outputType);
        }, "");
    }

    @Override
    public Object evalsha(String sha1, List<String> keys, List<String> args) {
        return this.evalsha(sha1, ScriptOutputType.INTEGER, keys, args);
    }

    @Override
    public Object evalsha(String sha1, ScriptOutputType outputType, List<String> keys, List<String> args) {
        return this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Object>)this.getAsyncExecutor().evalsha(sha1, outputType, keys.toArray(new String[keys.size()]), args.toArray())).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().evalsha(sha1, outputType, keys.toArray(new String[keys.size()]), args.toArray());
        }, "");
    }

    @Override
    public Object evalsha(String sha1, int keyCount, String[] params) {
        return this.evalsha(sha1, ScriptOutputType.INTEGER, keyCount, params);
    }

    @Override
    public Object evalsha(String sha1, ScriptOutputType outputType, int keyCount, String[] params) {
        String[] keys = new String[keyCount];
        String[] values = new String[params.length - keyCount];
        for (int i = 0; i < params.length; i++) {
            if (i < keyCount) {
                keys[i] = params[i];
            } else {
                values[i - keyCount] = params[i];
            }
        }
        return this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<Object>)this.getAsyncExecutor().evalsha(sha1, outputType, keys, values)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().evalsha(sha1, outputType, keys, values);
        }, "");
    }

    @Override
    public Boolean scriptExists(String sha1) {
        return (Boolean) this.execute(() -> {
            List list =
                    this.asyncGet() ? ((Future<List>)this.getAsyncExecutor().scriptExists(sha1)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().scriptExists(sha1);
            if (CollectionUtils.isNotEmpty(list)) {
                return list.get(0);
            }
            return false;
        }, "");
    }

    @Override
    public List<Boolean> scriptExists(String... sha1) {
        return (List<Boolean>) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<List>)this.getAsyncExecutor().scriptExists(sha1)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().scriptExists(sha1);
        }, "");
    }

    @Override
    public String scriptLoad(String script) {
        return (String) this.execute(() -> {
            return
                    this.asyncGet() ? ((Future<String>)this.getAsyncExecutor().scriptLoad(script)).get(getTimeout(), TimeUnit.MILLISECONDS) :
                            this.sync().scriptLoad(script);
        }, "");
    }

    @Override
    public Map<String, Long> delBatch(List<String> keys) {
        Map<String, Long> res = new HashMap<>(keys.size());
        keys.stream().forEach(k -> {
            try {
                res.put(k, this.del(k));
            } catch (Exception e) {
                CacheExceptionFactory.addErrorLog("del batch error ! " + k, e);
                res.put(k, 0L);
            }
        });
        return res;
    }

    /**
     * 获取异步执行器
     *
     * @return
     */
    private RedisClusterAsyncCommands getAsyncExecutor() {
        return this.async(this.getConnectResource());
    }

    /**
     * 获取超时时间
     *
     * @return
     */
    private int getTimeout() {
        final BaseCacheConfig redisSourceConfig = this.getRedisSourceConfig();
        if (redisSourceConfig instanceof LettuceConnectSourceConfig) {
            return ((LettuceConnectSourceConfig) redisSourceConfig).getSoTimeout();
        }
        if (redisSourceConfig instanceof LettuceClusterConnectSourceConfig) {
            return ((LettuceClusterConnectSourceConfig) redisSourceConfig).getSoTimeout();
        }
        return 2000;
    }

    /**
     * 是否异步执行
     *
     * @return
     */
    private boolean asyncGet() {
        return this.getCacheConfigModel().isLettuceAsyncGet();
    }
}
