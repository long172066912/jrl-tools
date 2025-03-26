package org.jrl.redis.config;

import org.jrl.redis.config.model.CacheShardConfig;
import org.jrl.redis.core.constant.ConnectTypeEnum;
import org.jrl.redis.core.constant.RedisClientConstants;
import org.jrl.redis.core.constant.UseTypeEnum;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: CacheBasicConfig
 * @Description: 缓存组件系统配置
 * @date 2021/1/22 11:50 AM
 */
public class CacheBasicConfig {

    /**
     * 客户端类型，默认Lettuce
     */
    public static int clientType = RedisClientConstants.LETTUCE;
    /**
     * 默认非连接池方式，节省内存与CPU
     */
    public static ConnectTypeEnum connectTypeEnum = ConnectTypeEnum.SIMPLE;
    /**
     * 是否本地缓存，默认不开启，暂不支持，请使用CaffeineLocalCache
     */
    @Deprecated
    public static boolean isLocalCache = false;
    /**
     * 散列配置，暂不支持
     * 需要设置连接方式为 ConnectTypeEnum.SHARDED
     */
    @Deprecated
    public static CacheShardConfig cacheShardConfig = new CacheShardConfig();
    /**
     * 是否开启MQ监控，暂不支持
     */
    @Deprecated
    public static boolean isOpenMq = false;
    /**
     * 用途
     */
    public static UseTypeEnum useType = UseTypeEnum.BUSINESS;
    /**
     * 心跳检测间隔时间（秒）
     */
    public static int HEART_CHECK_lINTERVAL_SECONDS = 30;
    /**
     * DB配置检测间隔时间（秒）
     */
    public static int DB_CONFIG_CHECK_lINTERVAL_SECONDS = 60;
    /**
     * 热key统计同步频次，5秒同步一次，单位（毫秒）
     */
    @Deprecated
    public static long HOT_KEY_COUNT_lINTERVAL_MILLISECONDS = 5000L;
    /**
     * 热key统计容量大小
     */
    public static final int HOTE_KEY_STATISTIC_CAPACITY = 50;
    /**
     * 热key统计周期内最小值
     */
    public static final int HOT_KEY_COUNT_LEAST_VALUE = 1000;
    /**
     * Redisson线程数配置
     */
    public static final int REDISSON_THREAD_NUM = 5;
    /**
     * 监控队列长度
     */
    public static final int MONITOR_QUEUE_SIZE = 100000;
}
