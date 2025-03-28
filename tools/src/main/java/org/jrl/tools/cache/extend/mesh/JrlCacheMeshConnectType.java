package org.jrl.tools.cache.extend.mesh;

/**
 * JrlCache redis连接类型
 *
 * @author JerryLong
 */
public enum JrlCacheMeshConnectType {
    /**
     * redis连接类型
     */
    NORMAL,
    /**
     * 连接池
     */
    POOL,
    /**
     * 连接环-固定多连接
     */
    LOOP,
    ;
}
