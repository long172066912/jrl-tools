package org.jrl.tools.cache.config;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 过期配置
 *
 * @author JerryLong
 */
public class DefaultJrlCacheExpireConfig implements JrlCacheExpireConfig {
    private final long expireTime;
    private final TimeUnit unit;
    private ExpireRandom expireRandom;

    public DefaultJrlCacheExpireConfig(long expireTime, TimeUnit unit) {
        this.expireTime = expireTime;
        this.unit = unit;
    }

    public DefaultJrlCacheExpireConfig(long expireTime, TimeUnit unit, ExpireRandom expireRandom) {
        this.expireTime = expireTime;
        this.unit = unit;
        this.expireRandom = expireRandom;
    }

    @Override
    public long expire() {
        if (null != this.expireRandom() && this.expireRandom().getMin() > 0 && this.expireRandom().getMax() > this.expireRandom().getMin()) {
            return (this.expireTime + ThreadLocalRandom.current().nextLong(this.expireRandom().getMin(), this.expireRandom().getMax()));
        }
        return this.expireTime;
    }

    @Override
    public TimeUnit unit() {
        return unit;
    }

    @Override
    public ExpireRandom expireRandom() {
        return expireRandom;
    }

    /**
     * 1分钟
     *
     * @return JrlCacheExpireConfig
     */
    public static JrlCacheExpireConfig oneMinute() {
        return new DefaultJrlCacheExpireConfig(1, TimeUnit.MINUTES);
    }

    /**
     * 1小时
     *
     * @return JrlCacheExpireConfig
     */
    public static JrlCacheExpireConfig oneHoure() {
        return new DefaultJrlCacheExpireConfig(1, TimeUnit.HOURS);
    }

    /**
     * 1天
     *
     * @return JrlCacheExpireConfig
     */
    public static JrlCacheExpireConfig oneDay() {
        return new DefaultJrlCacheExpireConfig(1, TimeUnit.DAYS);
    }

    public static JrlCacheExpireConfig oneDay(ExpireRandom random) {
        return new DefaultJrlCacheExpireConfig(1, TimeUnit.DAYS, random);
    }
}
