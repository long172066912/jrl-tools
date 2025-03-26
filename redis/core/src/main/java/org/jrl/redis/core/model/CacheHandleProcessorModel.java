package org.jrl.redis.core.model;

import org.jrl.redis.util.CacheFunction;

import java.util.Arrays;
import java.util.List;

/**
 * @author JerryLong
 * @version V1.0
 * @Title: CacheHandleProcessorModel
 * @Description: 执行器模板
 * @date 2021/2/23 3:41 PM
 */
public class CacheHandleProcessorModel {

    public CacheHandleProcessorModel(CacheFunction function) {
        this.function = function;
    }

    public CacheHandleProcessorModel(CacheFunction function, String commands, CacheConfigModel cacheConfigModel, String... keys) {
        this.function = function;
        this.cacheConfigModel = cacheConfigModel;
        this.commands = commands;
        this.setKey(keys[0]);
        if(keys.length > 1){
            this.setKeys(Arrays.asList(keys));
        }
    }

    /**
     * 命令
     */
    private String commands;
    /**
     * key
     */
    private String key;
    /**
     * 缓存命中率的key
     */
    private String hitKey;
    /**
     * keys
     */
    private List<String> keys;
    /**
     * 方法
     */
    private CacheFunction function;
    /**
     * 执行结果
     */
    private Object result;
    /**
     * 命令执行时间
     */
    private Long executeTime;
    /**
     * 配置
     */
    private CacheConfigModel cacheConfigModel;
    /**
     * 异常信息
     */
    private Exception e;

    public String getHitKey() {
        return hitKey;
    }

    public void setHitKey(String hitKey) {
        this.hitKey = hitKey;
    }

    public Exception getE() {
        return e;
    }

    public void setE(Exception e) {
        this.e = e;
    }

    public CacheFunction getFunction() {
        return function;
    }

    public void setFunction(CacheFunction function) {
        this.function = function;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public CacheConfigModel getCacheConfigModel() {
        return cacheConfigModel;
    }

    public void setCacheConfigModel(CacheConfigModel cacheConfigModel) {
        this.cacheConfigModel = cacheConfigModel;
    }

    public String getCommands() {
        return commands;
    }

    public void setCommands(String commands) {
        this.commands = commands;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public Long getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(Long executeTime) {
        this.executeTime = executeTime;
    }
}
