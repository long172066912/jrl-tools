package org.jrl.redis.core;

/**
* 热key
* @author JerryLong
*/
@FunctionalInterface
public interface CacheHitKeyConvertor {
    /**
     * 传入所有的key
     * @param key
     * @return 业务自定义转换规则，返回业务自定义转换后的key，例如：app:get:123 => app:get:*
     */
    String convert(String key);

    class DefaultCacheHitKeyConvertor implements CacheHitKeyConvertor {

        public static final DefaultCacheHitKeyConvertor INSTANCE = new DefaultCacheHitKeyConvertor();
        @Override
        public String convert(String key) {
            return key.replaceAll("\\d+", "*");
        }
    }
}
