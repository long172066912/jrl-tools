package org.jrl.redis.core.cache.redis.jedis.config;

import org.jrl.redis.config.BaseCacheConfig;
import redis.clients.jedis.JedisShardInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @Title: JedisShardConnectSourceConfig
* @Description: shard方式连接
* @author JerryLong
* @date 2021/1/27 5:37 PM
* @version V1.0
*/
public class JedisShardConnectSourceConfig extends BaseCacheConfig {
    /**
     * 节点信息
     */
    private List<JedisShardInfo> shards;

    @Override
    public List<String> getHosts() {
        return new ArrayList<>(shards.stream().map(e -> e.getHost()).collect(Collectors.toSet()));
    }

    public List<JedisShardInfo> getShards() {
        return shards;
    }

    public JedisShardConnectSourceConfig setShards(List<JedisShardInfo> shards) {
        this.shards = shards;
        return this;
    }
}
