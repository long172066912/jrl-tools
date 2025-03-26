package org.jrl.redis.connect;

import org.jrl.redis.core.cache.redis.jedis.connect.JedisConnectResource;
import org.jrl.redis.core.cache.redis.lettuce.connect.LettuceConnectResource;

import java.util.concurrent.locks.StampedLock;

/**
* @Title: ConnectResource
* @Description: 缓存连接资源
* @author JerryLong
* @date 2021/2/1 4:03 PM
* @version V1.0
*/
public class ConnectResource implements InterfaceConnectResource{
    /**
     * 连接资源控制锁，当资源替换时，通过此锁控制
     */
    private StampedLock stampedLock = new StampedLock();

    private JedisConnectResource jedisConnectResource;

    private LettuceConnectResource lettuceConnectResource;

    public JedisConnectResource getJedisConnectResource() {
        return jedisConnectResource;
    }

    public ConnectResource setJedisConnectResource(JedisConnectResource jedisConnectResource) {
        this.jedisConnectResource = jedisConnectResource;
        return this;
    }

    public LettuceConnectResource getLettuceConnectResource() {
        return lettuceConnectResource;
    }

    public ConnectResource setLettuceConnectResource(LettuceConnectResource lettuceConnectResource) {
        this.lettuceConnectResource = lettuceConnectResource;
        return this;
    }

    @Override
    public String toString() {
        return "ConnectResource{" +
                "jedisConnectResource=" + jedisConnectResource +
                ", lettuceConnectResource=" + lettuceConnectResource +
                '}';
    }

    @Override
    public void close() throws Exception{
        if(null != this.jedisConnectResource){
            this.jedisConnectResource.close();
        }
        if(null != this.lettuceConnectResource){
            this.lettuceConnectResource.close();
        }
    }

    public StampedLock getStampedLock() {
        return stampedLock;
    }

    public InterfaceConnectResource getResource(){
        if(null != this.jedisConnectResource){
            return this.jedisConnectResource;
        }
        if(null != this.lettuceConnectResource){
            return this.lettuceConnectResource;
        }
        return null;
    }
}
