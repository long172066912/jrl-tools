package org.jrl.redis.util;

import org.jrl.redis.extend.config.model.RedisDbConfigModel;
import org.jrl.redis.extend.config.model.RedisDbSourceConfigModel;
import org.jrl.redis.core.constant.CacheConfigSourceTypeEnum;
import org.jrl.redis.core.constant.ConnectTypeEnum;
import org.jrl.redis.core.constant.UseTypeEnum;
import org.jrl.redis.core.model.CacheConfigModel;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: CacheConfigUtils
 * @Description: 配置操作帮助类
 * @date 2021/1/29 5:15 PM
 */
public class CacheConfigUtils {

    /**
     * 间隔符
     */
    public final static String INTERVAL = "-&";

    /**
     * model转hashKey
     * 配置与连接缓存用不到isLocalCache
     *
     * @param cacheConfigModel
     * @return
     */
    public static String modelToHashKey(CacheConfigModel cacheConfigModel) {
        return cacheConfigModel.getCacheType() + INTERVAL + cacheConfigModel.getClientType() + INTERVAL + cacheConfigModel.getConnectTypeEnum() + INTERVAL + cacheConfigModel.getUseType();
    }

    /**
     * model转hashKey
     * 配置与连接缓存用不到isLocalCache
     *
     * @param cacheConfigModel
     * @return
     */
    public static String modelToHashKeyNoUseType(CacheConfigModel cacheConfigModel) {
        return cacheConfigModel.getCacheType() + INTERVAL + cacheConfigModel.getClientType() + INTERVAL + cacheConfigModel.getConnectTypeEnum();
    }

    /**
     * hashKey转回对象，不能用于实际操作，只能对比
     *
     * @param hashKey
     * @return
     */
    public static CacheConfigModel hashKeyToModel(String hashKey) {
        String[] split = hashKey.split(INTERVAL);
        return new CacheConfigModel()
                .setCacheType(split[0])
                .setClientType(Integer.valueOf(split[1]))
                .setConnectTypeEnum(split.length > 2 ? ConnectTypeEnum.valueOf(split[2]) : null)
                .setUseType(split.length > 3 ? UseTypeEnum.valueOf(split[3]) : null)
                .setLocalCache(false);
    }

    /**
     * 检测2个key是否相等，cacheType+clientType+connectType
     *
     * @param hashKey1
     * @param hashKey2
     * @return
     */
    public static boolean checkIsEquals(String hashKey1, String hashKey2) {
        CacheConfigModel cacheConfigModel1 = hashKeyToModel(hashKey1);
        CacheConfigModel cacheConfigModel2 = hashKeyToModel(hashKey2);
        return cacheConfigModel1.getCacheType().equals(cacheConfigModel2.getCacheType())
                && cacheConfigModel1.getClientType() == cacheConfigModel2.getClientType()
                && cacheConfigModel1.getConnectTypeEnum().equals(cacheConfigModel2.getConnectTypeEnum())
                && cacheConfigModel1.getConfigSourceType().equals(cacheConfigModel2.getConfigSourceType());
    }

    /**
     * 检测DB配置是否变更
     *
     * @param oldConfig
     * @param newConfig
     * @return
     */
    public static boolean checkDbConfigIsChange(RedisDbConfigModel oldConfig, RedisDbConfigModel newConfig) {
        if (oldConfig.getConfigList().size() != newConfig.getConfigList().size()) {
            return true;
        }
        RedisDbSourceConfigModel redisDbSourceConfigModel1 = null;
        RedisDbSourceConfigModel redisDbSourceConfigModel2 = null;
        for (int i = 0; i < oldConfig.getConfigList().size(); i++) {
            redisDbSourceConfigModel1 = oldConfig.getConfigList().get(i);
            redisDbSourceConfigModel2 = newConfig.getConfigList().get(i);
            if (!redisDbSourceConfigModel1.getRedisHost().equals(redisDbSourceConfigModel2.getRedisHost())) {
                return true;
            }
            if (!redisDbSourceConfigModel1.getRedisPort().equals(redisDbSourceConfigModel2.getRedisPort())) {
                return true;
            }
            if (!redisDbSourceConfigModel1.getPassword().equals(redisDbSourceConfigModel2.getPassword())) {
                return true;
            }
            if (!redisDbSourceConfigModel1.getConnectTimeout().equals(redisDbSourceConfigModel2.getConnectTimeout())) {
                return true;
            }
            if (!redisDbSourceConfigModel1.getReadTimeout().equals(redisDbSourceConfigModel2.getReadTimeout())) {
                return true;
            }
            if (!redisDbSourceConfigModel1.getReadOnly().equals(redisDbSourceConfigModel2.getReadOnly())) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        CacheConfigModel cacheConfigModel1 = new CacheConfigModel().setCacheType("a").setClientType(1).setConnectTypeEnum(ConnectTypeEnum.POOL).setUseType(UseTypeEnum.BUSINESS).setConfigSourceType(CacheConfigSourceTypeEnum.APOLLO).setLocalCache(false);
        CacheConfigModel cacheConfigModel2 = new CacheConfigModel().setCacheType("a").setClientType(1).setConnectTypeEnum(ConnectTypeEnum.POOL).setUseType(UseTypeEnum.PUBSUB).setConfigSourceType(CacheConfigSourceTypeEnum.DB).setLocalCache(false);
        System.out.println(checkIsEquals(modelToHashKey(cacheConfigModel1), modelToHashKey(cacheConfigModel2)));
    }
}
