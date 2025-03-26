package org.jrl.redis.extend.config.model;

import org.jrl.redis.config.model.CommonCacheConfig;

import java.util.List;

/**
* @Title: ApolloRedisConfig
* @Description: Apollo统一配置
* @author JerryLong
* @date 2021/3/31 3:09 PM
* @version V1.0
*/
public class ApolloRedisConfig {
    /**
     * 节点配置，与DB方式保持一致
     */
    private List<RedisDbSourceConfigModel> nodes;

    /**
     * 公共配置
     */
    private CommonCacheConfig commonConfig = new CommonCacheConfig();

    public List<RedisDbSourceConfigModel> getNodes() {
        return nodes;
    }

    public void setNodes(List<RedisDbSourceConfigModel> nodes) {
        this.nodes = nodes;
    }

    public CommonCacheConfig getCommonConfig() {
        return commonConfig;
    }

    public void setCommonConfig(CommonCacheConfig commonConfig) {
        this.commonConfig = commonConfig;
    }
}
