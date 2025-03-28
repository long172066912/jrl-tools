package org.jrl.tools.cache.extend.model;

/**
 * JrlCache消费实体
 *
 * @author JerryLong
 */
public class JrlCacheSubscribeVo {
    /**
     * 缓存名称
     */
    private String cacheName;
    /**
     * 消息类型
     */
    private JrlCacheChannelHandleType handleType;
    /**
     * 数据
     */
    private String msg;

    public JrlCacheSubscribeVo() {
    }

    public JrlCacheSubscribeVo(String cacheName, JrlCacheChannelHandleType handleType, String msg) {
        this.cacheName = cacheName;
        this.handleType = handleType;
        this.msg = msg;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public JrlCacheChannelHandleType getHandleType() {
        return handleType;
    }

    public void setHandleType(JrlCacheChannelHandleType handleType) {
        this.handleType = handleType;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
