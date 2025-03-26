package org.jrl.redis.extend.config;

import org.jrl.redis.core.cache.redis.jedis.config.JedisClusterConnectSourceConfig;
import org.jrl.redis.core.cache.redis.jedis.config.JedisConnectSourceConfig;
import org.jrl.redis.core.cache.redis.jedis.config.JedisShardConnectSourceConfig;
import org.jrl.redis.core.cache.redis.lettuce.config.LettuceClusterConnectSourceConfig;
import org.jrl.redis.core.cache.redis.lettuce.config.LettuceConnectSourceConfig;
import org.jrl.redis.extend.config.model.RedisDbConfigModel;
import org.jrl.redis.extend.config.model.RedisDbSourceConfigModel;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisShardInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
* @Title: DbConfigConvertUtils
* @Description: DB配置转换帮助类
* @author JerryLong
* @date 2021/7/25 4:49 PM
* @version V1.0
*/
public class DbConfigConvertUtils {

    /**
     * DB配置转JedisConnectSourceConfig
     * @param redisDbConfigModel
     * @return
     */
    public static JedisConnectSourceConfig dbConfigToJedisConnectSourceConfig(RedisDbConfigModel redisDbConfigModel){
        JedisConnectSourceConfig jedisConnectSourceConfig = new JedisConnectSourceConfig();
        jedisConnectSourceConfig.setHost(redisDbConfigModel.getConfigList().get(0).getRedisHost());
        jedisConnectSourceConfig.setPort(redisDbConfigModel.getConfigList().get(0).getRedisPort());
        jedisConnectSourceConfig.setPwd(redisDbConfigModel.getConfigList().get(0).getPassword());
        jedisConnectSourceConfig.setTimeout(redisDbConfigModel.getConfigList().get(0).getConnectTimeout());
        jedisConnectSourceConfig.setSoTimeout(redisDbConfigModel.getConfigList().get(0).getReadTimeout());
        //TODO 设置其他参数
        return jedisConnectSourceConfig;
    }

    /**
     * JedisShardConnectSourceConfig
     * @param redisDbConfigModel
     * @return
     */
    public static JedisShardConnectSourceConfig dbConfigToJedisShardConnectSourceConfig(RedisDbConfigModel redisDbConfigModel){
        JedisShardConnectSourceConfig jedisShardConnectSourceConfig = new JedisShardConnectSourceConfig();
        List<JedisShardInfo> list = new ArrayList<>();
        for (RedisDbSourceConfigModel redisDbSourceConfigModel : redisDbConfigModel.getConfigList()) {
            JedisShardInfo jedisShardInfo = new JedisShardInfo(redisDbSourceConfigModel.getRedisHost(), redisDbSourceConfigModel.getRedisPort());
            jedisShardInfo.setSoTimeout(redisDbSourceConfigModel.getReadTimeout());
            jedisShardInfo.setConnectionTimeout(redisDbSourceConfigModel.getConnectTimeout());
            jedisShardInfo.setPassword(redisDbSourceConfigModel.getPassword());
            list.add(jedisShardInfo);
        }
        jedisShardConnectSourceConfig.setShards(list);
        return jedisShardConnectSourceConfig;
    }

    /**
     * DB配置转JedisClusterConnectSourceConfig
     * @param redisDbConfigModel
     * @return
     */
    public static JedisClusterConnectSourceConfig dbConfigToJedisClusterConnectSourceConfig(RedisDbConfigModel redisDbConfigModel){
        JedisClusterConnectSourceConfig jedisClusterConnectSourceConfig = new JedisClusterConnectSourceConfig();
        jedisClusterConnectSourceConfig.setPwd(redisDbConfigModel.getConfigList().get(0).getPassword());
        jedisClusterConnectSourceConfig.setTimeout(redisDbConfigModel.getConfigList().get(0).getConnectTimeout());
        jedisClusterConnectSourceConfig.setSoTimeout(redisDbConfigModel.getConfigList().get(0).getReadTimeout());
        Set<HostAndPort> set = new HashSet<>();
        for (RedisDbSourceConfigModel redisDbSourceConfigModel : redisDbConfigModel.getConfigList()) {
            set.add(new HostAndPort(redisDbSourceConfigModel.getRedisHost(),redisDbSourceConfigModel.getRedisPort()));
        }
        jedisClusterConnectSourceConfig.setNodes(set);
        return jedisClusterConnectSourceConfig;
    }

    /**
     * DB配置转LettuceConnectSourceConfig
     * @param redisDbConfigModel
     * @return
     */
    public static LettuceConnectSourceConfig dbConfigToLettuceConnectSourceConfig(RedisDbConfigModel redisDbConfigModel){
        LettuceConnectSourceConfig lettuceConnectSourceConfig = new LettuceConnectSourceConfig();
        lettuceConnectSourceConfig.setHost(redisDbConfigModel.getConfigList().get(0).getRedisHost());
        lettuceConnectSourceConfig.setPort(redisDbConfigModel.getConfigList().get(0).getRedisPort());
        lettuceConnectSourceConfig.setPwd(redisDbConfigModel.getConfigList().get(0).getPassword());
        lettuceConnectSourceConfig.setTimeout(redisDbConfigModel.getConfigList().get(0).getConnectTimeout());
        lettuceConnectSourceConfig.setSoTimeout(redisDbConfigModel.getConfigList().get(0).getReadTimeout());
        return lettuceConnectSourceConfig;
    }
    /**
     /**
     * DB配置转LettuceClusterConnectSourceConfig
     * @param redisDbConfigModel
     * @return
     */
    public static LettuceClusterConnectSourceConfig dbConfigToLettuceClusterConnectSourceConfig(RedisDbConfigModel redisDbConfigModel){
        LettuceClusterConnectSourceConfig lettuceClusterConnectSourceConfig = new LettuceClusterConnectSourceConfig();
        lettuceClusterConnectSourceConfig.setSoTimeout(redisDbConfigModel.getConfigList().get(0).getReadTimeout());
        Set<LettuceConnectSourceConfig> set = new HashSet<>();
        for (RedisDbSourceConfigModel redisDbSourceConfigModel : redisDbConfigModel.getConfigList()) {
            set.add(new LettuceConnectSourceConfig(redisDbSourceConfigModel.getRedisHost(),redisDbSourceConfigModel.getRedisPort(),redisDbSourceConfigModel.getPassword(),redisDbSourceConfigModel.getConnectTimeout()));
        }
        lettuceClusterConnectSourceConfig.setNodes(set);
        return lettuceClusterConnectSourceConfig;
    }
}
