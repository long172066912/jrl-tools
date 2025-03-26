package org.jrl.redis.core.handle;

import io.lettuce.core.ScriptOutputType;
import org.apache.commons.lang3.StringUtils;
import org.jrl.monitor.client.JrlMonitor;
import org.jrl.redis.core.BaseCacheExecutor;
import org.jrl.redis.core.cache.localcache.CaffeineLocalCache;
import org.jrl.redis.core.cache.redis.lua.RedisLuaInterface;
import org.jrl.redis.core.cache.redis.lua.RedisLuaScripts;
import org.jrl.redis.core.constant.HandlePostProcessorTypeEnum;
import org.jrl.redis.core.converters.PostProcessorConvertersAndExecutor;
import org.jrl.redis.core.model.CacheDataBuilder;
import org.jrl.redis.core.model.CacheHandleProcessorModel;
import org.jrl.redis.core.model.CacheLockResponse;
import org.jrl.redis.core.processor.AbstractHandlePostProcessor;
import org.jrl.redis.exception.CacheException;
import org.jrl.redis.exception.CacheExceptionFactory;
import org.jrl.redis.exception.CacheKeyNotExistsException;
import org.jrl.redis.util.CacheFunction;
import org.jrl.tools.json.JrlJsonNoExpUtil;
import org.redisson.api.RLock;
import org.redisson.api.RMapCache;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: AbstractCommomHandle
 * @Description: 对公告执行接口的抽象
 * @date 2021/1/18 3:20 PM
 */
public abstract class AbstractCacheHandle extends BaseCacheExecutor implements InterfaceCommomHandle {

    public static final String ATTEMPT_TO_UNLOCK_LOCK_NOT_LOCKED_BY_CURRENT_THREAD = "attempt to unlock lock, not locked by current thread";
    protected PostProcessorConvertersAndExecutor postProcessorConverters = new PostProcessorConvertersAndExecutor();

    protected ExecutorService threadPoolExecutor = JrlMonitor.getMonitorExecutorService("Cache2-Handle-CallerRunsPolicy-Pool", new ThreadPoolExecutor(5, 20, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10000), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "Cache2-Handle-CallerRunsPolicy-Pool");
        }
    }, new ThreadPoolExecutor.CallerRunsPolicy()));

    /**
     * lua缓存信息
     */
    private Map<RedisLuaInterface, String> luaLoadsInfo = new ConcurrentHashMap<>();

    /**
     * 锁前缀
     */
    private static final String LOCK_PRE = "cache2:lock:";

    /**
     * 获取客户端类型
     * RedisClientConstants
     *
     * @return
     */
    public abstract int getClientType();

    public ExecutorService getExpireThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    /**
     * 根据操作类型获取执行链路
     *
     * @return
     */
    public List<AbstractHandlePostProcessor> getHandleLinkList() {
        return postProcessorConverters.getHandlePostProcessors(HandlePostProcessorTypeEnum.HANDLE, this.getClientType());
    }

    @Override
    public Object execute(CacheFunction function) {
        try {
            final CacheHandleProcessorModel cacheHandleProcessorModel = new CacheHandleProcessorModel(function, function.fnToFnName(), getCacheConfigModel(), "");
            cacheHandleProcessorModel.setHitKey(this.getHitKeyThreadLocal());
            return postProcessorConverters.executeHandles(this.getHandleLinkList(), cacheHandleProcessorModel);
        } finally {
            //释放资源
            this.returnConnectResource();
            this.cleanHitKey();
        }
    }

    /**
     * 执行命令
     *
     * @return
     */
    @Override
    public Object execute(CacheFunction function, String... keys) {
        try {
            final CacheHandleProcessorModel cacheHandleProcessorModel = new CacheHandleProcessorModel(function, function.fnToFnName(), getCacheConfigModel(), keys);
            cacheHandleProcessorModel.setHitKey(this.getHitKeyThreadLocal());
            return postProcessorConverters.executeHandles(this.getHandleLinkList(), cacheHandleProcessorModel);
        } finally {
            //释放资源
            this.returnConnectResource();
            this.cleanHitKey();
        }
    }

    @Override
    public <T> CacheLockResponse<T> lock(String name, long leaseTime, TimeUnit unit, Supplier<T> supplier) throws Throwable {
        if (StringUtils.isBlank(name)) {
            CacheExceptionFactory.addErrorLog("lock name is blank !");
        }
        CacheLockResponse<T> response = null;
        final RLock lock = this.lock(name, leaseTime, unit);
        try {
            if (null != lock) {
                try {
                    response = new CacheLockResponse<>(true, supplier.get());
                } catch (Throwable e) {
                    response = new CacheLockResponse<>(true, null, e);
                }
            } else {
                response = new CacheLockResponse<>(false, null);
            }
        } finally {
            unLock(lock);
        }
        if (null != response.getBusinessException()) {
            throw response.getBusinessException();
        }
        return response;
    }

    @Override
    public <T> CacheLockResponse<T> tryLock(String name, long waitTime, long leaseTime, TimeUnit unit, Supplier<T> supplier) throws Throwable {
        if (StringUtils.isBlank(name)) {
            CacheExceptionFactory.addErrorLog("tryLock name is blank !");
        }
        CacheLockResponse<T> response = null;
        final RLock lock = this.tryLock(name, waitTime, leaseTime, unit);
        try {
            if (null != lock) {
                try {
                    response = new CacheLockResponse<>(true, supplier.get());
                } catch (Throwable e) {
                    response = new CacheLockResponse<>(true, null, e);
                }
            } else {
                response = new CacheLockResponse<>(false, null);
            }
        } finally {
            unLock(lock);
        }
        if (null != response.getBusinessException()) {
            throw response.getBusinessException();
        }
        return response;
    }

    @Override
    public RLock lock(String name, long leaseTime, TimeUnit unit) {
        if (StringUtils.isBlank(name)) {
            CacheExceptionFactory.addErrorLog("lock name is blank !");
        }
        final String lockName = key(LOCK_PRE + name);
        return (RLock) this.execute(() -> {
            try {
                RLock lock = this.getRedissonClient().getLock(lockName);
                lock.lock(leaseTime, unit);
                return lock;
            } catch (Exception e) {
                CacheExceptionFactory.throwException("JrlRedis lock error !", e);
                return null;
            }
        }, lockName);
    }

    @Override
    public RLock tryLock(String name, long waitTime, long leaseTime, TimeUnit unit) {
        if (StringUtils.isBlank(name)) {
            CacheExceptionFactory.addErrorLog("tryLock name is blank !");
        }
        final String lockName = key(LOCK_PRE + name);
        return (RLock) this.execute(() -> {
            try {
                RLock lock = this.getRedissonClient().getLock(lockName);
                boolean b = lock.tryLock(waitTime, leaseTime, unit);
                if (b) {
                    return lock;
                }
                return null;
            } catch (InterruptedException e) {
                CacheExceptionFactory.throwException("JrlRedis tryLock error !", e);
                return null;
            }
        }, lockName);
    }

    @Override
    public RLock lockBySpin(String name, long leaseTime, TimeUnit unit) {
        if (StringUtils.isBlank(name)) {
            CacheExceptionFactory.addErrorLog("lock name is blank !");
        }
        String lockName = key(LOCK_PRE + name);
        return (RLock) this.execute(() -> {
            try {
                RLock lock = this.getRedissonClient().getSpinLock(lockName);
                lock.lock(leaseTime, unit);
                return lock;
            } catch (Exception e) {
                CacheExceptionFactory.throwException("JrlRedis lockBySpin error !", e);
                return null;
            }
        }, lockName);
    }

    @Override
    public RLock tryLockBySpin(String name, long waitTime, long leaseTime, TimeUnit unit) {
        if (StringUtils.isBlank(name)) {
            CacheExceptionFactory.addErrorLog("tryLock name is blank !");
        }
        String lockName = key(LOCK_PRE + name);
        return (RLock) this.execute(() -> {
            try {
                RLock lock = this.getRedissonClient().getSpinLock(lockName);
                boolean b = lock.tryLock(waitTime, leaseTime, unit);
                if (b) {
                    return lock;
                }
                return null;
            } catch (InterruptedException e) {
                CacheExceptionFactory.throwException("JrlRedis tryLockBySpin error !", e);
                return null;
            }
        }, lockName);
    }

    @Override
    public RLock readTryLock(String name, long waitTime, long leaseTime, TimeUnit unit) {
        if (StringUtils.isBlank(name)) {
            CacheExceptionFactory.addErrorLog("readLock name is blank !");
        }
        String lockName = key(LOCK_PRE + name);
        return (RLock) this.execute(() -> {
            try {
                final RLock lock = this.getRedissonClient().getReadWriteLock(lockName).readLock();
                final boolean b = lock.tryLock(waitTime, leaseTime, unit);
                if (b) {
                    return lock;
                }
            } catch (Exception e) {
                CacheExceptionFactory.throwException("JrlRedis readTryLock error !", e);
            }
            return null;
        }, lockName);
    }

    @Override
    public RLock writeTryLock(String name, long waitTime, long leaseTime, TimeUnit unit) {
        if (StringUtils.isBlank(name)) {
            CacheExceptionFactory.addErrorLog("writeLock name is blank !");
        }
        String lockName = key(LOCK_PRE + name);
        return (RLock) this.execute(() -> {
            try {
                final RLock lock = this.getRedissonClient().getReadWriteLock(lockName).writeLock();
                final boolean b = lock.tryLock(waitTime, leaseTime, unit);
                if (b) {
                    return lock;
                }
            } catch (Exception e) {
                CacheExceptionFactory.throwException("JrlRedis writeTryLock error !", e);
            }
            return null;
        }, lockName);
    }

    @Override
    public void unLock(RLock lock) {
        if (null != lock) {
            this.execute(() -> {
                try {
                    lock.unlock();
                } catch (Exception e) {
                    //如果不是当前锁，则不处理
                    if (e instanceof IllegalMonitorStateException && e.getMessage().contains(ATTEMPT_TO_UNLOCK_LOCK_NOT_LOCKED_BY_CURRENT_THREAD)) {
                        CacheExceptionFactory.addWarnLog("cache unLock fail ! attempt to unlock lock, not locked by current thread ! lockName : {}", lock.getName());
                        return null;
                    }
                    //防止判断时异常导致锁一直不释放
                    CacheExceptionFactory.addWarnLog("cache unLock fail ! lockName : {}", e, lock.getName());
                    lock.unlock();
                    CacheExceptionFactory.throwException("JedisRedisCommandsImpl->unLock error !", e);
                }
                return null;
            }, lock.getName());
        }
    }

    @Override
    public Object localGetAndSet(String key) {
        return CaffeineLocalCache.get(key, e -> this.get(key));
    }

    @Override
    public Object localGetAndSet(String key, Function function) {
        return CaffeineLocalCache.get(key, e -> function.apply(this));
    }

    @Override
    public <R extends Object> R getCacheData(CacheDataBuilder<R> cacheDataBuilder) {
        if (null == cacheDataBuilder || StringUtils.isBlank(cacheDataBuilder.getLockKey()) || null == cacheDataBuilder.getCacheGetFunction() || null == cacheDataBuilder.getDbGetFunction() || null == cacheDataBuilder.getCacheSetFunction()) {
            CacheExceptionFactory.throwException("cache2 getCacheData params error !");
            return null;
        }
        cacheDataBuilder = this.checkCacheDataBuilder(cacheDataBuilder);
        R data = null;
        try {
            //初次从缓存获取
            data = cacheDataBuilder.getCacheGetFunction().apply(this);
            if (null != data) {
                return data;
            }
            RLock lock = this.tryLock(cacheDataBuilder.getLockKey(), cacheDataBuilder.getWaitTime(), cacheDataBuilder.getLeaseTime(), cacheDataBuilder.getUnit());
            if (null != lock) {
                try {
                    //双检测
                    data = cacheDataBuilder.getCacheGetFunction().apply(this);
                    if (null != data) {
                        return data;
                    }
                    //从数据库获取，并保存数据
                    data = cacheDataBuilder.getDbGetFunction().apply(this);
                    if (null != data) {
                        //同步放入缓存
                        cacheDataBuilder.getCacheSetFunction().apply(data);
                    }
                    return data;
                } finally {
                    this.unLock(lock);
                }
            } else {
                //锁等待失败，最后尝试再从缓存拿一次
                return cacheDataBuilder.getCacheGetFunction().apply(this);
            }
        } catch (CacheException e) {
            CacheExceptionFactory.addErrorLog("AbstractCacheHandle", "getCacheData", "test:[{}]", e, JrlJsonNoExpUtil.toJson(cacheDataBuilder));
            //redis异常从数据库拿
            if (null != data) {
                return data;
            }
            if (cacheDataBuilder.getCacheExceptionByDb()) {
                return cacheDataBuilder.getDbGetFunction().apply(this);
            } else {
                throw e;
            }
        }
    }

    private CacheDataBuilder checkCacheDataBuilder(CacheDataBuilder cacheDataBuilder) {
        if (null == cacheDataBuilder.getWaitTime()) {
            cacheDataBuilder.setWaitTime(10L);
        }
        if (null == cacheDataBuilder.getLeaseTime()) {
            cacheDataBuilder.setLeaseTime(30L);
        }
        if (null == cacheDataBuilder.getUnit()) {
            cacheDataBuilder.setUnit(TimeUnit.SECONDS);
        }
        if (null == cacheDataBuilder.getCacheExceptionByDb()) {
            cacheDataBuilder.setCacheExceptionByDb(true);
        }
        return cacheDataBuilder;
    }

    @Override
    public String getLuaSha1(RedisLuaInterface redisLuaScripts) {
        return this.luaLoadsInfo.get(redisLuaScripts);
    }

    @Override
    public Object executeByLua(RedisLuaInterface lua, ScriptOutputType outputType, List<String> keys, List<String> args) {
        String luaSha1 = getLuaSha1(lua);
        if (StringUtils.isBlank(luaSha1)) {
            luaSha1 = this.luaLoadsInfo.computeIfAbsent(lua, t -> this.scriptLoad(lua.getScripts()));
        }
        return this.evalsha(luaSha1, outputType, Optional.ofNullable(keys).orElse(new ArrayList<>()), Optional.ofNullable(args).orElse(new ArrayList<>()));
    }

    protected Map<RedisLuaInterface, String> getLuaLoadsInfo() {
        return luaLoadsInfo;
    }

    private static final String RMAP_PREFIX = "rmap:";

    @Override
    public Object getByRedissonMap(Object key) {
        return this.execute(() -> getRmap().get(key));
    }

    @Override
    public void putByRedissonMap(Object key, Object value) {
        this.execute(() -> getRmap().fastPut(key, value, 1, TimeUnit.DAYS));
    }

    @Override
    public Object putIfAbsentByRedissonMap(Object key, Object value) {
        return this.execute(() -> getRmap().putIfAbsent(key, value, 1, TimeUnit.DAYS));
    }

    @Override
    public void removeByRedissonMap(Object key) {
        this.execute(() -> getRmap().remove(key));
    }

    private RMapCache getRmap() {
        return this.getRedissonClient().getMapCache(RMAP_PREFIX + this.getCacheConfigModel().getCacheType());
    }

    @Override
    public List<String> mgetToList(String... keys) {
        final Map<String, Object> mget = this.mget(keys);
        final List<String> list = new ArrayList<>();
        for (String key : keys) {
            list.add(Optional.ofNullable(mget.get(key)).map(Object::toString).orElse(null));
        }
        return list;
    }

    @Override
    public long delBatch(String... keys) {
        final Map<String, Long> map = this.delBatch(Arrays.asList(keys));
        if (null == map) {
            return 0;
        }
        return map.values().stream().filter(v -> v > 0).count();
    }

    @Override
    public Long zaddIfKeyExists(String key, double score, String member, int seconds) {
        try {
            return this.executeByLuaAndCheckKeyExistsLong(() -> this.executeByLua(RedisLuaScripts.ZADD_IF_EXISTS, ScriptOutputType.VALUE, Collections.singletonList(key), Arrays.asList(score + "", member)), seconds, key);
        } catch (CacheKeyNotExistsException e) {
            return 0L;
        }
    }

    @Override
    public Long zaddIfKeyMustExists(String key, double score, String member, int seconds) throws CacheKeyNotExistsException {
        return this.executeByLuaAndCheckKeyExistsLong(() -> this.executeByLua(RedisLuaScripts.ZADD_IF_EXISTS, ScriptOutputType.VALUE, Collections.singletonList(key), Arrays.asList(score + "", member)), seconds, key);
    }

    @Override
    public String hgetIfKeyExists(String key, String field) throws CacheKeyNotExistsException {
        final Object resp = executeByLuaAndCheckKeyExists(() -> this.executeByLua(RedisLuaScripts.HGET_IF_KEY_EXISTS, ScriptOutputType.VALUE, Collections.singletonList(key), Collections.singletonList(field)), -1, key);
        if (null == resp) {
            return null;
        }
        return resp.toString();
    }

    @Override
    public Long hsetIfKeyExists(String key, String field, String value, int seconds) throws CacheKeyNotExistsException {
        return this.executeByLuaAndCheckKeyExistsLong(() -> this.executeByLua(RedisLuaScripts.HSET_IF_EXISTS, ScriptOutputType.VALUE, Collections.singletonList(key), Arrays.asList(field, value)), seconds, key);
    }

    @Override
    public Map<String, Double> zscoreBatch(String key, List<String> members) {
        return JrlJsonNoExpUtil.fromJson(this.execute(() -> this.executeByLua(RedisLuaScripts.ZSCORE_BATCH, ScriptOutputType.VALUE, Collections.singletonList(key), members), key).toString(), Map.class);
    }

    @Override
    public Long saddIfKeyExist(String key, int seconds, String... member) throws CacheKeyNotExistsException {
        return this.executeByLuaAndCheckKeyExistsLong(() -> this.executeByLua(RedisLuaScripts.SADD_IF_EXISTS, ScriptOutputType.VALUE, Collections.singletonList(key), Arrays.asList(member)), seconds, key);
    }

    @Override
    public Long lpushIfExists(String key, String member, int seconds) throws CacheKeyNotExistsException {
        return this.executeByLuaAndCheckKeyExistsLong(() -> this.executeByLua(RedisLuaScripts.LPUSH_IF_EXISTS, ScriptOutputType.VALUE, Collections.singletonList(key), Collections.singletonList(member)), seconds, key);
    }

    @Override
    public Long rpushIfExists(String key, String member, int seconds) throws CacheKeyNotExistsException {
        return this.executeByLuaAndCheckKeyExistsLong(() -> this.executeByLua(RedisLuaScripts.RPUSH_IF_EXISTS, ScriptOutputType.VALUE, Collections.singletonList(key), Collections.singletonList(member)), seconds, key);
    }

    private Long executeByLuaAndCheckKeyExistsLong(Callable<Object> lua, int seconds, String key) {
        final Object resp = this.executeByLuaAndCheckKeyExists(lua, seconds, key);
        return null != resp ? Long.parseLong(resp.toString()) : null;
    }

    private Object executeByLuaAndCheckKeyExists(Callable<Object> lua, int seconds, String key) {
        final Object resp = this.execute(lua::call, key);
        if (KEY_NOT_EXISTS.equals(resp)) {
            throw new CacheKeyNotExistsException("JrlRedis execute by lua error ! key : " + key + " not exists !");
        }
        if (seconds > 0) {
            CompletableFuture.runAsync(() -> this.expire(key, seconds), threadPoolExecutor);
        }
        return resp;
    }

    // 默认泳道标识
    public static final String LANE_CODE = "";

    protected String key(String key) {
        return StringUtils.isNotBlank(LANE_CODE) ? LANE_CODE + "_" + key : key;
    }
}
