package org.jrl.tools.cache.hotkey;

/**
* 热key配置
* @author JerryLong
*/
public class JrlCacheHotKeyConfig {
    private int statSeconds = 60;
    private boolean statHotKey = false;
    private boolean autoCacheHotKey = false;
    /**
     * 热key缓存容量，默认50
     */
    private int capacity = 50;
    /**
     * 热key最小值，默认50
     */
    private int countLeastValue = 1000;
    /**
     * 本地缓存时间，默认10秒
     */
    private int localCacheSeconds = 10;

    public JrlCacheHotKeyConfig() {
    }

    public JrlCacheHotKeyConfig(int statSeconds, boolean statHotKey, boolean autoCacheHotKey, int capacity, int countLeastValue, int localCacheSeconds) {
        this.statSeconds = statSeconds;
        this.statHotKey = statHotKey;
        this.autoCacheHotKey = autoCacheHotKey;
        this.capacity = capacity;
        this.countLeastValue = countLeastValue;
        this.localCacheSeconds = localCacheSeconds;
    }

    public int getStatSeconds() {
        return statSeconds;
    }

    public void setStatSeconds(int statSeconds) {
        this.statSeconds = statSeconds;
    }

    public boolean isStatHotKey() {
        return statHotKey;
    }

    public void setStatHotKey(boolean statHotKey) {
        this.statHotKey = statHotKey;
    }

    public boolean isAutoCacheHotKey() {
        return autoCacheHotKey;
    }

    public void setAutoCacheHotKey(boolean autoCacheHotKey) {
        this.autoCacheHotKey = autoCacheHotKey;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCountLeastValue() {
        return countLeastValue;
    }

    public void setCountLeastValue(int countLeastValue) {
        this.countLeastValue = countLeastValue;
    }

    public int getLocalCacheSeconds() {
        return localCacheSeconds;
    }

    public void setLocalCacheSeconds(int localCacheSeconds) {
        this.localCacheSeconds = localCacheSeconds;
    }
}
