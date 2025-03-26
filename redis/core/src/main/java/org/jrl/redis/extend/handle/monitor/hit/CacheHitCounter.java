package org.jrl.redis.extend.handle.monitor.hit;

import org.jrl.redis.exception.CacheExceptionFactory;
import org.jrl.redis.extend.handle.monitor.statistics.SkipMapStatistics;
import org.jrl.redis.util.async.AsyncExecutorUtils;
import org.apache.commons.lang3.StringUtils;
import org.jrl.tools.json.JrlJsonNoExpUtil;
import org.jrl.tools.log.JrlLoggerFactory;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 缓存命中率统计
 *
 * @author JerryLong
 */
public class CacheHitCounter {

    private static Logger LOGGER = JrlLoggerFactory.getLogger(CacheHitCounter.class);

    private static final String HIT_KEY = "hitKey";
    private static final String CACHE_TYPE = "cacheType";
    private static final String HIT = "hit";

    private static Map<String, CacheHitKey> hitKeys = new ConcurrentHashMap<>();

    private static final int MAX_KEY_NUM = 100;

    /**
     * 缓存命中率计算
     *
     * @param key   key
     * @param isHit 是否命中
     */
    public static void count(String cacheType, String key, boolean isHit) {
        if (StringUtils.isBlank(key)) {
            return;
        }
        // 如果key数量超过100个，并且key不在hitKeys中，不进行统计
        final CacheHitKey cacheHitKey = hitKeys.computeIfAbsent(cacheType, e -> new CacheHitKey());
        if (!cacheHitKey.containsKey(key)) {
            return;
        }
        final Map<String, String> tags = new HashMap<>(4);
        tags.put(CACHE_TYPE, cacheType);
        tags.put(HIT_KEY, key);
//        JrlMonitor.count("cacheHitPercent", tags, new CacheHitCountMultiValue(isHit));
    }

//    private static class CacheHitCountMultiValue extends AbstractCountMultiValue {
//
//        private final boolean isHit;
//
//        public CacheHitCountMultiValue(boolean isHit) {
//            this.isHit = isHit;
//        }
//
//        @Override
//        public int amount() {
//            return 1;
//        }
//
//        @Override
//        public Map<String, Integer> multiValue() {
//            return ImmutableMap.of(HIT, isHit ? 1 : 0);
//        }
//    }

    private static class CacheHitKey {
        /**
         * 次数统计排序，取100个，大于0的key
         */
        private final SkipMapStatistics skipMapStatistics = new SkipMapStatistics("hit", MAX_KEY_NUM, 0);

        public CacheHitKey() {
            /**
             * 定时60秒调用一次
             */
            AsyncExecutorUtils.submitScheduledTask(() -> {
                try {
                    //清理内存key数据，防止内存泄漏
                    skipMapStatistics.clean();
                    LOGGER.debug("CacheHitKey clean success !");
                } catch (Exception e) {
                    CacheExceptionFactory.addErrorLog("CacheHitKey synchronization error !", e);
                }
            }, 60, 60, TimeUnit.SECONDS);
        }

        /**
         * 对hit进行本地缓存
         */
        private Set<String> keys = new HashSet<>();
        /**
         * 上次本地缓存的时间
         */
        private long lastTime = 0;
        /**
         * 1分钟更新1次
         */
        public static final long SECOND_60 = 10_000L;

        public boolean containsKey(String key) {
            //加入key，计数+1
            skipMapStatistics.incr(HIT, key);
            //如果本地存储的key列表已经1分钟没更新，则重新获取调用排名前100的
            if (lastTime == 0 || lastTime < (System.currentTimeMillis() - SECOND_60)) {
                keys = skipMapStatistics.getKeys();
                LOGGER.debug("CacheHitCounter hit 100 keys : {}", JrlJsonNoExpUtil.toJson(keys));
                lastTime = System.currentTimeMillis();
            }
            return keys.contains(key);
        }
    }
}
