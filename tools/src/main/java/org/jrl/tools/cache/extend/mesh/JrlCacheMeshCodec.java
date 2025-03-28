package org.jrl.tools.cache.extend.mesh;

/**
 * 数据编解码
 *
 * @author JerryLong
 */
public interface JrlCacheMeshCodec<V, V1> {
    /**
     * 编码
     *
     * @param value 待编码的值
     * @return 编码后的值
     */
    V1 encode(V value);

    /**
     * 解码
     *
     * @param value 待解码的值
     * @return 解码后的值
     */
    V decode(V1 value);
}
