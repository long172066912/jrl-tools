package org.jrl.tools.cache.config;

import java.util.concurrent.TimeUnit;

/**
 * 缓存过期接口
 *
 * @author JerryLong
 */
public interface JrlCacheExpireConfig {

    /**
     * 获取过期时间
     *
     * @return long
     */
    long expire();

    /**
     * 时间单位
     *
     * @return TimeUnit
     */
    TimeUnit unit();

    /**
     * 获取随机过期时间
     *
     * @return ExpireRandom
     */
    ExpireRandom expireRandom();

    public static class ExpireRandom {
        private Long max;
        private Long min;

        public ExpireRandom(Long max, Long min) {
            this.max = max;
            this.min = min;
        }

        public Long getMax() {
            return max;
        }

        public void setMax(Long max) {
            this.max = max;
        }

        public Long getMin() {
            return min;
        }

        public void setMin(Long min) {
            this.min = min;
        }
    }
}
