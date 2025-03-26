package org.jrl.redis.config;

import org.jrl.redis.config.model.CommonCacheConfig;

import java.util.List;

/**
* @Title: BaseCacheConfig
* @Description: 缓存配置抽象
* @author JerryLong
* @date 2021/2/24 3:26 PM
* @version V1.0
*/
public abstract class BaseCacheConfig implements InterfaceCacheConfig{

    /**
     * 公共配置
     */
    private CommonCacheConfig commonCacheConfig = new CommonCacheConfig();

    public CommonCacheConfig getCommonCacheConfig() {
        return commonCacheConfig;
    }

    public void setCommonCacheConfig(CommonCacheConfig commonCacheConfig) {
        this.commonCacheConfig = commonCacheConfig;
    }

    /**
     * 获取host
     * @return
     */
    public abstract List<String> getHosts();
}
