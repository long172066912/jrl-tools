package org.jrl.redis.core.cache.redis.lettuce.connect;

import org.jrl.redis.connect.InterfaceConnectResource;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import org.apache.commons.pool2.impl.GenericObjectPool;
/**
* @Title: LettuceConnectResource
* @Description: Lettuce连接资源对象
* @author JerryLong
* @date 2021/2/1 4:01 PM
* @version V1.0
*/
public class LettuceConnectResource implements InterfaceConnectResource {

    /**
     * 链接资源
     */
    private StatefulRedisConnection statefulRedisConnection;
    /**
     * 连接池
     */
    private GenericObjectPool genericObjectPool;
    /**
     * 集群方式
     */
    private StatefulRedisClusterConnection statefulRedisClusterConnection;

    /**
     * 多连接循环模式
     */
    private LettuceConnectLoop<StatefulRedisConnection> lettuceConnectLoop;

    public StatefulRedisConnection getStatefulRedisConnection() {
        return statefulRedisConnection;
    }

    public LettuceConnectResource setStatefulRedisConnection(StatefulRedisConnection statefulRedisConnection) {
        this.statefulRedisConnection = statefulRedisConnection;
        return this;
    }

    public GenericObjectPool getGenericObjectPool() {
        return genericObjectPool;
    }

    public LettuceConnectResource setGenericObjectPool(GenericObjectPool genericObjectPool) {
        this.genericObjectPool = genericObjectPool;
        return this;
    }

    public StatefulRedisClusterConnection getStatefulRedisClusterConnection() {
        return statefulRedisClusterConnection;
    }

    public LettuceConnectResource setStatefulRedisClusterConnection(StatefulRedisClusterConnection statefulRedisClusterConnection) {
        this.statefulRedisClusterConnection = statefulRedisClusterConnection;
        return this;
    }

    public LettuceConnectLoop<StatefulRedisConnection> getLettuceConnectLoop() {
        return lettuceConnectLoop;
    }

    public LettuceConnectResource setLettuceConnectLoop(LettuceConnectLoop<StatefulRedisConnection> lettuceConnectLoop) {
        this.lettuceConnectLoop = lettuceConnectLoop;
        return this;
    }

    @Override
    public String toString() {
        return "LettuceConnectResource{" +
                "statefulRedisConnection=" + statefulRedisConnection +
                ", genericObjectPool=" + genericObjectPool +
                ", statefulRedisClusterConnection=" + statefulRedisClusterConnection +
                '}';
    }

    @Override
    public void close() {
        if(null != this.statefulRedisConnection){
            this.statefulRedisConnection.closeAsync();
        }
        if(null != this.genericObjectPool){
            this.genericObjectPool.close();
        }
        if(null != this.statefulRedisClusterConnection){
            this.statefulRedisClusterConnection.closeAsync();
        }
    }
}
