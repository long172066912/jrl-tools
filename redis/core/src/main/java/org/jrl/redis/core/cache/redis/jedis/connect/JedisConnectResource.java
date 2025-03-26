package org.jrl.redis.core.cache.redis.jedis.connect;

import org.jrl.redis.connect.InterfaceConnectResource;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ShardedJedis;

/**
* @Title: JedisConnectResource
* @Description: Jedis连接资源
* @author JerryLong
* @date 2021/2/1 4:03 PM
* @version V1.0
*/
public class JedisConnectResource implements InterfaceConnectResource {
    /**
     * 单机模式
     */
    private Jedis jedis;
    /**
     * 分片
     */
    private ShardedJedis shardedJedis;
    /**
     * 集群
     */
    private JedisCluster jedisCluster;
    /**
     * 连接池
     */
    private JedisPool jedisPool;

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public JedisConnectResource setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        return this;
    }

    public Jedis getJedis() {
        return jedis;
    }

    public JedisConnectResource setJedis(Jedis jedis) {
        this.jedis = jedis;
        return this;
    }

    public ShardedJedis getShardedJedis() {
        return shardedJedis;
    }

    public JedisConnectResource setShardedJedis(ShardedJedis shardedJedis) {
        this.shardedJedis = shardedJedis;
        return this;
    }

    public JedisCluster getJedisCluster() {
        return jedisCluster;
    }

    public JedisConnectResource setJedisCluster(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
        return this;
    }

    @Override
    public String toString() {
        return "JedisConnectResource{" +
                "jedis=" + jedis +
                ", shardedJedis=" + shardedJedis +
                ", jedisCluster=" + jedisCluster +
                '}';
    }

    @Override
    public void close() throws Exception {
        if(null != this.jedis){
            this.jedis.close();
        }
        if(null != this.shardedJedis){
            this.shardedJedis.close();
        }
        if(null != this.jedisCluster){
            this.jedisCluster.close();
        }
        if(null != this.jedisPool){
            this.jedisPool.close();
        }
    }
}
