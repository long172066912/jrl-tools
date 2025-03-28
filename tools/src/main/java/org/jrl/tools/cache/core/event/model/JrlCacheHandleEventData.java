package org.jrl.tools.cache.core.event.model;

import org.jrl.tools.cache.model.JrlCacheType;

import java.util.List;

/**
 * JrlCache命令实现数据模型
 *
 * @author JerryLong
 */
public class JrlCacheHandleEventData {
    /**
     * 缓存名称
     */
    private String cacheName;
    /**
     * 缓存类型
     */
    private JrlCacheType type;
    /**
     * 命令
     */
    private String command;
    /**
     * 是否命中
     */
    private Boolean isHit;
    /**
     * 缓存key
     */
    private List<Key> key;

    public JrlCacheHandleEventData() {
    }

    public JrlCacheHandleEventData(String cacheName, JrlCacheType type, String command, Boolean isHit, List<Key> key) {
        this.cacheName = cacheName;
        this.type = type;
        this.command = command;
        this.isHit = isHit;
        this.key = key;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public JrlCacheType getType() {
        return type;
    }

    public void setType(JrlCacheType type) {
        this.type = type;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Boolean getHit() {
        return isHit;
    }

    public void setHit(Boolean hit) {
        isHit = hit;
    }

    public List<Key> getKey() {
        return key;
    }

    public void setKey(List<Key> key) {
        this.key = key;
    }

    public static class Key {
        /**
         * 缓存key
         */
        private String key;
        /**
         * 是否命中
         */
        private Boolean isHit;
        /**
         * 统计命中的key，如果没设置，则数字转*，截取前16位
         */
        private String hitKey;

        public Key() {
        }

        public Key(String key, Boolean isHit, String hitKey) {
            this.key = key;
            this.isHit = isHit;
            this.hitKey = hitKey;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Boolean getHit() {
            return isHit;
        }

        public void setHit(Boolean hit) {
            isHit = hit;
        }

        public String getHitKey() {
            return hitKey;
        }

        public void setHitKey(String hitKey) {
            this.hitKey = hitKey;
        }
    }
}