package org.jrl.redis.extend.config.model;

import java.util.List;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: RedisDbConfigModel
 * @Description: 缓存DB配置
 * @date 2021/1/28 8:54 PM
 */
public class RedisDbConfigModel {

    public RedisDbConfigModel() {
    }

    public RedisDbConfigModel(List<RedisDbSourceConfigModel> configList) {
        this.configList = configList;
    }

    /**
     * 数据库配置
     */
    private List<RedisDbSourceConfigModel> configList;

    public List<RedisDbSourceConfigModel> getConfigList() {
        return configList;
    }

    public void setConfigList(List<RedisDbSourceConfigModel> configList) {
        this.configList = configList;
    }
}
