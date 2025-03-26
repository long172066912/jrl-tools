package org.jrl.redis.extend.config.model;

/**
* @Title: RedisDbSourceConfigModel
* @Description: 数据库配置，SourceScheduled定时检测是否变更
* @author JerryLong
* @date 2021/1/29 8:36 PM
* @version V1.0
*/
public class RedisDbSourceConfigModel {
    /**
     * 资源名称
     */
    private String sourceName;
    /**
     * host
     */
    private String redisHost;
    /**
     * 端口
     */
    private Integer redisPort;
    /**
     * 密码
     */
    private String password;
    /**
     * 连接超时时间
     */
    private Integer connectTimeout;
    /**
     * 命令执行超时时间
     */
    private Integer readTimeout;
    /**
     * readOnly
     */
    private Integer readOnly;

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }

    public Integer getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(Integer redisPort) {
        this.redisPort = redisPort;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Integer getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Integer readOnly) {
        this.readOnly = readOnly;
    }
}
